package net.domisafonov.templateproject.domain.usecase

import net.domisafonov.templateproject.BuildConfig
import java.net.MalformedURLException
import java.net.URL

fun interface ValidateUrlInputUc {
    suspend fun execute(input: String): Boolean
}

class ValidateUrlInputUcImpl : ValidateUrlInputUc {
    override suspend fun execute(input: String): Boolean {
        val originalUrl = URL(BuildConfig.PAGE_API_URL)

        val newUrl = try {
            URL(originalUrl, input)
        } catch (e: MalformedURLException) {
            return false
        }

        return newUrl.protocol == originalUrl.protocol &&
            newUrl.port == originalUrl.port &&
            newUrl.host == originalUrl.host &&
            newUrl.authority == originalUrl.authority &&
            newUrl.userInfo == originalUrl.userInfo
    }
}
