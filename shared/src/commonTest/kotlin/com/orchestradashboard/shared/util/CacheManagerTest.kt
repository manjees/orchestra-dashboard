package com.orchestradashboard.shared.util

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CacheManagerTest {
    @Test
    fun `should return cached data immediately on subsequent fetches`() =
        runTest {
            val cache = CacheManager<String, List<String>>(maxSize = 10, ttlMs = 60_000L)
            var fetchCount = 0
            cache.getOrFetch("key") {
                fetchCount++
                listOf("a", "b")
            }
            cache.getOrFetch("key") {
                fetchCount++
                listOf("a", "b")
            }
            assertEquals(1, fetchCount)
        }

    @Test
    fun `should evict entry after TTL expires`() =
        runTest {
            var currentTime = 0L
            val cache = CacheManager<String, String>(maxSize = 10, ttlMs = 1000L, clock = { currentTime })
            cache.getOrFetch("key") { "value" }
            currentTime = 1001L
            var fetchCount = 0
            cache.getOrFetch("key") {
                fetchCount++
                "new"
            }
            assertEquals(1, fetchCount)
        }

    @Test
    fun `should evict LRU entry when max size exceeded`() =
        runTest {
            val cache = CacheManager<String, String>(maxSize = 2, ttlMs = 60_000L)
            cache.getOrFetch("a") { "1" }
            cache.getOrFetch("b") { "2" }
            cache.getOrFetch("c") { "3" }
            var fetchCount = 0
            cache.getOrFetch("a") {
                fetchCount++
                "re-fetched"
            }
            assertEquals(1, fetchCount)
        }

    @Test
    fun `invalidateAll clears all entries`() =
        runTest {
            val cache = CacheManager<String, String>(maxSize = 10, ttlMs = 60_000L)
            cache.getOrFetch("a") { "1" }
            cache.invalidateAll()
            var fetchCount = 0
            cache.getOrFetch("a") {
                fetchCount++
                "new"
            }
            assertEquals(1, fetchCount)
        }

    @Test
    fun `invalidate removes specific key`() =
        runTest {
            val cache = CacheManager<String, String>(maxSize = 10, ttlMs = 60_000L)
            cache.getOrFetch("a") { "1" }
            cache.getOrFetch("b") { "2" }
            cache.invalidate("a")
            var aCount = 0
            var bCount = 0
            cache.getOrFetch("a") {
                aCount++
                "new"
            }
            cache.getOrFetch("b") {
                bCount++
                "new"
            }
            assertEquals(1, aCount)
            assertEquals(0, bCount)
        }
}
