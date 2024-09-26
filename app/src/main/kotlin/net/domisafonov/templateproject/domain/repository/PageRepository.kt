package net.domisafonov.templateproject.domain.repository

import kotlinx.coroutines.flow.Flow
import net.domisafonov.templateproject.domain.model.Page

interface PageRepository {
    fun observePage(url: String): Flow<Page?>
    suspend fun forceUpdatePage(url: String)
}
