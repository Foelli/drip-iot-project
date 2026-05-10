package web.routing

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import plant.domain.entity.Plant

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("This is v1.0.0 of the YAPS")
        }
    }
}