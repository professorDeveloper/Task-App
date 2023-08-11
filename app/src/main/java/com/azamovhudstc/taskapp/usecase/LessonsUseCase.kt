package com.azamovhudstc.taskapp.usecase

import com.azamovhudstc.taskapp.data.remote.response.Lesson
import kotlinx.coroutines.flow.Flow

interface LessonsUseCase {
    fun getLessons(): Flow<List<Lesson>?>
}