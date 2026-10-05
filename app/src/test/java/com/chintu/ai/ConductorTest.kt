package com.chintu.ai.agent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConductorTest {

    @Test
    fun workerLifecycle() {
        val conductor = Conductor()

        val worker = conductor.start("test-worker")

        assertNotNull(worker)
        assertEquals("test-worker", worker.role)
        assertEquals(
            WorkerStatus.STARTING,
            worker.status
        )

        conductor.update(
            worker.id,
            WorkerStatus.RUNNING
        )

        val running =
            conductor
                .snapshot()
                .firstOrNull {
                    it.id == worker.id
                }

        assertNotNull(running)
        assertEquals(
            WorkerStatus.RUNNING,
            running?.status
        )

        conductor.update(
            worker.id,
            WorkerStatus.COMPLETED,
            "Test completed"
        )

        val completed =
            conductor
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
            "Test completed",
            completed?.result
        )
    }

    @Test
    fun cancelWorker() {
        val conductor = Conductor()

        val worker = conductor.start("cancel-test")

        conductor.cancel(worker.id)

        val cancelled =
            conductor
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

        assertTrue(
            workers.any { it.id == first.id }
        )

        assertTrue(
            workers.any { it.id == second.id }
        )

        assertEquals(
            2,
            workers.size
        )
    }
}
