package com.azamovhudstc.taskapp.di

import com.azamovhudstc.taskapp.repo.LessonsRepository
import com.azamovhudstc.taskapp.repo.imp.LessonsRepositoryImp
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)

interface RepositoryModule {
    @Binds
    fun getLessonsRepository(imp: LessonsRepositoryImp): LessonsRepository
}