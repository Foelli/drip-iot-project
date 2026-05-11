package plant.data.database

import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.Database

object DatabaseFactory {
    fun init(config: ApplicationConfig) {
        val dbHost = config.propertyOrNull("database.host")?.getString()
            ?: System.getenv("POSTGRES_HOST")
            ?: "localhost"

        val dbPort = config.propertyOrNull("database.port")?.getString()
            ?: System.getenv("POSTGRES_PORT")
            ?: "5432"

        val dbName = config.propertyOrNull("database.name")?.getString()
            ?: System.getenv("POSTGRES_DB")
            ?: "ranelle"

        val dbUser = config.propertyOrNull("database.user")?.getString()
            ?: System.getenv("POSTGRES_USER")
            ?: "ranelle_user"

        val dbPassword = config.propertyOrNull("database.password")?.getString()
            ?: System.getenv("POSTGRES_PASSWORD")
            ?: "ranelle_password"

        Database.connect(
            url = "jdbc:postgresql://$dbHost:$dbPort/$dbName",
            driver = "org.postgresql.Driver",
            user = dbUser,
            password = dbPassword
        )

        // Minimal startup schema init for local/dev. Consider Flyway/Liquibase later.
        transaction {
            SchemaUtils.createMissingTablesAndColumns(PlantsTable)
        }
    }
}
