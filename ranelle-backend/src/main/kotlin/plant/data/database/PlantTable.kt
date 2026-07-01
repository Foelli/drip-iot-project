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
    val idealMoistureMin = integer("ideal_moisture_min").default(35)
    val idealMoistureMax = integer("ideal_moisture_max").default(100)
    val idealTempMin = integer("ideal_temp_min").default(18)
    val idealTempMax = integer("ideal_temp_max").default(30)
    val idealAirMoistureMin = integer("ideal_air_moisture_min").default(40)
    val idealAirMoistureMax = integer("ideal_air_moisture_max").default(70)
    val careNotes = text("care_notes").nullable()

    override val primaryKey = PrimaryKey(id)
}
