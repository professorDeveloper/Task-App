package com.azamovhudstc.taskapp.ui.adapter

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.azamovhudstc.taskapp.R
import com.azamovhudstc.taskapp.data.remote.response.Lesson
import com.azamovhudstc.taskapp.databinding.CourseItemBinding
import com.azamovhudstc.taskapp.databinding.ItmeCourseLargeBinding
import com.azamovhudstc.taskapp.ui.dialog.PaymentSheetDialog
import com.azamovhudstc.taskapp.ui.screens.HomeScreen
import com.azamovhudstc.taskapp.utils.*
import com.bumptech.glide.Glide

class HomeItemAdapter(
    var type: Int,
    private val mediaList: MutableList<Lesson>?,
    private val activity: HomeScreen,
    private val matchParent: Boolean = false,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val uiSettings =
        loadData<UISettings>("ui_settings") ?: UISettings()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (type) {
            0 -> MediaViewHolder(
                CourseItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            1 -> MediaLargeViewHolder(
                ItmeCourseLargeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException()
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (type) {
            0 -> {

                val b = (holder as MediaViewHolder).binding
                setAnimation(activity.requireActivity(), b.root, uiSettings)
                val media = mediaList?.getOrNull(position)
                if (media != null) {
                    holder.binding.apply {
                        itemImg.loadImage(media.thumbnail)
                        titleItem.text = media.name
                    }
                    if (!media.checkUnLock()) {
                        b.unlockBg.visible()
                    } else {
                        b.unlockBg.gone()
                    }
                }
            }
            1 -> {
                val b = (holder as MediaLargeViewHolder).binding
                setAnimation(activity.requireActivity(), b.root, uiSettings)
                val media = mediaList?.getOrNull(position)
                if (media != null) {
                    b.apply {
                        Glide.with(b.itemEpisodeImage).load(media.thumbnail).override(400, 0)
                            .into(b.itemEpisodeImage)
                        b.description.text = media.description
                        b.title.text = media.name
                    }
                    handleProgress(
                        b.itemEpisodeProgressCont,
                        b.itemEpisodeProgress,
                        b.itemEpisodeProgressEmpty,
                        media.id,
                    )
                    if (!media.checkUnLock()) {
                        b.itemEpisodeViewed.visible()
                    } else {
                        b.itemEpisodeViewed.gone()
                    }
                }
            }
        }
    }

    override fun getItemCount() = mediaList!!.size

    override fun getItemViewType(position: Int): Int {
        return type
    }


    fun Lesson.checkUnLock(): Boolean {
        val checkStatus = loadData<List<Lesson>>("unLockList") ?: emptyList()

        return checkStatus.contains(this)
    }

    inner class MediaViewHolder(val binding: CourseItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            if (matchParent) itemView.updateLayoutParams { width = -1 }
            itemView.setSafeOnClickListener {
                if (mediaList!!.get(bindingAdapterPosition).checkUnLock()) {
                    clicked(bindingAdapterPosition)
                } else {
                    lockClicked(bindingAdapterPosition)
                }
            }
        }
    }

    inner class MediaLargeViewHolder(val binding: ItmeCourseLargeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setSafeOnClickListener {
                if (mediaList!!.get(bindingAdapterPosition).checkUnLock()) {
                    clicked(bindingAdapterPosition)
                } else {
                    lockClicked(bindingAdapterPosition)
                }
            }
        }
    }

    private fun handleProgress(cont: LinearLayout, bar: View, empty: View, mediaId: Int) {
        val curr = loadData<Long>("${mediaId}_")
        val max = loadData<Long>("${mediaId}_max")
        if (curr != null && max != null) {
            cont.visibility = View.VISIBLE
            val div = curr.toFloat() / max.toFloat()
            val barParams = bar.layoutParams as LinearLayout.LayoutParams
            barParams.weight = div
            bar.layoutParams = barParams
            val params = empty.layoutParams as LinearLayout.LayoutParams
            params.weight = 1 - div
            empty.layoutParams = params
        } else {
            cont.visibility = View.GONE
        }
    }

    fun clicked(position: Int) {
        if ((mediaList?.size ?: 0) > position && position != -1) {
            val media = mediaList?.getOrNull(position)
            if (media != null) {
                val bundle = Bundle()
                bundle.putSerializable("data", media)
                activity.findNavController()
                    .navigate(R.id.action_homeScreen_to_detailScreen, bundle)
            }
        }
    }

    fun lockClicked(position: Int) {
        if ((mediaList?.size ?: 0) > position && position != -1) {
            val media = mediaList?.getOrNull(position)
            if (media != null) {
                PaymentSheetDialog.newInstance(activity)
                    .show(activity.parentFragmentManager, "dialog")

            }
        }
    }
}