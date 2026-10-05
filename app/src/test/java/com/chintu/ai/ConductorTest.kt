package com.chintu.ai

import com.chintu.ai.agent.Conductor
import com.chintu.ai.agent.WorkerStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ConductorTest {

    @Test
    fun workerLifecycleIsTruthful() {

        val c =
            Conductor()

        val w =
            c.start(
                "Research Agent"
            )

        assertEquals(
            WorkerStatus.STARTING,
            w.status
        )

        c.update(
            w.id,
            WorkerStatus.RUNNING
        )

        assertEquals(
            WorkerStatus.RUNNING,
            c.snapshot()
                .single()
                .status
        )

        c.cancel(w.id)

        assertEquals(
            WorkerStatus.CANCELLED,
            c.snapshot()
                .single()
                .status
        )
    }
}
