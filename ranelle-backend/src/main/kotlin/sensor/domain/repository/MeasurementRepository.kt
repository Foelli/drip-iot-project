package sensor.domain.repository

import sensor.domain.entity.Measurement

interface MeasurementRepository {
    suspend fun addMeasurement(measurement: Measurement): Measurement
    suspend fun getMeasurementsForPlant(plantId: Int): List<Measurement>
    suspend fun getLatestMeasurementForPlant(plantId: Int): Measurement?
}
