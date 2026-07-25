namespace Acme.Payments.CardPaymentService.Messaging;

public sealed class RabbitPaymentConsumerOptions
{
    public string Host { get; init; } = "localhost";

    public int Port { get; init; } = 5672;

    public string Username { get; init; } = "guest";

    public string Password { get; init; } = "guest";

    public int MaxRetryCount { get; init; } = 3;

    public int RetryDelayMilliseconds { get; init; } = 30_000;
}
