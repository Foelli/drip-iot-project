package sensor.data.repository

import java.time.Instant
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import sensor.data.database.MeasurementsTable
import sensor.domain.entity.Measurement
import sensor.domain.repository.MeasurementRepository

class PostgresMeasurementRepositoryImpl : MeasurementRepository {
    override suspend fun addMeasurement(measurement: Measurement): Measurement {
        val measuredAt = Instant.parse(measurement.measuredAt)

        return transaction {
            val inserted = MeasurementsTable.insert {
                it[plantId] = measurement.plantId
                it[temperature] = measurement.temperature
                it[soilMoisture] = measurement.soilMoisture
                it[airMoisture] = measurement.airMoisture
                it[MeasurementsTable.measuredAt] = measuredAt
            }

            measurement.copy(
                id = inserted[MeasurementsTable.id],
                measuredAt = inserted[MeasurementsTable.measuredAt].toString(),
            )
        }
    }

    override suspend fun getMeasurementsForPlant(plantId: Int): List<Measurement> =
        transaction {
            MeasurementsTable
                .selectAll()
                .where { MeasurementsTable.plantId eq plantId }
                .orderBy(MeasurementsTable.measuredAt to SortOrder.ASC)
                .map(::rowToMeasurement)
        }

    override suspend fun getLatestMeasurementForPlant(plantId: Int): Measurement? =
        transaction {
            MeasurementsTable
                .selectAll()
                .where { MeasurementsTable.plantId eq plantId }
                .orderBy(MeasurementsTable.measuredAt to SortOrder.DESC)
                .limit(1)
                .map(::rowToMeasurement)
                .singleOrNull()
        }

    private fun rowToMeasurement(row: ResultRow): Measurement =
        Measurement(
            id = row[MeasurementsTable.id],
            plantId = row[MeasurementsTable.plantId],
            temperature = row[MeasurementsTable.temperature],
            soilMoisture = row[MeasurementsTable.soilMoisture],
            airMoisture = row[MeasurementsTable.airMoisture],
            measuredAt = row[MeasurementsTable.measuredAt].toString(),
        )
}
