package com.acme.payments.pixboleto.infrastructure

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class CorrelationIdFilter : OncePerRequestFilter() {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val startedAt = System.nanoTime()
        val correlationId = request.getHeader(HEADER)?.takeIf(::isSafeCorrelationId)
            ?: UUID.randomUUID().toString()

        MDC.put(MDC_KEY, correlationId)
        response.setHeader(HEADER, correlationId)
        try {
            filterChain.doFilter(request, response)
        } finally {
            logger.info("http_outcome service=pix-boleto method={} route={} status={} duration_ms={} correlation_id={}", request.method, request.requestURI, response.status, (System.nanoTime() - startedAt) / 1_000_000, correlationId)
            MDC.remove(MDC_KEY)
        }
    }

    private fun isSafeCorrelationId(value: String?): Boolean =
        value != null && value.length <= 128 && value.matches(Regex("[A-Za-z0-9._:-]+"))

    companion object {
        const val HEADER = "X-Correlation-Id"
        private const val MDC_KEY = "correlationId"
    }
}
