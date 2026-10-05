package com.chintu.ai.agent

enum class WorkerStatus {
    STARTING,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class Worker(
    val id: String,
    val role: String,
    var status: WorkerStatus,
    val result: String? = null
)

class Conductor {

    private val workers =
        mutableMapOf<String, Worker>()

    fun start(
        role: String
    ): Worker {

        val w =
            Worker(
                java.util.UUID
                    .randomUUID()
                    .toString(),
                role,
                WorkerStatus.STARTING
            )

        workers[w.id] = w

        return w
    }

    fun update(
        id: String,
        status: WorkerStatus,
        result: String? = null
    ) {

        workers[id]?.let {

            it.status = status

            workers[id] =
                it.copy(
                    result =
                        result
                            ?: it.result
                )
        }
    }

    fun cancel(
        id: String
    ) {

        update(
            id,
            WorkerStatus.CANCELLED
        )
    }

    fun snapshot() =
        workers.values.toList()
}
