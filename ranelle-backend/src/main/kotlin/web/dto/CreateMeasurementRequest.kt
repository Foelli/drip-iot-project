package web.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateMeasurementRequest(
    val temperature: Int? = null,
    val soilMoisture: Int? = null,
    val airMoisture: Int? = null,
)
