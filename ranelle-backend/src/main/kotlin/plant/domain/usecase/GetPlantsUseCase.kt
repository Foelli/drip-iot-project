package plant.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import plant.domain.entity.Plant
import plant.domain.repository.PlantRepository
import util.UseCase

class GetPlantsUseCase(
    private val repository: PlantRepository,
    override val dispatcher: CoroutineDispatcher
) : UseCase<Unit, List<Plant>>() {
    override suspend fun execute(params: Unit): List<Plant> {
       return repository.getAllPlants()
    }
}