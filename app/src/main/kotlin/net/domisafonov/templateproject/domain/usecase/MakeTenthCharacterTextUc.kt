package net.domisafonov.templateproject.domain.usecase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

/**
 * Takes every 10th unicode code point in the string, starting with the 10th,
 * into a new string.
 */
fun interface MakeTenthCharacterTextUc {
    suspend fun execute(src: String): String
}

class MakeTenthCharacterTextUcImpl(
    private val ioDispatcher: CoroutineDispatcher,
) : MakeTenthCharacterTextUc {

    override suspend fun execute(src: String): String = withContext(ioDispatcher) {
        val codePointList = ArrayList<Int>(src.length)
        var n = 1
        src.codePoints().forEach { cp ->
            if (n % 10 == 0) codePointList += cp
            ++n
        }
        codePointList.joinToString(separator = "") { String(Character.toChars(it)) }
    }
}
