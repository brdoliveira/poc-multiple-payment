FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY CardPaymentService.sln .
COPY src ./src
COPY tests ./tests
RUN dotnet publish src/CardPaymentService/CardPaymentService.csproj -c Release -o /app/publish

FROM mcr.microsoft.com/dotnet/aspnet:8.0
WORKDIR /app
COPY --from=build /app/publish .
EXPOSE 8082
ENTRYPOINT ["dotnet", "CardPaymentService.dll"]
