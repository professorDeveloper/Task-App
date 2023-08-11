package com.azamovhudstc.taskapp.repo.imp

import com.azamovhudstc.taskapp.data.remote.api.LessonsApi
import com.azamovhudstc.taskapp.data.remote.response.Lesson
import com.azamovhudstc.taskapp.repo.LessonsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class LessonsRepositoryImp @Inject constructor(private val api: LessonsApi) : LessonsRepository {
    override fun getLessons() = flow<List<Lesson>?> {
        val response = api.getLessons()
        if (response.isSuccessful){
            emit(response.body()?.lessons)
        }
    }.flowOn(Dispatchers.IO)
}