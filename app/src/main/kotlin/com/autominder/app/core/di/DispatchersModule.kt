package com.autominder.app.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Injectable dispatchers.
 *
 * `.claude/rules/data.md` requires that "the blocking class is responsible for
 * main-safety via an injected dispatcher (IO blocking, Default CPU)". Injecting
 * them rather than reaching for `Dispatchers.IO` inline is what makes those
 * classes testable: a unit test substitutes an unconfined or test dispatcher and
 * the suspending work runs deterministically instead of hopping threads.
 *
 * Note for Hilt: a Kotlin default parameter value does **not** satisfy Dagger.
 * A constructor annotated `@Inject` must have every parameter bound in the
 * graph, which is exactly why this module exists.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    /** Blocking I/O: disk, database, file system. */
    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /** CPU-bound work: parsing, sorting, status computation over large sets. */
    @Provides
    @Singleton
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}
