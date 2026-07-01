package plant.data.database

import io.ktor.server.config.ApplicationConfig
import java.nio.file.Files
import java.nio.file.Path
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.Database
import sensor.data.database.MeasurementsTable
import watering.data.database.WateringEventsTable

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val dotEnv = loadDotEnv()

        val dbHost = databaseConfigValue(config, "host", "POSTGRES_HOST", dotEnv)
            ?: "localhost"

        val dbPort = databaseConfigValue(config, "port", "POSTGRES_PORT", dotEnv)
            ?: "5432"

        val dbName = databaseConfigValue(config, "name", "POSTGRES_DB", dotEnv)
            ?: "plantDB"

        val dbUser = databaseConfigValue(config, "user", "POSTGRES_USER", dotEnv)
            ?: "plant_user"

        val dbPassword = databaseConfigValue(config, "password", "POSTGRES_PASSWORD", dotEnv)
            ?: "plant_password"

        Database.connect(
            url = "jdbc:postgresql://$dbHost:$dbPort/$dbName",
            driver = "org.postgresql.Driver",
            user = dbUser,
            password = dbPassword
        )

        // Minimal startup schema init for local/dev. Consider Flyway/Liquibase later.
        transaction {
            SchemaUtils.createMissingTablesAndColumns(PlantsTable, MeasurementsTable, WateringEventsTable)
        }
    }

    private fun databaseConfigValue(
        config: ApplicationConfig,
        key: String,
        envKey: String,
        dotEnv: Map<String, String>
    ): String? =
        config.propertyOrNull("database.$key")?.getString()
            ?: System.getenv(envKey)
            ?: dotEnv[envKey]

    private fun loadDotEnv(): Map<String, String> {
        val dotEnvPath = Path.of(".env")
        if (!Files.isRegularFile(dotEnvPath)) return emptyMap()

        return Files.readAllLines(dotEnvPath)
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .mapNotNull { line ->
                val separatorIndex = line.indexOf('=')
                if (separatorIndex <= 0) return@mapNotNull null

                val key = line.substring(0, separatorIndex).trim()
                val value = line.substring(separatorIndex + 1).trim().trim('"', '\'')
                key to value
            }
            .toMap()
    }
}
