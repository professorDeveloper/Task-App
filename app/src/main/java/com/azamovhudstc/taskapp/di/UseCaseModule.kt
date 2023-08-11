package com.azamovhudstc.taskapp.di

import com.azamovhudstc.taskapp.usecase.LessonsUseCase
import com.azamovhudstc.taskapp.usecase.imp.LessonsUseCaseImp
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)

interface UseCaseModule {
    @Binds
    fun getLessonsRepository(imp: LessonsUseCaseImp): LessonsUseCase
}