using Microsoft.Extensions.Configuration;
using MongoDB.Driver;

namespace Acme.Payments.CardPaymentService.Webhooks;

public sealed class MongoWebhookPayloadStore : IWebhookPayloadStore
{
    private readonly IMongoCollection<ProviderWebhookPayload> collection;

    public MongoWebhookPayloadStore(IConfiguration configuration)
    {
        string connectionString = configuration.GetConnectionString("Mongo")
                                  ?? "mongodb://localhost:27017";
        string databaseName = configuration["Mongo:Database"] ?? "payment_operational";
        var client = new MongoClient(connectionString);
        collection = client
            .GetDatabase(databaseName)
            .GetCollection<ProviderWebhookPayload>("webhook_payloads");
    }

    public async Task SaveAsync(ProviderWebhookPayload payload, CancellationToken cancellationToken)
    {
        var filter = Builders<ProviderWebhookPayload>.Filter.And(
            Builders<ProviderWebhookPayload>.Filter.Eq(item => item.Provider, payload.Provider),
            Builders<ProviderWebhookPayload>.Filter.Eq(item => item.ExternalEventId, payload.ExternalEventId)
        );

        await collection.ReplaceOneAsync(
            filter,
            payload,
            new ReplaceOptions { IsUpsert = true },
            cancellationToken
        );
    }
}
