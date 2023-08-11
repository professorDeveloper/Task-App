package com.azamovhudstc.taskapp.usecase.imp

import com.azamovhudstc.taskapp.data.remote.response.Lesson
import com.azamovhudstc.taskapp.repo.LessonsRepository
import com.azamovhudstc.taskapp.usecase.LessonsUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LessonsUseCaseImp @Inject constructor(private val repository: LessonsRepository) :
    LessonsUseCase {
    override fun getLessons(): Flow<List<Lesson>?> {
        return repository.getLessons()
    }

}