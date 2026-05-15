package web.routing

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import plant.domain.entity.Plant
import plant.domain.repository.PlantRepository
import plant.domain.usecase.AddPlantUseCase
import plant.domain.usecase.DeletePlantUseCase
import plant.domain.usecase.GetPlantByIdUseCase
import plant.domain.usecase.GetPlantsUseCase
import plant.domain.usecase.UpdatePlantUseCase

class HealthRouteTest {

    @Test
    fun `GET health returns ok`() = testApplication {
        val repository = fakeRepository()

        application {
            configureRouting(
                getPlantsUseCase = GetPlantsUseCase(repository, Dispatchers.Unconfined),
                getPlantByIdUseCase = GetPlantByIdUseCase(Dispatchers.Unconfined, repository),
                addPlantUseCase = AddPlantUseCase(Dispatchers.Unconfined, repository),
                updatePlantUseCase = UpdatePlantUseCase(Dispatchers.Unconfined, repository),
                deletePlantUseCase = DeletePlantUseCase(Dispatchers.Unconfined, repository),
            )
        }

        val response = client.get("/api/v1/health")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("ok", response.bodyAsText())
    }
}

private fun fakeRepository(): PlantRepository =
    object : PlantRepository {
        override suspend fun addPlant(plant: Plant) = Unit
        override suspend fun deletePlant(plantId: Int) = Unit
        override suspend fun updatePlant(plant: Plant) = Unit
        override suspend fun getAllPlants(): List<Plant> = emptyList()
        override suspend fun findById(plantId: Int): Plant? = null
    }
