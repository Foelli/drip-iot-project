package plant.data.repository

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import plant.data.database.PlantsTable
import plant.domain.entity.Plant
import plant.domain.repository.PlantRepository

class PostgresRepositoryImpl: PlantRepository {
    override suspend fun addPlant(plant: Plant) {
        transaction {
            PlantsTable.insert {
                it[apiId] = plant.apiId
                it[commonName] = plant.commonName
                it[scientificName] = plant.scientificName
                it[customName] = plant.customName
                it[thumbnailUrl] = plant.thumbnailUrl
                it[description] = plant.description
            }
        }
    }

    override suspend fun deletePlant(plantId: Int) {
        val deletedRows = transaction {
            PlantsTable.deleteWhere {
                PlantsTable.id eq plantId
            }
        }
        if (deletedRows == 0) {
            throw NoSuchElementException("Plant with id $plantId was not found")
        }
    }

    override suspend fun updatePlant(plant: Plant) {
        val id = plant.id ?: error("Can't update plant without id")

        val updatedRows = transaction {
            PlantsTable.update({ PlantsTable.id eq id }) {
                it[apiId] = plant.apiId
                it[commonName] = plant.commonName
                it[scientificName] = plant.scientificName
                it[customName] = plant.customName
                it[thumbnailUrl] = plant.thumbnailUrl
                it[description] = plant.description
            }
        }
        if (updatedRows == 0) {
            throw NoSuchElementException("Plant with id $id was not found")
        }
    }

    override suspend fun getAllPlants(): List<Plant> {
        return transaction {
            PlantsTable
                .selectAll()
                .map(::rowToPlant)
        }
    }

    private fun rowToPlant(row: ResultRow): Plant {
        return Plant(
            id = row[PlantsTable.id],
            apiId = row[PlantsTable.apiId],
            commonName = row[PlantsTable.commonName],
            scientificName = row[PlantsTable.scientificName],
            customName = row[PlantsTable.customName],
            thumbnailUrl = row[PlantsTable.thumbnailUrl],
            description = row[PlantsTable.description],
        )
    }
}
