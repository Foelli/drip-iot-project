package web.dto

import kotlinx.serialization.Serializable

/**
 * Wire contract for `POST /plants` — the shape of JSON a client may send when
 * creating a new plant.
 *
 * Intentionally distinct from [plant.domain.entity.Plant]:
 *  - No `id`: the primary key is server-assigned, so the client must never
 *    supply it.
 *  - No place for future internal fields (e.g. `ownerId`, `createdAt`,
 *    `isDeleted`). When those land on the domain entity they stay off this
 *    DTO, which prevents clients from setting them via the API
 *    (mass-assignment safety by type, not by discipline).
 *
 * Field rules:
 *  - [apiId] is required: every newly created plant must be linked to the
 *    external plant catalog. (If user-defined plants without a catalog entry
 *    are introduced later, this can be relaxed to nullable.)
 *  - All descriptive fields are optional so the client can create a minimal
 *    record and enrich it later (e.g. before species lookup finishes).
 */
@Serializable
data class CreatePlantRequest(
    val apiId: Int,
    val commonName: String? = null,
    val scientificName: String? = null,
    val customName: String? = null,
    val thumbnailUrl: String? = null,
    val description: String? = null,
    val temperature: Int? = null,
    val moisture: Int? = null,
    val airMoisture: Int? = null,
    val wateringEnabled: Boolean = true,
    val moistureThreshold: Int = 35,
    val pumpDurationMs: Int = 2000,
    val waterSettleMs: Int = 20000,
)
