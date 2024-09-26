package net.domisafonov.templateproject.ui.util

import android.content.res.Resources
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

interface ResourceDelegate {
    fun getString(@StringRes id: Int): String
    fun getString(@StringRes id: Int, vararg formatArgs: Any?): String
    fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any?): String
    fun getQuantityString(@PluralsRes id: Int, quantity: Int): String
}

class ResourceDelegateImpl(
    private val resources: Resources
) : ResourceDelegate {
    override fun getString(@StringRes id: Int): String = resources.getString(id)

    override fun getString(@StringRes id: Int, vararg formatArgs: Any?): String =
        resources.getString(id, *formatArgs)

    override fun getQuantityString(@PluralsRes id: Int, quantity: Int, vararg formatArgs: Any?): String =
        resources.getQuantityString(id, quantity, *formatArgs)

    override fun getQuantityString(@PluralsRes id: Int, quantity: Int): String =
        resources.getQuantityString(id, quantity)
}
