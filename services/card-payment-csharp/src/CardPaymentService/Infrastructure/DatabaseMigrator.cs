using DbUp;
using System.Reflection;

namespace Acme.Payments.CardPaymentService.Infrastructure;

public static class DatabaseMigrator
{
    public static void Migrate(string connectionString)
    {
        EnsureDatabase.For.PostgresqlDatabase(connectionString);

        var result = DeployChanges.To
            .PostgresqlDatabase(connectionString)
            .WithScriptsEmbeddedInAssembly(Assembly.GetExecutingAssembly())
            .LogToConsole()
            .Build()
            .PerformUpgrade();

        if (!result.Successful)
        {
            throw new InvalidOperationException("database migration failed", result.Error);
        }
    }
}
