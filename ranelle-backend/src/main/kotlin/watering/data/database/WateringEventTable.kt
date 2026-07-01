package watering.data.database

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp
import plant.data.database.PlantsTable

object WateringEventsTable : Table("watering_events") {
    val id = integer("id").autoIncrement()
    val plantId = integer("plant_id").references(PlantsTable.id)
    val moistureBefore = integer("moisture_before").nullable()
    val pumpDurationMs = integer("pump_duration_ms")
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
