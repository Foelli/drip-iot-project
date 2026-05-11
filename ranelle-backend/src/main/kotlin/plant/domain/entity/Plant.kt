package plant.domain.entity

import kotlinx.serialization.Serializable

@Serializable
data class Plant(

    // Internal unique plant id
    val id: Int? = null,

    // External API unique plant id
    val apiId: Int,

    // Human-readable plant name
    val commonName: String? = null,

    // Scientific species name
    val scientificName: String? = null,

    // Custom user-assigned plant name
    val customName: String? = null,

    // Plant thumbnail/image URL
    val thumbnailUrl: String? = null,

    // Plant description
    val description: String? = null,
)
