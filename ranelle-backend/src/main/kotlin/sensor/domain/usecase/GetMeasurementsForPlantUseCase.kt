package sensor.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import sensor.domain.entity.Measurement
import sensor.domain.repository.MeasurementRepository
import util.UseCase

class GetMeasurementsForPlantUseCase(
    override val dispatcher: CoroutineDispatcher,
    private val repository: MeasurementRepository,
) : UseCase<Int, List<Measurement>>() {
    override suspend fun execute(params: Int): List<Measurement> =
        repository.getMeasurementsForPlant(params)
}
