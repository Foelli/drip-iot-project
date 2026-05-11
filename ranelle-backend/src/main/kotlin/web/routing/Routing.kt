package web.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import plant.domain.entity.Plant
import plant.domain.usecase.AddPlantUseCase
import plant.domain.usecase.DeletePlantUseCase
import plant.domain.usecase.GetPlantsUseCase
import plant.domain.usecase.UpdatePlantUseCase
import web.dto.CreatePlantRequest
import web.dto.UpdatePlantRequest

fun Application.configureRouting(
    getPlantsUseCase: GetPlantsUseCase,
    addPlantUseCase: AddPlantUseCase,
    updatePlantUseCase: UpdatePlantUseCase,
    deletePlantUseCase: DeletePlantUseCase
) {
    routing {
        get("/") {
            call.respondText("This is v1.0.0 of the YAPS")
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

                val req = call.receive<UpdatePlantRequest>()
                val commonName = req.commonName
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        "PUT /plants/{id} requires all fields; missing commonName"
                    )
                val scientificName = req.scientificName
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        "PUT /plants/{id} requires all fields; missing scientificName"
                    )
                val customName = req.customName
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        "PUT /plants/{id} requires all fields; missing customName"
                    )
                val thumbnailUrl = req.thumbnailUrl
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        "PUT /plants/{id} requires all fields; missing thumbnailUrl"
                    )
                val description = req.description
                    ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        "PUT /plants/{id} requires all fields; missing description"
                    )
                val plant = Plant(
                    id = id,
                    apiId = req.apiId,
                    commonName = commonName,
                    scientificName = scientificName,
                    customName = customName,
                    thumbnailUrl = thumbnailUrl,
                    description = description,
                )
                try {
                    updatePlantUseCase(plant)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: NoSuchElementException) {
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
