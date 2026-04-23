package com.orchestradashboard.shared.push

import com.orchestradashboard.shared.domain.model.NotificationSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.util.UUID
import java.util.prefs.Preferences
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DesktopNotificationLocalStoreTest {
    private val nodeName = "orchestra-test-${UUID.randomUUID()}"
    private val prefs: Preferences = Preferences.userRoot().node(nodeName)
    private val store = DesktopNotificationLocalStore(prefs)

    @AfterTest
    fun tearDown() {
        prefs.removeNode()
        prefs.flush()
    }

    @Test
    fun `load returns defaults when nothing has been saved`() =
        runTest {
            val result = store.load()

            assertEquals(NotificationSettings(), result)
        }

    @Test
    fun `save persists settings that survive subsequent load`() =
        runTest {
            val target =
                NotificationSettings(
                    enabled = false,
                    notifyOnSuccess = false,
                    notifyOnFailure = true,
                )

            store.save(target)
            val reloaded = store.load()

            assertEquals(target, reloaded)
        }

    @Test
    fun `observe emits latest settings after save`() =
        runTest {
            val target = NotificationSettings(enabled = false)

            store.save(target)
            val observed = store.observe().first()

            assertEquals(target, observed)
        }
}
