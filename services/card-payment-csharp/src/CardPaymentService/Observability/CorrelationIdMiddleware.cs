using System.Diagnostics;

namespace Acme.Payments.CardPaymentService.Observability;

public sealed class CorrelationIdMiddleware
{
    public const string Header = "X-Correlation-Id";
    private const string LogScopeKey = "CorrelationId";

    private readonly RequestDelegate next;

    public CorrelationIdMiddleware(RequestDelegate next)
    {
        this.next = next;
    }

    public async Task InvokeAsync(HttpContext context, ILogger<CorrelationIdMiddleware> logger)
    {
        string correlationId = context.Request.Headers[Header].FirstOrDefault() ?? string.Empty;
        if (!IsSafeCorrelationId(correlationId))
        {
            correlationId = Guid.NewGuid().ToString();
        }

        context.Response.Headers[Header] = correlationId;
        long startedAt = Stopwatch.GetTimestamp();
        using (logger.BeginScope(new Dictionary<string, object> { [LogScopeKey] = correlationId }))
        {
            try
            {
                await next(context);
            }
            finally
            {
                logger.LogInformation("http_outcome service=card-payment method={Method} route={Route} status={Status} duration_ms={DurationMs} correlation_id={CorrelationId}", context.Request.Method, context.Request.Path, context.Response.StatusCode, Stopwatch.GetElapsedTime(startedAt).TotalMilliseconds, correlationId);
            }
        }
    }

    private static bool IsSafeCorrelationId(string? value) =>
        value is { Length: > 0 and <= 128 } && value.All(c => char.IsLetterOrDigit(c) || "._:-".Contains(c));
}
