package com.azamovhudstc.taskapp.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.azamovhudstc.taskapp.R
import com.azamovhudstc.taskapp.data.remote.response.Lesson
import com.azamovhudstc.taskapp.databinding.DetailScreenBinding
import com.azamovhudstc.taskapp.ui.activity.ExoPlayerActivity
import com.azamovhudstc.taskapp.utils.*


class DetailScreen : Fragment() {
    private var _binding: DetailScreenBinding? = null
    private val binding get() = _binding!!
    private val uiSettings = loadData<UISettings>("ui_settings") ?: UISettings()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DetailScreenBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initDescription()
        binding.posterContainer.slideStart(800,0)
        binding.fabBack.slideStart(800,0)
        binding.fabWatch.slideUp(800,0)
        binding.llTitle.slideStart(800,0)
        binding.layoutDescription.llDescription.slideStart(800,0)
        binding.fabWatch.setSafeOnClickListener {
            val data = arguments?.getSerializable("data") as Lesson
            ExoPlayerActivity.pipStatus = true
            val localData = loadData<List<Lesson>>("unLockList") ?: emptyList()
            val position = localData!!.indexOf(data)
            val videos = ArrayList<String>()
            localData.onEach {
                videos.add(it.video_url)
            }


            val intent = ExoPlayerActivity.newIntent(
                requireContext(),
                data.video_url,
                position, localData.size,
                videos,
                data.name,
                data.name
            )
            startActivity(intent)

        }
        binding.fabBack.setSafeOnClickListener {
            findNavController().popBackStack()
        }

    }


    private fun initDescription() {
        val data = arguments?.getSerializable("data") as Lesson
        binding.poster.loadImage(data.thumbnail)
        binding.itemCompactBannerNoKen.loadImage(data.thumbnail)
        binding.title.text = data.name
        with(binding.layoutDescription) {
            llDescription.visibility = View.VISIBLE
            tvDescription.text = data.description
            fun descriptionOnClickListener() {
                if (tvDescription.maxLines == 3) {
                    tvDescription.maxLines = Int.MAX_VALUE
                    tvShowHideDescription.text =
                        requireContext().getString(R.string.hideDetail)
                } else {
                    tvDescription.maxLines = 3
                    tvShowHideDescription.text = requireContext().getString(R.string.more)
                }
            }
            tvDescription.setOnClickListener { descriptionOnClickListener() }
            tvShowHideDescription.setOnClickListener { descriptionOnClickListener() }
        }

    }
}