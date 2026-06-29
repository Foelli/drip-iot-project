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
 *  - All remaining fields are **non-nullable** with no defaults. This encodes
 *    PUT's "full replace" semantics directly in the type: kotlinx.serialization
 *    will reject any request that omits a field with `MissingFieldException`,
 *    so the handler no longer needs per-field null guards. (For partial
 *    updates, introduce a separate `PatchPlantRequest` with nullable fields.)
 */
@Serializable
data class UpdatePlantRequest(
    val commonName: String,
    val scientificName: String,
    val customName: String,
    val thumbnailUrl: String,
    val description: String,
    val temperature: Int,
    val moisture: Int,
    val light: Int,
)
