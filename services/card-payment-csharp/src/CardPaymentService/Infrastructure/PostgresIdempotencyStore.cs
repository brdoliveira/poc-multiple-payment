using Acme.Payments.CardPaymentService.Domain;
using Npgsql;

namespace Acme.Payments.CardPaymentService.Infrastructure;

public sealed class PostgresIdempotencyStore : IIdempotencyStore
{
    private readonly NpgsqlDataSource dataSource;

    public PostgresIdempotencyStore(NpgsqlDataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public async Task<CardPaymentResult?> FindAsync(string key, CancellationToken cancellationToken)
    {
        await using NpgsqlCommand command = dataSource.CreateCommand("""
            SELECT id, provider, status, external_authorization_id
            FROM card_payments
            WHERE idempotency_key = @idempotencyKey
            """);
        command.Parameters.AddWithValue("idempotencyKey", key);

        await using NpgsqlDataReader reader = await command.ExecuteReaderAsync(cancellationToken);
        if (!await reader.ReadAsync(cancellationToken))
        {
            return null;
        }

        return new CardPaymentResult(
            reader.GetGuid(0),
            Enum.Parse<PaymentProvider>(reader.GetString(1)),
            Enum.Parse<CardPaymentStatus>(reader.GetString(2)),
            reader.GetString(3)
        );
    }

    public async Task<string?> FindFingerprintAsync(string key, CancellationToken cancellationToken)
    {
        await using NpgsqlCommand command = dataSource.CreateCommand("""
            SELECT request_fingerprint
            FROM card_payments
            WHERE idempotency_key = @idempotencyKey
            """);
        command.Parameters.AddWithValue("idempotencyKey", key);
        return await command.ExecuteScalarAsync(cancellationToken) as string;
    }

    public async Task<bool> TryReserveAsync(
        string key,
        CardPaymentCommand cardCommand,
        CardPaymentResult reservation,
        string requestFingerprint,
        CancellationToken cancellationToken)
    {
        await using NpgsqlCommand command = dataSource.CreateCommand("""
            INSERT INTO card_payments (
                id, idempotency_key, provider, amount, currency, installments,
                status, external_authorization_id, request_fingerprint, created_at, updated_at
            )
            VALUES (
                @id, @idempotencyKey, @provider, @amount, @currency, @installments,
                @status, @externalAuthorizationId, @requestFingerprint, NOW(), NOW()
            )
            ON CONFLICT (idempotency_key) DO NOTHING
            """);
        command.Parameters.AddWithValue("id", reservation.PaymentId);
        command.Parameters.AddWithValue("idempotencyKey", key);
        command.Parameters.AddWithValue("provider", reservation.Provider.ToString());
        command.Parameters.AddWithValue("amount", cardCommand.Amount);
        command.Parameters.AddWithValue("currency", cardCommand.Currency.ToUpperInvariant());
        command.Parameters.AddWithValue("installments", cardCommand.Installments);
        command.Parameters.AddWithValue("status", reservation.Status.ToString());
        command.Parameters.AddWithValue("externalAuthorizationId", reservation.ExternalAuthorizationId);
        command.Parameters.AddWithValue("requestFingerprint", requestFingerprint);
        return await command.ExecuteNonQueryAsync(cancellationToken) == 1;
    }

    public async Task SaveAsync(string key, CardPaymentCommand cardCommand, CardPaymentResult result, string requestFingerprint, CancellationToken cancellationToken)
    {
        await using NpgsqlCommand command = dataSource.CreateCommand("""
            UPDATE card_payments
            SET provider = @provider,
                amount = @amount,
                currency = @currency,
                installments = @installments,
                status = @status,
                external_authorization_id = @externalAuthorizationId,
                updated_at = NOW()
            WHERE idempotency_key = @idempotencyKey
              AND request_fingerprint = @requestFingerprint
            """);
        command.Parameters.AddWithValue("provider", result.Provider.ToString());
        command.Parameters.AddWithValue("amount", cardCommand.Amount);
        command.Parameters.AddWithValue("currency", cardCommand.Currency.ToUpperInvariant());
        command.Parameters.AddWithValue("installments", cardCommand.Installments);
        command.Parameters.AddWithValue("status", result.Status.ToString());
        command.Parameters.AddWithValue("externalAuthorizationId", result.ExternalAuthorizationId);
        command.Parameters.AddWithValue("idempotencyKey", key);
        command.Parameters.AddWithValue("requestFingerprint", requestFingerprint);
        await command.ExecuteNonQueryAsync(cancellationToken);
    }
}
