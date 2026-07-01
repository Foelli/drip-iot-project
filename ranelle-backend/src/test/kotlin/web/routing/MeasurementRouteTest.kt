package web.routing

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import plant.domain.entity.Plant
import plant.domain.repository.PlantRepository
import plant.domain.usecase.AddPlantUseCase
import plant.domain.usecase.DeletePlantUseCase
import plant.domain.usecase.GetPlantByIdUseCase
import plant.domain.usecase.GetPlantsUseCase
import plant.domain.usecase.UpdatePlantUseCase
import sensor.domain.entity.Measurement
import sensor.domain.repository.MeasurementRepository
import sensor.domain.usecase.AddMeasurementUseCase
import sensor.domain.usecase.GetLatestMeasurementUseCase
import sensor.domain.usecase.GetMeasurementsForPlantUseCase

class MeasurementRouteTest {
    @Test
    fun `POST plant measurement stores historical measurement`() = testApplication {
        val measurementRepository = InMemoryMeasurementRepository()

        application {
            install(ContentNegotiation) {
                json()
            }
            configureRouting(
                getPlantsUseCase = GetPlantsUseCase(existingPlantRepository(), Dispatchers.Unconfined),
                getPlantByIdUseCase = GetPlantByIdUseCase(Dispatchers.Unconfined, existingPlantRepository()),
                addPlantUseCase = AddPlantUseCase(Dispatchers.Unconfined, existingPlantRepository()),
                updatePlantUseCase = UpdatePlantUseCase(Dispatchers.Unconfined, existingPlantRepository()),
                deletePlantUseCase = DeletePlantUseCase(Dispatchers.Unconfined, existingPlantRepository()),
                addMeasurementUseCase = AddMeasurementUseCase(Dispatchers.Unconfined, measurementRepository),
                getLatestMeasurementUseCase = GetLatestMeasurementUseCase(
                    Dispatchers.Unconfined,
                    measurementRepository,
                ),
                getMeasurementsForPlantUseCase = GetMeasurementsForPlantUseCase(
                    Dispatchers.Unconfined,
                    measurementRepository,
                ),
            )
        }

        val response = client.post("/api/v1/plants/1/measurements") {
            contentType(ContentType.Application.Json)
            setBody("""{"temperature":31,"soilMoisture":1,"airMoisture":53}""")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertContains(response.bodyAsText(), """"soilMoisture":1""")
        assertEquals(1, measurementRepository.measurements.size)
    }

    @Test
    fun `GET latest plant measurement returns newest measurement`() = testApplication {
        val measurementRepository = InMemoryMeasurementRepository()
        measurementRepository.addMeasurement(
            Measurement(
                plantId = 1,
                temperature = 25,
                soilMoisture = 10,
                airMoisture = 40,
                measuredAt = "2026-06-29T10:00:00Z",
            )
        )
        measurementRepository.addMeasurement(
            Measurement(
                plantId = 1,
                temperature = 31,
                soilMoisture = 1,
                airMoisture = 53,
                measuredAt = "2026-06-29T10:30:00Z",
            )
        )

        application {
            install(ContentNegotiation) {
                json()
            }
            configureRouting(
                getPlantsUseCase = GetPlantsUseCase(existingPlantRepository(), Dispatchers.Unconfined),
                getPlantByIdUseCase = GetPlantByIdUseCase(Dispatchers.Unconfined, existingPlantRepository()),
                addPlantUseCase = AddPlantUseCase(Dispatchers.Unconfined, existingPlantRepository()),
                updatePlantUseCase = UpdatePlantUseCase(Dispatchers.Unconfined, existingPlantRepository()),
                deletePlantUseCase = DeletePlantUseCase(Dispatchers.Unconfined, existingPlantRepository()),
                addMeasurementUseCase = AddMeasurementUseCase(Dispatchers.Unconfined, measurementRepository),
                getLatestMeasurementUseCase = GetLatestMeasurementUseCase(
                    Dispatchers.Unconfined,
                    measurementRepository,
                ),
                getMeasurementsForPlantUseCase = GetMeasurementsForPlantUseCase(
                    Dispatchers.Unconfined,
                    measurementRepository,
                ),
            )
        }

        val response = client.get("/api/v1/plants/1/measurements/latest")

        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), """"temperature":31""")
        assertContains(response.bodyAsText(), """"airMoisture":53""")
    }
}

private fun existingPlantRepository(): PlantRepository =
    object : PlantRepository {
        override suspend fun addPlant(plant: Plant) = Unit
        override suspend fun deletePlant(plantId: Int) = Unit
        override suspend fun updatePlant(plant: Plant) = Unit
        override suspend fun getAllPlants(): List<Plant> = listOf(existingPlant)
        override suspend fun findById(plantId: Int): Plant? = existingPlant.takeIf { plantId == it.id }
    }

private val existingPlant = Plant(id = 1, apiId = 123, commonName = "Basil")

private class InMemoryMeasurementRepository : MeasurementRepository {
    val measurements = mutableListOf<Measurement>()

    override suspend fun addMeasurement(measurement: Measurement): Measurement {
        val created = measurement.copy(id = measurements.size + 1)
        measurements += created
        return created
    }

    override suspend fun getMeasurementsForPlant(plantId: Int): List<Measurement> =
        measurements.filter { it.plantId == plantId }.sortedBy { it.measuredAt }

    override suspend fun getLatestMeasurementForPlant(plantId: Int): Measurement? =
        measurements.filter { it.plantId == plantId }.maxByOrNull { it.measuredAt }
}
