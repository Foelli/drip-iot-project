package sensor.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import sensor.domain.entity.Measurement
import sensor.domain.repository.MeasurementRepository
import util.UseCase

class GetLatestMeasurementUseCase(
    override val dispatcher: CoroutineDispatcher,
    private val repository: MeasurementRepository,
) : UseCase<Int, Measurement?>() {
    override suspend fun execute(params: Int): Measurement? =
        repository.getLatestMeasurementForPlant(params)
}
