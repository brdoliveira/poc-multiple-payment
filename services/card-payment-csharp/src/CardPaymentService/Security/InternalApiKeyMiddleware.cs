namespace Acme.Payments.CardPaymentService.Security;

public sealed class InternalApiKeyMiddleware
{
    private const string ApiKeyHeader = "X-Internal-Api-Key";

    private readonly RequestDelegate next;
    private readonly string apiKey;

    public InternalApiKeyMiddleware(RequestDelegate next, IConfiguration configuration)
    {
        this.next = next;
        apiKey = configuration["Security:ApiKey"] ?? string.Empty;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        if (string.IsNullOrWhiteSpace(apiKey) ||
            context.Request.Path.StartsWithSegments("/health") ||
            context.Request.Path.StartsWithSegments("/webhooks"))
        {
            await next(context);
            return;
        }

        if (!string.Equals(context.Request.Headers[ApiKeyHeader].FirstOrDefault(), apiKey, StringComparison.Ordinal))
        {
            context.Response.StatusCode = StatusCodes.Status401Unauthorized;
            await context.Response.WriteAsync("invalid internal api key");
            return;
        }

        await next(context);
    }
}
