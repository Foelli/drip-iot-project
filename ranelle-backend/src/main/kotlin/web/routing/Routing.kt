package web.routing

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import plant.domain.entity.Plant
import plant.domain.usecase.AddPlantUseCase
import plant.domain.usecase.DeletePlantUseCase
import plant.domain.usecase.GetPlantByIdUseCase
import plant.domain.usecase.GetPlantsUseCase
import plant.domain.usecase.UpdatePlantUseCase
import sensor.domain.entity.Measurement
import sensor.domain.usecase.AddMeasurementUseCase
import sensor.domain.usecase.GetLatestMeasurementUseCase
import sensor.domain.usecase.GetMeasurementsForPlantUseCase
import web.dto.CreateMeasurementRequest
import web.dto.CreatePlantRequest
import web.dto.CreateWateringEventRequest
import web.dto.UpdatePlantRequest
import web.dto.WateringConfigResponse
import watering.data.repository.PostgresWateringEventRepository
import watering.domain.entity.WateringEvent

fun Application.configureRouting(
    getPlantsUseCase: GetPlantsUseCase,
    getPlantByIdUseCase: GetPlantByIdUseCase,
    addPlantUseCase: AddPlantUseCase,
    updatePlantUseCase: UpdatePlantUseCase,
    deletePlantUseCase: DeletePlantUseCase,
    addMeasurementUseCase: AddMeasurementUseCase,
    getLatestMeasurementUseCase: GetLatestMeasurementUseCase,
    getMeasurementsForPlantUseCase: GetMeasurementsForPlantUseCase,
    wateringEventRepository: PostgresWateringEventRepository = PostgresWateringEventRepository(),
) {
    routing {
        route("/api/v1") {
            get {
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
                        temperature = req.temperature,
                        moisture = req.moisture,
                        airMoisture = req.airMoisture,
                        wateringEnabled = req.wateringEnabled,
                        moistureThreshold = req.moistureThreshold,
                        pumpDurationMs = req.pumpDurationMs,
                        waterSettleMs = req.waterSettleMs,

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

                    val req = call.receive<UpdatePlantRequest>()
                    val updated = existing.copy(
                        commonName = req.commonName ?: existing.commonName,
                        scientificName = req.scientificName ?: existing.scientificName,
                        customName = req.customName ?: existing.customName,
                        thumbnailUrl = req.thumbnailUrl ?: existing.thumbnailUrl,
                        description = req.description ?: existing.description,
                        temperature = req.temperature ?: existing.temperature,
                        moisture = req.moisture ?: existing.moisture,
                        airMoisture = req.airMoisture ?: existing.airMoisture,
                        wateringEnabled = req.wateringEnabled ?: existing.wateringEnabled,
                        moistureThreshold = req.moistureThreshold ?: existing.moistureThreshold,
                        pumpDurationMs = req.pumpDurationMs ?: existing.pumpDurationMs,
                        waterSettleMs = req.waterSettleMs ?: existing.waterSettleMs,
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

                get ("/{id}") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid id")

                        val plant = getPlantByIdUseCase(id)
                        call.respond(plant as Plant)

                }

                get("/{id}/watering-config") {
                    val id = call.parameters["id"]?.toIntOrNull()
                        ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid id")

                    val plant = getPlantByIdUseCase(id)
                        ?: return@get call.respond(
                            HttpStatusCode.NotFound,
                            "Plant with id $id was not found"
                        )

                    call.respond(
                        WateringConfigResponse(
                            enabled = plant.wateringEnabled,
                            moistureThreshold = plant.moistureThreshold,
                            pumpDurationMs = plant.pumpDurationMs,
                            waterSettleMs = plant.waterSettleMs,
                        )
                    )
                }

                route("/{id}/watering-events") {
                    post {
                        val plantId = call.parameters["id"]?.toIntOrNull()
                            ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid id")

                        getPlantByIdUseCase(plantId)
                            ?: return@post call.respond(
                                HttpStatusCode.NotFound,
                                "Plant with id $plantId was not found"
                            )

                        val req = call.receive<CreateWateringEventRequest>()
                        val event = wateringEventRepository.add(
                            WateringEvent(
                                plantId = plantId,
                                moistureBefore = req.moistureBefore,
                                pumpDurationMs = req.pumpDurationMs,
                            )
                        )

                        call.respond(HttpStatusCode.Created, event)
                    }

                    get {
                        val plantId = call.parameters["id"]?.toIntOrNull()
                            ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid id")

                        getPlantByIdUseCase(plantId)
                            ?: return@get call.respond(
                                HttpStatusCode.NotFound,
                                "Plant with id $plantId was not found"
                            )

                        call.respond(wateringEventRepository.getForPlant(plantId))
                    }
                }

                route("/{id}/measurements") {
                    post {
                        val plantId = call.parameters["id"]?.toIntOrNull()
                            ?: return@post call.respond(HttpStatusCode.BadRequest, "Invalid id")

                        getPlantByIdUseCase(plantId)
                            ?: return@post call.respond(
                                HttpStatusCode.NotFound,
                                "Plant with id $plantId was not found"
                            )

                        val req = call.receive<CreateMeasurementRequest>()
                        val measurement = addMeasurementUseCase(
                            Measurement(
                                plantId = plantId,
                                temperature = req.temperature,
                                soilMoisture = req.soilMoisture,
                                airMoisture = req.airMoisture,
                            )
                        )

                        call.respond(HttpStatusCode.Created, measurement)
                    }

                    get {
                        val plantId = call.parameters["id"]?.toIntOrNull()
                            ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid id")

                        getPlantByIdUseCase(plantId)
                            ?: return@get call.respond(
                                HttpStatusCode.NotFound,
                                "Plant with id $plantId was not found"
                            )

                        call.respond(getMeasurementsForPlantUseCase(plantId))
                    }

                    get("/latest") {
                        val plantId = call.parameters["id"]?.toIntOrNull()
                            ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid id")

                        getPlantByIdUseCase(plantId)
                            ?: return@get call.respond(
                                HttpStatusCode.NotFound,
                                "Plant with id $plantId was not found"
                            )

                        val measurement = getLatestMeasurementUseCase(plantId)
                            ?: return@get call.respond(
                                HttpStatusCode.NotFound,
                                "No measurements found for plant with id $plantId"
                            )

                        call.respond(measurement)
                    }
                }
            }
        }
    }
}
