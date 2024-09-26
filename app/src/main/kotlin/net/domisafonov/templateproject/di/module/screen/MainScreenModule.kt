package net.domisafonov.templateproject.di.module.screen

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import net.domisafonov.templateproject.ui.mainscreen.MainScreenMvi

@Module
@InstallIn(ViewModelComponent::class)
object MainScreenModule {
    @Provides
    @ViewModelScoped
    fun actor(): MainScreenMvi.Actor = MainScreenMvi.Actor()
}
