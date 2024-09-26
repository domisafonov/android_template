package net.domisafonov.templateproject.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Count words and output a string with a count for each unique word.
 *
 * "a hamster is a squirrel" would get
 * ```
 * "a": 2
 * "hamster": 1
 * "is": 1
 * "squirrel": 1
 * ```
 *
 * Uses the java regex definition of a "space character", some "space-like"
 * characters special from Unicode are not accounted for.
 */
fun interface MakeWordCountTextUc {

    suspend fun execute(src: String): String
}

class MakeWordCountTextUcImpl(
    private val ioDispatcher: CoroutineDispatcher,
) : MakeWordCountTextUc {

    override suspend fun execute(src: String): String = withContext(ioDispatcher) {
        val words = mutableMapOf<String, Int>()
        src.split("\\s++".toPattern()).forEach { words[it] = (words[it] ?: 0) + 1 }
        words.remove("")
        words.toSortedMap().entries.joinToString(separator = "\n") { (word, count) ->
            "\"$word\": $count"
        }
    }
}
