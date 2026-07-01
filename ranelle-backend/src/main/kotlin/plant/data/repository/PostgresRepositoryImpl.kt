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
import java.util.NoSuchElementException

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
                it[temperature] = plant.temperature
                it[moisture] = plant.moisture
                it[airMoisture] = plant.airMoisture
                it[wateringEnabled] = plant.wateringEnabled
                it[moistureThreshold] = plant.moistureThreshold
                it[pumpDurationMs] = plant.pumpDurationMs
                it[waterSettleMs] = plant.waterSettleMs
                it[idealMoistureMin] = plant.idealMoistureMin
                it[idealMoistureMax] = plant.idealMoistureMax
                it[idealTempMin] = plant.idealTempMin
                it[idealTempMax] = plant.idealTempMax
                it[idealAirMoistureMin] = plant.idealAirMoistureMin
                it[idealAirMoistureMax] = plant.idealAirMoistureMax
                it[careNotes] = plant.careNotes
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

        // `apiId` is intentionally omitted: a plant's link to the external
        // catalog is set at creation and treated as immutable. Excluding it
        // here mirrors the omission on `UpdatePlantRequest` as defense in
        // depth — even if a caller passes a `Plant` with a different apiId,
        // the column will not be touched.
        val updatedRows = transaction {
            PlantsTable.update({ PlantsTable.id eq id }) {
                it[commonName] = plant.commonName
                it[scientificName] = plant.scientificName
                it[customName] = plant.customName
                it[thumbnailUrl] = plant.thumbnailUrl
                it[description] = plant.description
                it[temperature] = plant.temperature
                it[moisture] = plant.moisture
                it[airMoisture] = plant.airMoisture
                it[wateringEnabled] = plant.wateringEnabled
                it[moistureThreshold] = plant.moistureThreshold
                it[pumpDurationMs] = plant.pumpDurationMs
                it[waterSettleMs] = plant.waterSettleMs
                it[idealMoistureMin] = plant.idealMoistureMin
                it[idealMoistureMax] = plant.idealMoistureMax
                it[idealTempMin] = plant.idealTempMin
                it[idealTempMax] = plant.idealTempMax
                it[idealAirMoistureMin] = plant.idealAirMoistureMin
                it[idealAirMoistureMax] = plant.idealAirMoistureMax
                it[careNotes] = plant.careNotes
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

    override suspend fun findById(plantId: Int): Plant? {
        return transaction {
            PlantsTable
                .selectAll()
                .where { PlantsTable.id eq plantId }
                .map(::rowToPlant)
                .singleOrNull()
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
            temperature = row[PlantsTable.temperature],
            moisture = row[PlantsTable.moisture],
            airMoisture = row[PlantsTable.airMoisture],
            wateringEnabled = row[PlantsTable.wateringEnabled],
            moistureThreshold = row[PlantsTable.moistureThreshold],
            pumpDurationMs = row[PlantsTable.pumpDurationMs],
            waterSettleMs = row[PlantsTable.waterSettleMs],
            idealMoistureMin = row[PlantsTable.idealMoistureMin],
            idealMoistureMax = row[PlantsTable.idealMoistureMax],
            idealTempMin = row[PlantsTable.idealTempMin],
            idealTempMax = row[PlantsTable.idealTempMax],
            idealAirMoistureMin = row[PlantsTable.idealAirMoistureMin],
            idealAirMoistureMax = row[PlantsTable.idealAirMoistureMax],
            careNotes = row[PlantsTable.careNotes],
        )
    }
}
