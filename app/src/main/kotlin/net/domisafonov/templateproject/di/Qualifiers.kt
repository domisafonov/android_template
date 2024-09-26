package net.domisafonov.templateproject.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class PageApiRetrofit

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.SOURCE)
annotation class RepositoryScope
