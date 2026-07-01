package watering.domain.entity

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class WateringEvent(
    val id: Int? = null,
    val plantId: Int,
    val moistureBefore: Int? = null,
    val pumpDurationMs: Int,
    val createdAt: String = Instant.now().toString(),
)
