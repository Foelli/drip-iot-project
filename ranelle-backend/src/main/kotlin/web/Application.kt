package web

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.coroutines.Dispatchers
import plant.data.database.DatabaseFactory
import plant.data.repository.PostgresRepositoryImpl
import plant.domain.usecase.AddPlantUseCase
import plant.domain.usecase.DeletePlantUseCase
import plant.domain.usecase.GetPlantByIdUseCase
import plant.domain.usecase.GetPlantsUseCase
import plant.domain.usecase.UpdatePlantUseCase
import web.routing.configureRouting

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init(environment.config)

    install(ContentNegotiation) {
        json()
    }

    val repository = PostgresRepositoryImpl()
    val dispatcher = Dispatchers.IO

    configureRouting(
        getPlantsUseCase = GetPlantsUseCase(repository = repository, dispatcher = dispatcher),
        getPlantByIdUseCase = GetPlantByIdUseCase(dispatcher = dispatcher, repository = repository),
        addPlantUseCase = AddPlantUseCase(dispatcher = dispatcher, repository = repository),
        updatePlantUseCase = UpdatePlantUseCase(dispatcher = dispatcher, repository = repository),
        deletePlantUseCase = DeletePlantUseCase(dispatcher = dispatcher, repository = repository),
    )
}
