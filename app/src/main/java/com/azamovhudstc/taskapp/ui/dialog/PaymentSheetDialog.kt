package com.azamovhudstc.taskapp.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.azamovhudstc.taskapp.data.remote.response.Lesson
import com.azamovhudstc.taskapp.databinding.PaymentSheetDialogBinding
import com.azamovhudstc.taskapp.ui.screens.HomeScreen
import com.azamovhudstc.taskapp.utils.loadData
import com.azamovhudstc.taskapp.utils.saveData
import com.azamovhudstc.taskapp.utils.setSafeOnClickListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PaymentSheetDialog(private val activity: HomeScreen) : BottomSheetDialogFragment() {
    private var _binding: PaymentSheetDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = PaymentSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            paymentApply.setSafeOnClickListener {
                val list = loadData<List<Lesson>>("localData") ?: emptyList()
                saveData("unLockList", list)
                dismiss()
                activity.model.loadHomeData()
            }
            searchFilterCancel.setSafeOnClickListener {
                dismiss()
            }
        }
    }

    companion object {
        fun newInstance(activity: HomeScreen) = PaymentSheetDialog(activity)
    }

}