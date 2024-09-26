package net.domisafonov.templateproject.di.module

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.domisafonov.templateproject.ui.util.ResourceDelegate
import net.domisafonov.templateproject.ui.util.ResourceDelegateImpl

@Module
@InstallIn(SingletonComponent::class)
object UiUtilitiesModule {
    @Provides
    fun resourceDelegate(
        @ApplicationContext context: Context,
    ): ResourceDelegate = ResourceDelegateImpl(
        resources = context.resources,
    )
}
