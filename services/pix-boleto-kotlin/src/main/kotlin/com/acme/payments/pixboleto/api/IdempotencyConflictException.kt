package com.acme.payments.pixboleto.api

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class IdempotencyConflictException(key: String) :
    RuntimeException("idempotency key was already used with a different request: $key")
