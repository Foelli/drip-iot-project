package web.dto

import kotlinx.serialization.Serializable

/**
 * Wire contract for `PUT /plants/{id}` — the shape of JSON a client must send
 * when replacing an existing plant.
 *
 * Intentionally distinct from both [plant.domain.entity.Plant] and
 * [CreatePlantRequest]:
 *
 *  - No `id`: the target plant is identified by the URL path parameter, so
 *    accepting `id` in the body would invite ambiguity (which one wins?).
 *
 *  - No `apiId`: a plant's link to the external catalog is set at creation
 *    and treated as immutable. Omitting the field here makes that rule
 *    enforceable by the type system rather than by handler-level checks.
 *
 *  - Fields are nullable so sensor clients can update only the measured plant
 *    state without needing to know descriptive catalog data.
 */
@Serializable
data class UpdatePlantRequest(
    val commonName: String? = null,
    val scientificName: String? = null,
    val customName: String? = null,
    val thumbnailUrl: String? = null,
    val description: String? = null,
    val temperature: Int? = null,
    val moisture: Int? = null,
    val airMoisture: Int? = null,
    val wateringEnabled: Boolean? = null,
    val moistureThreshold: Int? = null,
    val pumpDurationMs: Int? = null,
    val waterSettleMs: Int? = null,
    val idealMoistureMin: Int? = null,
    val idealMoistureMax: Int? = null,
    val idealTempMin: Int? = null,
    val idealTempMax: Int? = null,
    val idealAirMoistureMin: Int? = null,
    val idealAirMoistureMax: Int? = null,
    val careNotes: String? = null,
)
