using System.Text;
using System.Text.Json;
using Acme.Payments.CardPaymentService.Application;
using Microsoft.Extensions.Options;
using RabbitMQ.Client;
using RabbitMQ.Client.Events;

namespace Acme.Payments.CardPaymentService.Messaging;

public sealed class RabbitPaymentConsumer : BackgroundService
{
    private static readonly JsonSerializerOptions JsonOptions = new(JsonSerializerDefaults.Web);

    private readonly RabbitPaymentConsumerOptions options;
    private readonly PaymentProcessingCommandMapper commandMapper;
    private readonly ApplicationCardPaymentService cardPaymentService;
    private readonly ILogger<RabbitPaymentConsumer> logger;
    private IConnection? connection;
    private IModel? channel;

    public RabbitPaymentConsumer(
        IOptions<RabbitPaymentConsumerOptions> options,
        PaymentProcessingCommandMapper commandMapper,
        ApplicationCardPaymentService cardPaymentService,
        ILogger<RabbitPaymentConsumer> logger)
    {
        this.options = options.Value;
        this.commandMapper = commandMapper;
        this.cardPaymentService = cardPaymentService;
        this.logger = logger;
    }

    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        while (!stoppingToken.IsCancellationRequested)
        {
            try
            {
                StartConsumer();
                await Task.Delay(Timeout.InfiniteTimeSpan, stoppingToken);
            }
            catch (OperationCanceledException) when (stoppingToken.IsCancellationRequested)
            {
                return;
            }
            catch (Exception exception)
            {
                logger.LogWarning(exception, "RabbitMQ card consumer is unavailable; retrying connection");
                await Task.Delay(TimeSpan.FromSeconds(5), stoppingToken);
            }
        }
    }

    public override void Dispose()
    {
        channel?.Dispose();
        connection?.Dispose();
        base.Dispose();
    }

    private void StartConsumer()
    {
        var factory = new ConnectionFactory
        {
            HostName = options.Host,
            Port = options.Port,
            UserName = options.Username,
            Password = options.Password,
            DispatchConsumersAsync = true
        };

        connection = factory.CreateConnection("card-payment-csharp");
        channel = connection.CreateModel();
        channel.BasicQos(0, 10, false);
        RabbitPaymentTopology.Declare(channel, options);

        var consumer = new AsyncEventingBasicConsumer(channel);
        consumer.Received += HandleMessageAsync;
        channel.BasicConsume(RabbitPaymentTopology.CardQueue, autoAck: false, consumer);
    }

    private async Task HandleMessageAsync(object sender, BasicDeliverEventArgs delivery)
    {
        if (channel is null)
        {
            return;
        }

        string payload = Encoding.UTF8.GetString(delivery.Body.Span);
        string correlationId = string.Empty;
        try
        {
            PaymentProcessingEvent? paymentEvent = JsonSerializer.Deserialize<PaymentProcessingEvent>(payload, JsonOptions);
            if (paymentEvent is not null)
            {
                correlationId = IsSafeCorrelationId(paymentEvent.CorrelationId) ? paymentEvent.CorrelationId : Guid.NewGuid().ToString();
                using (logger.BeginScope(new Dictionary<string, object> { ["CorrelationId"] = correlationId }))
                {
                    var command = commandMapper.ToCommand(paymentEvent);
                    if (command is not null)
                    {
                        await cardPaymentService.AuthorizeAsync(command, CancellationToken.None);
                    }
                    logger.LogInformation("payment_event outcome=processed service=card-payment event_type={EventType} payment_id={PaymentId} correlation_id={CorrelationId}", paymentEvent.EventType, paymentEvent.PaymentId, correlationId);
                }
            }

            channel.BasicAck(delivery.DeliveryTag, false);
        }
        catch (Exception exception)
        {
            if (ShouldDeadLetter(delivery))
            {
                PublishToDeadLetter(payload);
                channel.BasicAck(delivery.DeliveryTag, false);
                logger.LogError(exception, "payment_event outcome=dead_letter service=card-payment correlation_id={CorrelationId}", correlationId);
                return;
            }

            channel.BasicReject(delivery.DeliveryTag, false);
            logger.LogWarning(exception, "payment_event outcome=retry service=card-payment correlation_id={CorrelationId}", correlationId);
        }
    }

    private static bool IsSafeCorrelationId(string value) =>
        value is { Length: > 0 and <= 128 } && value.All(c => char.IsLetterOrDigit(c) || "._:-".Contains(c));

    private bool ShouldDeadLetter(BasicDeliverEventArgs delivery)
    {
        long retryCount = RabbitPaymentTopology.DeadLettersFrom(
            delivery.BasicProperties.Headers,
            RabbitPaymentTopology.CardQueue
        );
        return retryCount >= options.MaxRetryCount;
    }

    private void PublishToDeadLetter(string payload)
    {
        if (channel is null)
        {
            return;
        }

        byte[] body = Encoding.UTF8.GetBytes(payload);
        IBasicProperties properties = channel.CreateBasicProperties();
        properties.ContentType = "application/json";
        properties.Persistent = true;

        channel.BasicPublish(
            RabbitPaymentTopology.CardDeadLetterExchange,
            RabbitPaymentTopology.DeadLetterRoutingKey,
            properties,
            body
        );
    }
}
