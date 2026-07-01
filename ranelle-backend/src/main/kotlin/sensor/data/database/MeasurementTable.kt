package sensor.data.database

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp
import plant.data.database.PlantsTable

object MeasurementsTable : Table("plant_measurements") {
    val id = integer("id").autoIncrement()
    val plantId = integer("plant_id").references(PlantsTable.id)
    val temperature = integer("temperature").nullable()
    val soilMoisture = integer("soil_moisture").nullable()
    val airMoisture = integer("air_moisture").nullable()
    val measuredAt = timestamp("measured_at")

    override val primaryKey = PrimaryKey(id)
}
