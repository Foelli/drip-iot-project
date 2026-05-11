package plant.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import plant.domain.repository.PlantRepository
import util.UseCase

class DeletePlantUseCase(
    override val dispatcher: CoroutineDispatcher,
    private val repository: PlantRepository
) : UseCase<Int, Unit>() {

    override suspend fun execute(params: Int) {
        repository.deletePlant(params)
    }
}
