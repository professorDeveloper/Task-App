package com.azamovhudstc.taskapp.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePaddingRelative
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.azamovhudstc.taskapp.R
import com.azamovhudstc.taskapp.data.remote.response.Lesson
import com.azamovhudstc.taskapp.databinding.HomeScreenBinding
import com.azamovhudstc.taskapp.ui.adapter.HomeItemAdapter
import com.azamovhudstc.taskapp.utils.*
import com.azamovhudstc.taskapp.viewmodel.imp.HomeScreenViewModelImp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeScreen : Fragment() {
    var style: Int = 0
    private var _binding: HomeScreenBinding? = null
    private lateinit var mediaAdaptor: HomeItemAdapter



     val model by viewModels<HomeScreenViewModelImp>()
    private val binding get() = _binding!!
    var height = statusBarHeight



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeScreenBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRv()
        model.lessonsList.observe(
            viewLifecycleOwner
        ) {
            when (it) {
                is Resource.Error -> {
                    snackString(it.throwable.message)
                    binding.animeTrendingProgressBar.gone()
                }
                Resource.Loading -> {
                    binding.lessonsRv.gone()
                    binding.animeTrendingProgressBar.visible()
                }

                is Resource.Success -> {
                    val isFirst = loadData<Boolean>("isFirst") ?: false

                    if (!isFirst) {
                        val unLockList = ArrayList<Lesson>()
                        var count = 1
                        it.data.onEach {
                            if (count <= 3) {
                                count++
                                unLockList.add(it)
                            }
                        }
                        saveData("isFirst", true)
                        saveData("unLockList", unLockList)
                    }

                    mediaAdaptor = HomeItemAdapter(
                        style,
                        it.data.toMutableList(),
                        this,
                        matchParent = true
                    )
                    binding.animeTrendingProgressBar.gone()
                    binding.lessonsRv.visible()
                    binding.lessonsRv.adapter = mediaAdaptor
                    mediaAdaptor.type = Save.type
                    binding.lessonsRv.layoutManager = GridLayoutManager(context, Save.spanCount)
                    binding.root.isMotionEventSplittingEnabled = false

                    binding.apply {
                        toggleButtonGroup.addOnButtonCheckedListener { toggleButtonGroup, checkedId, isChecked ->
                            if (isChecked) {
                                var spanCount = 0
                                var type = 0
                                when (checkedId) {
                                    R.id.b_grid_1 -> {
                                        type = 0
                                        spanCount = 3
                                    }
                                    R.id.b_grid_2 -> {
                                        type = 1
                                        spanCount = 1
                                    }
                                }
                                mediaAdaptor.type = type
                                lessonsRv.layoutManager = GridLayoutManager(context, spanCount)
                                Save.type = type
                                Save.spanCount = spanCount
                            } else {
                                if (toggleButtonGroup.checkedButtonId == View.NO_ID) {
                                    snackString("No Alignment Selected")
                                }
                            }
                        }

                    }

                }
            }
        }

        initView()
        binding.animeRefresh.setSlingshotDistance(height + 250)
        binding.animeRefresh.setProgressViewEndTarget(false, height + 250)

    }

    private fun initView() {

    }


    private fun initRv() {
        binding.apply {
            binding.animeRefresh.setSlingshotDistance(height + 250)
            binding.animeRefresh.setProgressViewEndTarget(false, height + 250)
            binding.animeRefresh.setOnRefreshListener {
                Refresh.activity[this.hashCode()]!!.postValue(
                    true
                )
            }
            val live = Refresh.activity.getOrPut(this.hashCode()) { MutableLiveData(false) }
            live.observe(viewLifecycleOwner) {
                binding.animeRefresh.isRefreshing = false
                model.loadHomeData()
                loadedBrowse = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (!loadedBrowse) Refresh.activity[this.hashCode()]!!.postValue(true)
    }


}