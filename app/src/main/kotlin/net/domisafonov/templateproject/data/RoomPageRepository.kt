package net.domisafonov.templateproject.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import net.domisafonov.templateproject.data.pageapi.PageApi
import net.domisafonov.templateproject.data.pagecache.PageCacheDao
import net.domisafonov.templateproject.data.pagecache.entity.RoomPage
import net.domisafonov.templateproject.data.pagecache.entity.toPage
import net.domisafonov.templateproject.domain.model.Page
import net.domisafonov.templateproject.domain.repository.PageRepository
import timber.log.Timber
import java.util.Collections
import java.util.Date
import java.util.WeakHashMap
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

// TODO: to cache arbitrary amount (not a fixed set) of pages if necessary,
//  please implement and call a cache clearing procedure
class RoomPageRepository(
    private val pageCacheDao: PageCacheDao,
    private val pageApi: PageApi,
    private val ioDispatcher: CoroutineDispatcher,
) : PageRepository {

    companion object {
        private val TIMEOUT = 5.minutes
    }

    private val inProgressUrls = Collections.synchronizedMap(WeakHashMap<RequestKey, Unit>())

    override fun observePage(url: String): Flow<Page?> = flow {

        val first = pageCacheDao.observe(url).first()
        if (first != null) emit(first.toPage())
        if (first == null || (Date().time - first.date.time).milliseconds > TIMEOUT) {
            forceUpdatePage(url)
        }

        pageCacheDao.observe(url).collect { emit(it?.toPage()) }
    }.conflate().flowOn(ioDispatcher)

    override suspend fun forceUpdatePage(url: String) = withContext(ioDispatcher) {

        Timber.v("requested to update the page for $url")

        val key = RequestKey(url)

        if (inProgressUrls.containsKey(key)) {
            return@withContext
        }

        Timber.v("updating page for $url")

        try {
            inProgressUrls[key] = Unit

            val body = try {
                pageApi.getPage(url)
            } catch (e: Throwable) {
                Timber.e(e, "Error requesting the page from the api")
                return@withContext
            }

            pageCacheDao.insert(
                RoomPage(
                    url = url,
                    date = Date(),
                    contents = body,
                )
            )

            Timber.v("Page update success")
        } finally {
            inProgressUrls.remove(key)
        }

        Timber.v("Page update completed")
    }

    private data class RequestKey(val url: String)
}
