using System.Text;
using RabbitMQ.Client;

namespace Acme.Payments.CardPaymentService.Messaging;

public static class RabbitPaymentTopology
{
    public const string PaymentEventsExchange = "payments.events";
    public const string PaymentProcessingRoutingKey = "PaymentProcessing";
    public const string CardQueue = "payment-events.card";
    public const string CardRetryQueue = "payment-events.card.retry";
    public const string CardDeadLetterQueue = "payment-events.card.dlq";
    public const string CardDispatchExchange = "payment-events.card.dispatch";
    public const string CardRetryExchange = "payment-events.card.retry";
    public const string CardDeadLetterExchange = "payment-events.card.dlx";
    public const string DispatchRoutingKey = "payment";
    public const string RetryRoutingKey = "retry";
    public const string DeadLetterRoutingKey = "dlq";

    public static void Declare(IModel channel, RabbitPaymentConsumerOptions options)
    {
        channel.ExchangeDeclare(PaymentEventsExchange, ExchangeType.Topic, durable: true, autoDelete: false);
        channel.ExchangeDeclare(CardDispatchExchange, ExchangeType.Direct, durable: true, autoDelete: false);
        channel.ExchangeDeclare(CardRetryExchange, ExchangeType.Direct, durable: true, autoDelete: false);
        channel.ExchangeDeclare(CardDeadLetterExchange, ExchangeType.Direct, durable: true, autoDelete: false);

        channel.QueueDeclare(
            CardQueue,
            durable: true,
            exclusive: false,
            autoDelete: false,
            arguments: new Dictionary<string, object>
            {
                ["x-dead-letter-exchange"] = CardRetryExchange,
                ["x-dead-letter-routing-key"] = RetryRoutingKey
            }
        );

        channel.QueueDeclare(
            CardRetryQueue,
            durable: true,
            exclusive: false,
            autoDelete: false,
            arguments: new Dictionary<string, object>
            {
                ["x-message-ttl"] = options.RetryDelayMilliseconds,
                ["x-dead-letter-exchange"] = CardDispatchExchange,
                ["x-dead-letter-routing-key"] = DispatchRoutingKey
            }
        );

        channel.QueueDeclare(CardDeadLetterQueue, durable: true, exclusive: false, autoDelete: false);
        channel.QueueBind(CardQueue, PaymentEventsExchange, PaymentProcessingRoutingKey);
        channel.QueueBind(CardQueue, CardDispatchExchange, DispatchRoutingKey);
        channel.QueueBind(CardRetryQueue, CardRetryExchange, RetryRoutingKey);
        channel.QueueBind(CardDeadLetterQueue, CardDeadLetterExchange, DeadLetterRoutingKey);
    }

    public static long DeadLettersFrom(IDictionary<string, object>? headers, string queue)
    {
        if (headers is null || !headers.TryGetValue("x-death", out object? deaths) || deaths is not IList<object> entries)
        {
            return 0;
        }

        foreach (object entry in entries)
        {
            if (entry is not IDictionary<string, object> death)
            {
                continue;
            }

            string deathQueue = HeaderText(death.TryGetValue("queue", out object? queueValue) ? queueValue : null);
            if (!string.Equals(deathQueue, queue, StringComparison.Ordinal))
            {
                continue;
            }

            return death.TryGetValue("count", out object? count) ? HeaderLong(count) : 0;
        }

        return 0;
    }

    private static string HeaderText(object? value)
    {
        return value switch
        {
            byte[] bytes => Encoding.UTF8.GetString(bytes),
            string text => text,
            null => string.Empty,
            _ => value.ToString() ?? string.Empty
        };
    }

    private static long HeaderLong(object? value)
    {
        return value switch
        {
            long number => number,
            int number => number,
            short number => number,
            byte number => number,
            null => 0,
            _ => long.TryParse(value.ToString(), out long parsed) ? parsed : 0
        };
    }
}
