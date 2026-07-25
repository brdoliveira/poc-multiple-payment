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
        string? correlationId = context.Request.Headers[Header].FirstOrDefault();
        if (string.IsNullOrWhiteSpace(correlationId))
        {
            correlationId = Guid.NewGuid().ToString();
        }

        context.Response.Headers[Header] = correlationId;
        using (logger.BeginScope(new Dictionary<string, object> { [LogScopeKey] = correlationId }))
        {
            await next(context);
        }
    }
}
