package web.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import plant.domain.entity.Plant
import plant.domain.usecase.AddPlantUseCase
import plant.domain.usecase.DeletePlantUseCase
import plant.domain.usecase.GetPlantByIdUseCase
import plant.domain.usecase.GetPlantsUseCase
import plant.domain.usecase.UpdatePlantUseCase
import web.dto.CreatePlantRequest
import web.dto.UpdatePlantRequest

@Serializable
data class MessageResponse(val message: String)

fun Application.configureRouting(
    getPlantsUseCase: GetPlantsUseCase,
    getPlantByIdUseCase: GetPlantByIdUseCase,
    addPlantUseCase: AddPlantUseCase,
    updatePlantUseCase: UpdatePlantUseCase,
    deletePlantUseCase: DeletePlantUseCase
) {
    routing {
        get("/api/v1") {
            call.respondText("This is v1.0.0 of the YAPS")
        }

        // Simple health endpoint
        get("/health") {
            call.respond(HttpStatusCode.OK, "ok")
        }

        route("/plants") {

            // GET /plants
            get {
                val plants = getPlantsUseCase(Unit)
                call.respond(plants)
            }

            // POST /plants
            post {
                val req = call.receive<CreatePlantRequest>()
                val plant = Plant(
                    id = null,
                    apiId = req.apiId,
                    commonName = req.commonName,
                    scientificName = req.scientificName,
                    customName = req.customName,
                    thumbnailUrl = req.thumbnailUrl,
                    description = req.description,
                )
                addPlantUseCase(plant)
                call.respond(HttpStatusCode.Created)
            }

            // PUT /plants/{id}
            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid id")

                // Fetch first so we can preserve immutable fields (apiId) and
                // return a clean 404 before touching the request body.
                val existing = getPlantByIdUseCase(id)
                    ?: return@put call.respond(
                        HttpStatusCode.NotFound,
                        "Plant with id $id was not found"
                    )

                // Required fields are non-nullable on `UpdatePlantRequest`, so
                // kotlinx.serialization rejects incomplete payloads before
                // this line — no per-field null guards needed.
                val req = call.receive<UpdatePlantRequest>()
                val updated = existing.copy(
                    commonName = req.commonName,
                    scientificName = req.scientificName,
                    customName = req.customName,
                    thumbnailUrl = req.thumbnailUrl,
                    description = req.description,
                )
                try {
                    updatePlantUseCase(updated)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: NoSuchElementException) {
                    // Race: plant deleted between fetch and update.
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Not found")
                }
            }

            // DELETE /plants/{id}
            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid id")

                try {
                    deletePlantUseCase(id)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: NoSuchElementException) {
                    call.respond(HttpStatusCode.NotFound, e.message ?: "Not found")
                }
            }
        }
    }
}
