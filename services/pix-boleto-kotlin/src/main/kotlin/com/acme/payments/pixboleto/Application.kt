package com.acme.payments.pixboleto

import com.acme.payments.pixboleto.provider.AsaasAdapter
import com.acme.payments.pixboleto.provider.IuguAdapter
import com.acme.payments.pixboleto.provider.MercadoPagoAdapter
import com.acme.payments.pixboleto.provider.PagBankAdapter
import com.acme.payments.pixboleto.provider.PaymentProviderAdapter
import com.acme.payments.pixboleto.provider.ProviderRouter
import com.acme.payments.pixboleto.provider.StripeAdapter
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class Application {
    @Bean
    fun adapters(): List<PaymentProviderAdapter> = listOf(
        AsaasAdapter(),
        MercadoPagoAdapter(),
        PagBankAdapter(),
        IuguAdapter(),
        StripeAdapter(),
    )

    @Bean
    fun providerRouter(adapters: List<PaymentProviderAdapter>) = ProviderRouter(adapters)
}

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
