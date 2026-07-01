package web.dto

import kotlinx.serialization.Serializable

@Serializable
data class CareInfoResponse(
    val idealMoistureMin: Int,
    val idealMoistureMax: Int,
    val idealTempMin: Int,
    val idealTempMax: Int,
    val idealAirMoistureMin: Int,
    val idealAirMoistureMax: Int,
    val careNotes: String,
)

@Serializable
data class GenerateCareInfoRequest(
    val commonName: String,
    val scientificName: String? = null,
    val notes: String? = null,
)
