package net.toload.main.hd

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withTimeoutOrNull

class QueryDispatcher {
    private val scope = CoroutineScope(SupervisorJob())

    @Volatile
    private var queryJob: Job? = null

    fun launchQuery(block: Runnable) = launchQuery(0L, block)

    /**
     * perf: optional [delayMs] debounce. Because each new keystroke calls cancel()
     * before launching, a delayed job is cancelled (during its cancellable delay)
     * when the next key arrives, so only the last keystroke of a burst hits the DB.
     * delayMs == 0 preserves the original immediate behaviour.
     */
    fun launchQuery(delayMs: Long, block: Runnable) {
        queryJob?.cancel()
        queryJob = scope.launch {
            if (delayMs > 0) delay(delayMs)
            runInterruptible(Dispatchers.IO) {
                block.run()
            }
        }
    }

    val isActive: Boolean
        get() = queryJob?.isActive == true

    fun awaitCompletion(timeoutMs: Long) {
        val job = queryJob ?: return
        if (!job.isActive) return
        runBlocking {
            withTimeoutOrNull(timeoutMs) { job.join() }
        }
    }

    fun cancel() {
        queryJob?.cancel()
    }

    fun destroy() {
        scope.cancel()
    }
}
