using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Hosting;
using System.Security.Cryptography;
using System.Text;

namespace Acme.Payments.CardPaymentService.Webhooks;

public sealed class WebhookSignatureValidator : IWebhookSignatureValidator
{
    private readonly IConfiguration configuration;
    private readonly IWebHostEnvironment environment;

    public WebhookSignatureValidator(IConfiguration configuration, IWebHostEnvironment environment)
    {
        this.configuration = configuration;
        this.environment = environment;
    }

    public bool IsValid(string provider, string payload, string signature)
    {
        string? secret = configuration[$"Providers:{provider}:WebhookSecret"];
        if (string.IsNullOrWhiteSpace(secret))
        {
            return environment.IsDevelopment();
        }

        byte[] secretBytes = Encoding.UTF8.GetBytes(secret);
        byte[] payloadBytes = Encoding.UTF8.GetBytes(payload);
        byte[] hash = HMACSHA256.HashData(secretBytes, payloadBytes);
        string expected = Convert.ToHexString(hash).ToLowerInvariant();

        return CryptographicOperations.FixedTimeEquals(
            Encoding.UTF8.GetBytes(expected),
            Encoding.UTF8.GetBytes(signature.ToLowerInvariant())
        );
    }
}
