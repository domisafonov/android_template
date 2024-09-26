package net.domisafonov.templateproject.data.pagecache.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.domisafonov.templateproject.domain.model.Page
import java.util.Date

@Entity
data class RoomPage(
    @PrimaryKey val url: String,
    val date: Date,
    val contents: String, // expecting only normal utf
)

fun RoomPage.toPage(): Page = Page(
    url = url,
    date = date,
    contents = contents,
)

fun Page.toRoomPage(): RoomPage = RoomPage(
    url = url,
    date = date,
    contents = contents,
)
