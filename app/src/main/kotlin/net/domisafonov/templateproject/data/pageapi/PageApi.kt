package net.domisafonov.templateproject.data.pageapi

import retrofit2.http.GET
import retrofit2.http.Url

interface PageApi {

    @GET
    suspend fun getPage(@Url url: String): String
}
