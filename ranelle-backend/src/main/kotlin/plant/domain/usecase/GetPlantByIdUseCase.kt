package plant.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import plant.domain.entity.Plant
import plant.domain.repository.PlantRepository
import util.UseCase

class GetPlantByIdUseCase(
    override val dispatcher: CoroutineDispatcher,
    private val repository: PlantRepository
) : UseCase<Int, Plant?>() {

    override suspend fun execute(params: Int): Plant? {
        return repository.findById(params)
    }
}
