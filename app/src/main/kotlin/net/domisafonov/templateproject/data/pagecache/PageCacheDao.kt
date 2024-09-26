package net.domisafonov.templateproject.data.pagecache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.domisafonov.templateproject.data.pagecache.entity.RoomPage

@Dao
interface PageCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(page: RoomPage)

    @Query("SELECT * from RoomPage WHERE url = :url")
    fun observe(url: String): Flow<RoomPage?>
}
