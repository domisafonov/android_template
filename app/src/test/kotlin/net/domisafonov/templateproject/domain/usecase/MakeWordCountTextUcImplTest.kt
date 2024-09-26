package net.domisafonov.templateproject.domain.usecase

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MakeWordCountTextUcImplTest {

    private val uc = MakeWordCountTextUcImpl(Dispatchers.Unconfined)

    @Test
    fun empty() = runTest {
        assertThat(uc.execute("")).isEmpty()
    }

    @Test
    fun onlySpaces() = runTest {
        assertThat(uc.execute(" \t\r\n")).isEmpty()
    }

    @Test
    fun oneWord() = runTest {
        assertThat(uc.execute("a")).isEqualTo(listOf("\"a\": 1"))
    }

    @Test
    fun multipleOfOneWord() = runTest {
        assertThat(uc.execute("a a a")).isEqualTo(listOf("\"a\": 3"))
    }

    @Test
    fun longSpaces() = runTest {
        assertThat(uc.execute("a   a  a")).isEqualTo(listOf("\"a\": 3"))
    }

    @Test
    fun differentSpaces() = runTest {
        assertThat(uc.execute("a \t\ra\n a")).isEqualTo(listOf("\"a\": 3"))
    }

    @Test
    fun edgeSpaces() = runTest {
        assertThat(uc.execute(" a a a\t")).isEqualTo(listOf("\"a\": 3"))
    }

    @Test
    fun multipleWords() = runTest {
        assertThat(uc.execute("hamster ham\uD83C\uDF09 ハムスター хомяк ham\uD83C\uDF09"))
            .isEqualTo(listOf("\"hamster\": 1", "\"ham\uD83C\uDF09\": 2", "\"хомяк\": 1", "\"ハムスター\": 1"))
    }
}
