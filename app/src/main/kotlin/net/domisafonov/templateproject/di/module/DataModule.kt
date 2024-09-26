package net.domisafonov.templateproject.di.module

import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import net.domisafonov.templateproject.data.RoomPageRepository
import net.domisafonov.templateproject.data.pageapi.PageApi
import net.domisafonov.templateproject.data.pagecache.PageCacheDb
import net.domisafonov.templateproject.di.IoDispatcher
import net.domisafonov.templateproject.domain.repository.PageRepository

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Reusable
    fun pageRepository(
        db: PageCacheDb,
        pageApi: PageApi,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): PageRepository = RoomPageRepository(
        pageCacheDao = db.pageCache(),
        pageApi = pageApi,
        ioDispatcher = ioDispatcher,
    )
}
