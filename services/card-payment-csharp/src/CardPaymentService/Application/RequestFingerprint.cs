using System.Security.Cryptography;
using System.Text;
using Acme.Payments.CardPaymentService.Domain;

namespace Acme.Payments.CardPaymentService.Application;

public static class RequestFingerprint
{
    public static string Of(CardPaymentCommand command)
    {
        string canonical = string.Join('|',
            command.Amount.ToString("0.####", System.Globalization.CultureInfo.InvariantCulture),
            command.Currency.ToUpperInvariant(),
            command.Installments,
            command.PreferredProvider?.ToString() ?? string.Empty,
            HashSensitive(command.CardToken));

        return Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(canonical))).ToLowerInvariant();
    }

    private static string HashSensitive(string value)
    {
        return Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(value))).ToLowerInvariant();
    }
}
