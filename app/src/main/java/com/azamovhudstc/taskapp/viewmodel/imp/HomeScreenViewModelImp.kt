package com.azamovhudstc.taskapp.viewmodel.imp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azamovhudstc.taskapp.data.remote.response.Lesson
import com.azamovhudstc.taskapp.usecase.imp.LessonsUseCaseImp
import com.azamovhudstc.taskapp.utils.*
import com.azamovhudstc.taskapp.viewmodel.HomeScreenViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModelImp @Inject constructor(private val useCaseImp: LessonsUseCaseImp) :
    HomeScreenViewModel, ViewModel() {
    override val lessonsList: MutableLiveData<Resource<List<Lesson>>> = MutableLiveData()

    override fun loadHomeData() {
        lessonsList.postValue(Resource.Loading)
        if (!isOnline(currContext()!!)) {
            if (loadData<List<Lesson>>("localData")?.size == 0) {
                lessonsList.postValue(Resource.Error(Exception("No Internet Connected ! ")))
            } else {
                lessonsList.postValue(Resource.Success(loadData<List<Lesson>>("localData")!!))

            }
        } else {
                useCaseImp.getLessons().onEach {
                    if (it != null) {
                        saveData("localData", it)
                        lessonsList.postValue(Resource.Success(it))
                    } else {
                        lessonsList.postValue(Resource.Success(loadData<List<Lesson>>("localData")!!))

                    }
                }.launchIn(viewModelScope)
        }

    }
}