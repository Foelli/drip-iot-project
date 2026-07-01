package web

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import kotlinx.coroutines.Dispatchers
import plant.data.database.DatabaseFactory
import plant.data.repository.PostgresRepositoryImpl
import plant.domain.usecase.AddPlantUseCase
import plant.domain.usecase.DeletePlantUseCase
import plant.domain.usecase.GetPlantByIdUseCase
import plant.domain.usecase.GetPlantsUseCase
import plant.domain.usecase.UpdatePlantUseCase
import sensor.data.repository.PostgresMeasurementRepositoryImpl
import sensor.domain.usecase.AddMeasurementUseCase
import sensor.domain.usecase.GetLatestMeasurementUseCase
import sensor.domain.usecase.GetMeasurementsForPlantUseCase
import watering.data.repository.PostgresWateringEventRepository
import web.routing.configureRouting

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init(environment.config)

    install(CORS) {
        allowHost("localhost:5173")
        allowHost("127.0.0.1:5173")
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
    }

    install(ContentNegotiation) {
        json()
    }

    val repository = PostgresRepositoryImpl()
    val measurementRepository = PostgresMeasurementRepositoryImpl()
    val wateringEventRepository = PostgresWateringEventRepository()
    val dispatcher = Dispatchers.IO

    configureRouting(
        getPlantsUseCase = GetPlantsUseCase(repository = repository, dispatcher = dispatcher),
        getPlantByIdUseCase = GetPlantByIdUseCase(dispatcher = dispatcher, repository = repository),
        addPlantUseCase = AddPlantUseCase(dispatcher = dispatcher, repository = repository),
        updatePlantUseCase = UpdatePlantUseCase(dispatcher = dispatcher, repository = repository),
        deletePlantUseCase = DeletePlantUseCase(dispatcher = dispatcher, repository = repository),
        addMeasurementUseCase = AddMeasurementUseCase(dispatcher = dispatcher, repository = measurementRepository),
        getLatestMeasurementUseCase = GetLatestMeasurementUseCase(
            dispatcher = dispatcher,
            repository = measurementRepository,
        ),
        getMeasurementsForPlantUseCase = GetMeasurementsForPlantUseCase(
            dispatcher = dispatcher,
            repository = measurementRepository,
        ),
        wateringEventRepository = wateringEventRepository,
    )
}
