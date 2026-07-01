package watering.data.repository

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import watering.data.database.WateringEventsTable
import watering.domain.entity.WateringEvent
import java.time.Instant

class PostgresWateringEventRepository {
    fun add(event: WateringEvent): WateringEvent =
        transaction {
            val createdAt = Instant.parse(event.createdAt)
            val inserted = WateringEventsTable.insert {
                it[plantId] = event.plantId
                it[moistureBefore] = event.moistureBefore
                it[pumpDurationMs] = event.pumpDurationMs
                it[WateringEventsTable.createdAt] = createdAt
            }

            event.copy(
                id = inserted[WateringEventsTable.id],
                createdAt = inserted[WateringEventsTable.createdAt].toString(),
            )
        }

    fun getForPlant(plantId: Int): List<WateringEvent> =
        transaction {
            WateringEventsTable
                .selectAll()
                .where { WateringEventsTable.plantId eq plantId }
                .orderBy(WateringEventsTable.createdAt to SortOrder.DESC)
                .map(::rowToEvent)
        }

    private fun rowToEvent(row: ResultRow): WateringEvent =
        WateringEvent(
            id = row[WateringEventsTable.id],
            plantId = row[WateringEventsTable.plantId],
            moistureBefore = row[WateringEventsTable.moistureBefore],
            pumpDurationMs = row[WateringEventsTable.pumpDurationMs],
            createdAt = row[WateringEventsTable.createdAt].toString(),
        )
}
