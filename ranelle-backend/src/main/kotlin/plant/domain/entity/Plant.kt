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

    // Plant family
    val family: String? = null,

    // JSON array string of plant origins
    val originJson: String? = null,

    // Care difficulty level
    val careLevel: String? = null,

    // JSON array string of sunlight recommendations
    val sunlightJson: String? = null,

    // Watering recommendation
    val watering: String? = null,

    // Watering interval value
    val wateringBenchmarkValue: String? = null,

    // Watering interval unit
    val wateringBenchmarkUnit: String? = null,

    // Minimum acceptable moisture level
    val moistureMin: Double? = null,

    // Maximum acceptable moisture level
    val moistureMax: Double? = null,

    // Minimum acceptable light level
    val lightMin: Double? = null,

    // Maximum acceptable light level
    val lightMax: Double? = null,

    // Linked sensor/device id
    val deviceId: String? = null,

    // Human-readable plant location
    val location: String? = null,

    // Raw API response JSON
    val rawJson: String? = null,

    // Creation timestamp
    val createdAt: String? = null,

    // Last update timestamp
    val updatedAt: String? = null,
)