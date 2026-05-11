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
                val plant = Plant(
                    id = id,
                    apiId = req.apiId,
                    commonName = req.commonName,
                    scientificName = req.scientificName,
                    customName = req.customName,
                    thumbnailUrl = req.thumbnailUrl,
                    description = req.description,
                )
                updatePlantUseCase(plant)
                call.respond(HttpStatusCode.NoContent)
            }

            // DELETE /plants/{id}
            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid id")

                deletePlantUseCase(id)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
