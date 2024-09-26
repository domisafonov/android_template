package net.domisafonov.templateproject.domain.usecase

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MakeTenthCharacterTextUcImplTest {

    private val uc = MakeTenthCharacterTextUcImpl(ioDispatcher = Dispatchers.Unconfined)

    @Test
    fun empty() = runTest {
        assertThat(uc.execute("")).isEmpty()
    }

    @Test
    fun lessThan10() = runTest {
        assertThat(uc.execute("123456789")).isEmpty()
    }

    @Test
    fun ten() = runTest {
        assertThat(uc.execute("0123456789")).isEqualTo("9")
    }

    @Test
    fun moreThan10NotMultipleOf10() = runTest {
        assertThat(uc.execute("0123456789qwerty")).isEqualTo("9")
    }

    @Test
    fun surrogatePair() = runTest {
        assertThat(uc.execute("012345678\uD83C\uDF09")).isEqualTo("\uD83C\uDF09")
        assertThat(uc.execute("01234567\uD83C\uDF099")).isEqualTo("9")
    }

    @Test
    fun longString() = runTest {
        assertThat(uc.execute("qwertyuio[]asdfghjkl;'\\zxcvbnm,./")).isEqualTo("[lm")
    }
}
