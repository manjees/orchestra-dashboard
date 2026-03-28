package com.orchestradashboard.shared.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

class CacheManager<K, V>(
    private val maxSize: Int,
    private val ttlMs: Long,
    private val clock: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {
    private data class CachedValue<V>(val value: V, val timestamp: Long)

    private val cache = mutableMapOf<K, CachedValue<V>>()
    private val accessOrder = mutableListOf<K>()
    private val mutex = Mutex()

    private fun touchKey(key: K) {
        accessOrder.remove(key)
        accessOrder.add(key)
    }

    private fun evictIfNeeded() {
        while (cache.size > maxSize && accessOrder.isNotEmpty()) {
            val oldest = accessOrder.removeFirst()
            cache.remove(oldest)
        }
    }

    suspend fun getOrFetch(
        key: K,
        fetcher: suspend () -> V,
    ): V {
        mutex.withLock {
            val entry = cache[key]
            if (entry != null && clock() - entry.timestamp < ttlMs) {
                touchKey(key)
                return entry.value
            }
            cache.remove(key)
            accessOrder.remove(key)
        }

        val newValue = fetcher()
        mutex.withLock {
            cache[key] = CachedValue(newValue, clock())
            touchKey(key)
            evictIfNeeded()
        }
        return newValue
    }

    suspend fun invalidateAll() {
        mutex.withLock {
            cache.clear()
            accessOrder.clear()
        }
    }

    suspend fun invalidate(key: K) {
        mutex.withLock {
            cache.remove(key)
            accessOrder.remove(key)
        }
    }
}
