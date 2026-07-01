package plant.data.database

import org.jetbrains.exposed.v1.core.Table

object PlantsTable : Table("plants") {
    val id = integer("id").autoIncrement()
    val apiId = integer("api_id")
    val commonName = text("common_name").nullable()
    val scientificName = text("scientific_name").nullable()
    val customName = text("custom_name").nullable()
    val thumbnailUrl = text("thumbnail_url").nullable()
    val description = text("description").nullable()
    val temperature = integer("temperature").nullable()
    val moisture = integer("moisture").nullable()
    val airMoisture = integer("air_moisture").nullable()
    val wateringEnabled = bool("watering_enabled").default(true)
    val moistureThreshold = integer("moisture_threshold").default(35)
    val pumpDurationMs = integer("pump_duration_ms").default(2000)
    val waterSettleMs = integer("water_settle_ms").default(20000)

    override val primaryKey = PrimaryKey(id)
}
