package util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

abstract class UseCase<P, R> {
    abstract val dispatcher: CoroutineDispatcher

    suspend operator fun invoke(params: P): R = withContext(dispatcher) {
        execute(params)
    }

    abstract suspend fun execute(params: P): R
}