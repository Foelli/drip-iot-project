package plant.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import plant.domain.entity.Plant
import plant.domain.repository.PlantRepository
import util.UseCase

class AddPlantUseCase(
    override val dispatcher: CoroutineDispatcher,
    private val repository: PlantRepository
): UseCase<Plant, Unit>() {

    override suspend fun execute(params: Plant) {
        repository.addPlant(params)
    }
}