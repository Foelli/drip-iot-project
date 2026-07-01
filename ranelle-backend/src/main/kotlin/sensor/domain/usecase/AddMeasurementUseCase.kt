package sensor.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import sensor.domain.entity.Measurement
import sensor.domain.repository.MeasurementRepository
import util.UseCase

class AddMeasurementUseCase(
    override val dispatcher: CoroutineDispatcher,
    private val repository: MeasurementRepository,
) : UseCase<Measurement, Measurement>() {
    override suspend fun execute(params: Measurement): Measurement =
        repository.addMeasurement(params)
}
