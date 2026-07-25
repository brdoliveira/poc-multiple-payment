package com.acme.payments.pixboleto.infrastructure

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class ApiKeyAuthenticationFilter(
    @Value("\${payments.security.api-key:}") private val apiKey: String,
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return apiKey.isBlank() || request.requestURI.startsWith("/actuator")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (request.getHeader(API_KEY_HEADER) != apiKey) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "invalid internal api key")
            return
        }

        filterChain.doFilter(request, response)
    }

    private companion object {
        const val API_KEY_HEADER = "X-Internal-Api-Key"
    }
}
