package com.azamovhudstc.taskapp.data.remote.api

import com.azamovhudstc.taskapp.data.remote.response.LessonsResponse
import retrofit2.Response
import retrofit2.http.GET

interface LessonsApi {
    @GET("/test-api/lessons")
    suspend fun getLessons():Response<LessonsResponse>

}