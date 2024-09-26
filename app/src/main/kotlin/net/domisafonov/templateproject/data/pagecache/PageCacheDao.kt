package net.domisafonov.templateproject.data.pagecache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PageCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(page: RoomPage)

    @Query("SELECT * from RoomPage WHERE url = :url")
    fun observe(url: String): Flow<RoomPage?>
}
