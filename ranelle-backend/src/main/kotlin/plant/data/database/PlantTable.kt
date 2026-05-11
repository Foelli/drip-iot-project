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

    override val primaryKey = PrimaryKey(id)
}