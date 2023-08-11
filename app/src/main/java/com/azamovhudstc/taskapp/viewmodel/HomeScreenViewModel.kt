package com.azamovhudstc.taskapp.viewmodel

import androidx.lifecycle.MutableLiveData
import com.azamovhudstc.taskapp.data.remote.response.Lesson
import com.azamovhudstc.taskapp.utils.Resource

interface HomeScreenViewModel {
    val lessonsList: MutableLiveData<Resource<List<Lesson>>>
    fun loadHomeData()
}