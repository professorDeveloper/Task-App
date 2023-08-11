package com.azamovhudstc.taskapp.data.remote.response

import java.io.Serializable

data class Lesson(
    val description: String,
    val id: Int,
    val name: String,
    val thumbnail: String,
    val video_url: String

):Serializable