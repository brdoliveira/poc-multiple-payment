using Acme.Payments.CardPaymentService.Application;
using Acme.Payments.CardPaymentService.Domain;
using Acme.Payments.CardPaymentService.Infrastructure;
using Acme.Payments.CardPaymentService.Messaging;
using Acme.Payments.CardPaymentService.Observability;
using Acme.Payments.CardPaymentService.Providers;
using Acme.Payments.CardPaymentService.Security;
using Acme.Payments.CardPaymentService.Webhooks;
using Npgsql;
using System.Text.Json.Serialization;

var builder = WebApplication.CreateBuilder(args);

string postgresConnectionString = builder.Configuration.GetConnectionString("Postgres")
                                  ?? "Host=localhost;Port=5432;Database=payments;Username=payments;Password=payments";

DatabaseMigrator.Migrate(postgresConnectionString);

builder.Services.AddSingleton(NpgsqlDataSource.Create(postgresConnectionString));
builder.Services.AddSingleton<IIdempotencyStore, PostgresIdempotencyStore>();
builder.Services.AddSingleton<ICardProviderAdapter, AsaasCardAdapter>();
builder.Services.AddSingleton<ICardProviderAdapter, MercadoPagoCardAdapter>();
builder.Services.AddSingleton<ICardProviderAdapter, PagBankCardAdapter>();
builder.Services.AddSingleton<ICardProviderAdapter, IuguCardAdapter>();
builder.Services.AddSingleton<ICardProviderAdapter, StripeCardAdapter>();
builder.Services.AddSingleton<ApplicationCardPaymentService>();
builder.Services.AddSingleton<IWebhookPayloadStore, MongoWebhookPayloadStore>();
builder.Services.AddSingleton<IWebhookSignatureValidator, WebhookSignatureValidator>();
builder.Services.Configure<RabbitPaymentConsumerOptions>(builder.Configuration.GetSection("RabbitMq"));
builder.Services.AddSingleton<PaymentProcessingCommandMapper>();
builder.Services.AddHostedService<RabbitPaymentConsumer>();
builder.Services.AddHealthChecks();
builder.Services.ConfigureHttpJsonOptions(options =>
{
    options.SerializerOptions.Converters.Add(new JsonStringEnumConverter());
});

var app = builder.Build();

app.UseMiddleware<CorrelationIdMiddleware>();
app.UseMiddleware<InternalApiKeyMiddleware>();

app.MapHealthChecks("/health");

app.MapPost("/cards/authorizations", async (
    CardPaymentCommand command,
    ApplicationCardPaymentService service,
    CancellationToken cancellationToken) =>
{
    CardPaymentResult result = await service.AuthorizeAsync(command, cancellationToken);
    return Results.Accepted($"/cards/authorizations/{result.PaymentId}", result);
});

app.MapPost("/webhooks/{provider}", async (
    string provider,
    HttpRequest request,
    IWebhookPayloadStore payloadStore,
    IWebhookSignatureValidator signatureValidator,
    CancellationToken cancellationToken) =>
{
    using var reader = new StreamReader(request.Body);
    string payload = await reader.ReadToEndAsync(cancellationToken);
    string signature = request.Headers["X-Webhook-Signature"].FirstOrDefault() ?? string.Empty;

    if (!signatureValidator.IsValid(provider, payload, signature))
    {
        return Results.Unauthorized();
    }

    var webhook = ProviderWebhookPayload.From(provider, payload, signature);
    await payloadStore.SaveAsync(webhook, cancellationToken);
    return Results.Accepted($"/webhooks/{provider}/{webhook.ExternalEventId}", webhook);
});

app.Run();

public partial class Program;
