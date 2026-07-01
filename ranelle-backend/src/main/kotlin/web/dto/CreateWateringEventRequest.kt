package web.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateWateringEventRequest(
    val moistureBefore: Int? = null,
    val pumpDurationMs: Int,
)

@Serializable
data class WateringConfigResponse(
    val enabled: Boolean,
    val moistureThreshold: Int,
    val pumpDurationMs: Int,
    val waterSettleMs: Int,
)
