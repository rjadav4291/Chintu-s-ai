package com.chintu.ai.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConductorTest {

    @Test
    fun startWorker() {
        val conductor = Conductor()

        val worker = conductor.start("test-worker")

        assertNotNull(worker)
        assertEquals("test-worker", worker.role)
        assertEquals(
            WorkerStatus.STARTING,
            worker.status
        )

        assertTrue(
            conductor.snapshot().any {
                it.id == worker.id
            }
        )
    }

    @Test
    fun updateWorkerToRunning() {
        val conductor = Conductor()

        val worker = conductor.start("test-worker")

        conductor.update(
            worker.id,
            WorkerStatus.RUNNING
        )

        val updated = conductor
            .snapshot()
            .firstOrNull {
                it.id == worker.id
            }

        assertNotNull(updated)

        assertEquals(
            WorkerStatus.RUNNING,
            updated?.status
        )
    }

    @Test
    fun completeWorkerWithResult() {
        val conductor = Conductor()

        val worker = conductor.start("test-worker")

        conductor.update(
            worker.id,
            WorkerStatus.COMPLETED,
            "Test completed successfully"
        )

        val completed = conductor
            .snapshot()
            .firstOrNull {
                it.id == worker.id
            }

        assertNotNull(completed)

        assertEquals(
            WorkerStatus.COMPLETED,
            completed?.status
        )

        assertEquals(
            "Test completed successfully",
            completed?.result
        )
    }

    @Test
    fun cancelWorker() {
        val conductor = Conductor()

        val worker = conductor.start("cancel-test")

        conductor.cancel(worker.id)

        val cancelled = conductor
            .snapshot()
            .firstOrNull {
                it.id == worker.id
            }

        assertNotNull(cancelled)

        assertEquals(
            WorkerStatus.CANCELLED,
            cancelled?.status
        )
    }

    @Test
    fun multipleWorkersCanBeCreated() {
        val conductor = Conductor()

        val first = conductor.start("worker-one")
        val second = conductor.start("worker-two")

        val workers = conductor.snapshot()

        assertEquals(
            2,
            workers.size
        )

        assertTrue(
            workers.any {
                it.id == first.id
            }
        )

        assertTrue(
            workers.any {
                it.id == second.id
            }
        )
    }
}
