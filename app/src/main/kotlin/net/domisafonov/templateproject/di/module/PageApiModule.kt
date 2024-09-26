package net.domisafonov.templateproject.di.module

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.domisafonov.templateproject.BuildConfig
import net.domisafonov.templateproject.data.pageapi.PageApi
import net.domisafonov.templateproject.di.PageApiRetrofit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PageApiModule {

    @Provides
    @Singleton
    fun okhttp(): OkHttpClient = OkHttpClient()

    @Provides
    @Singleton
    @PageApiRetrofit
    fun retrofit(
        okhttp: OkHttpClient,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.PAGE_API_URL)
        .client(okhttp)
        .apply { if (BuildConfig.DEBUG) { validateEagerly(true) } }
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    @Provides
    @Reusable
    fun pageApi(
        @PageApiRetrofit retrofit: Retrofit,
    ): PageApi = retrofit
        .create(PageApi::class.java)
}
