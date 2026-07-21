using Acme.Payments.CardPaymentService.Application;
using Acme.Payments.CardPaymentService.Domain;
using Acme.Payments.CardPaymentService.Infrastructure;
using Acme.Payments.CardPaymentService.Providers;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddSingleton<IIdempotencyStore, InMemoryIdempotencyStore>();
builder.Services.AddSingleton<ICardProviderAdapter, AsaasCardAdapter>();
builder.Services.AddSingleton<ICardProviderAdapter, MercadoPagoCardAdapter>();
builder.Services.AddSingleton<ICardProviderAdapter, PagBankCardAdapter>();
builder.Services.AddSingleton<ICardProviderAdapter, IuguCardAdapter>();
builder.Services.AddSingleton<ICardProviderAdapter, StripeCardAdapter>();
builder.Services.AddSingleton<ApplicationCardPaymentService>();

var app = builder.Build();

app.MapPost("/cards/authorizations", async (
    CardPaymentCommand command,
    ApplicationCardPaymentService service,
    CancellationToken cancellationToken) =>
{
    CardPaymentResult result = await service.AuthorizeAsync(command, cancellationToken);
    return Results.Accepted($"/cards/authorizations/{result.PaymentId}", result);
});

app.Run();

public partial class Program;
