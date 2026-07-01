package sensor.domain.entity

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Measurement(
    val id: Int? = null,
    val plantId: Int,
    val temperature: Int? = null,
    val soilMoisture: Int? = null,
    val airMoisture: Int? = null,
    val measuredAt: String = Instant.now().toString(),
)
