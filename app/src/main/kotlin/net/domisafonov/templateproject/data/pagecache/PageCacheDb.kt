package net.domisafonov.templateproject.data.pagecache

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import net.domisafonov.templateproject.data.pagecache.entity.RoomPage

const val PAGE_CACHE_DB_VERSION = 1
const val PAGE_CACHE_DB_NAME = "page_cache_db"

@Database(
    entities = [RoomPage::class],
    version = PAGE_CACHE_DB_VERSION,
)
@TypeConverters(RoomConverters::class)
abstract class PageCacheDb : RoomDatabase() {
    abstract fun pageCache(): PageCacheDao
}
