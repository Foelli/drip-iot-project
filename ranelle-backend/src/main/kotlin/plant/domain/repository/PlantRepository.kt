package plant.domain.repository

import plant.domain.entity.Plant

interface PlantRepository {
    suspend fun addPlant(plant: Plant)

    suspend fun deletePlant(plantId: Int)

    suspend fun updatePlant(plant: Plant)

    suspend fun getAllPlants(): List<Plant>
}