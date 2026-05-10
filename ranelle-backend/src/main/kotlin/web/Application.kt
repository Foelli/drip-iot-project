package web

import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import plant.data.database.DatabaseFactory
import web.routing.configureRouting

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init(environment.config)
    configureRouting()
}
