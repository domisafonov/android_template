package net.domisafonov.templateproject.domain.model

import java.util.Date

data class Page(
    val url: String,
    val date: Date,
    val contents: String, // expecting only normal utf
)
