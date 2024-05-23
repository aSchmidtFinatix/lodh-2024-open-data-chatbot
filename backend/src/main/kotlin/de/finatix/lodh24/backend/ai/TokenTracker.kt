package de.finatix.lodh24.backend.ai

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class TokenTracker {

    private val tokenUsage = ConcurrentHashMap<Long, Long>()
    private val mutex = Mutex()

    suspend fun addTokens(tokens: Long) {
        val currentTime = System.currentTimeMillis() / 60000
        mutex.withLock {
            tokenUsage.merge(currentTime, tokens, Long::plus)
        }
    }

    suspend fun getTokensUsedInLastMinute(): Long {
        val currentTime = System.currentTimeMillis() / 60000 // current minute
        return mutex.withLock {
            tokenUsage[currentTime] ?: 0
        }
    }
}