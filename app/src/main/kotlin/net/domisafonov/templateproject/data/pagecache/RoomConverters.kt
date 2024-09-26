package net.domisafonov.templateproject.data.pagecache

import androidx.room.TypeConverter
import java.util.Date

class RoomConverters {

    @TypeConverter
    fun fromDate(value: Date?): Long? = value?.time

    @TypeConverter
    fun toDate(value: Long?): Date? = value?.let(::Date)
}
