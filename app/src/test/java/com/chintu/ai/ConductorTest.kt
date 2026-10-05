package com.chintu.ai.agent

class Conductor {

    private val workers = mutableMapOf<String, Thread>()

    fun start(id: String, task: () -> Unit) {
        stop(id)

        val worker = Thread {
            try {
                task()
            } finally {
                workers.remove(id)
            }
        }

        workers[id] = worker
        worker.start()
    }

    fun stop(id: String) {
        workers.remove(id)?.interrupt()
    }

    fun stopAll() {
        workers.values.forEach { it.interrupt() }
        workers.clear()
    }

    fun isRunning(id: String): Boolean {
        return workers[id]?.isAlive == true
    }

    fun runningCount(): Int {
        return workers.values.count { it.isAlive }
    }
}
