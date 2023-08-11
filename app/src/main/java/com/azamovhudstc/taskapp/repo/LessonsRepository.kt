package com.azamovhudstc.taskapp.repo

import com.azamovhudstc.taskapp.data.remote.response.Lesson
import kotlinx.coroutines.flow.Flow

interface LessonsRepository {
    fun getLessons(): Flow<List<Lesson>?>
}