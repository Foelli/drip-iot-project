package web.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdatePlantRequest(
    val apiId: Int,
    val commonName: String? = null,
    val scientificName: String? = null,
    val customName: String? = null,
    val thumbnailUrl: String? = null,
    val description: String? = null,
)
