package com.orchestradashboard.shared.data.api

sealed class OrchestratorApiException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

class OrchestratorUnauthorizedException(
    message: String = "Unauthorized (401)",
) : OrchestratorApiException(message)

class OrchestratorNotFoundException(
    message: String = "Not found (404)",
) : OrchestratorApiException(message)

class OrchestratorNetworkException(
    message: String,
    cause: Throwable? = null,
) : OrchestratorApiException(message, cause)
