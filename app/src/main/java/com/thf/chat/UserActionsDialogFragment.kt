package com.thf.chat

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.thf.chat.databinding.UserActionsDialogFragmentBinding
import com.thf.chat.viewmodel.ChatViewModel

class UserActionsDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            val user = arguments?.getString("user")

            val binding = UserActionsDialogFragmentBinding.inflate(layoutInflater)
            val viewModel: ChatViewModel by viewModels({requireParentFragment()})

            val blockArray = resources.getStringArray(R.array.block)

            binding.blockToggle.apply {
                items = blockArray
                onToggle { which ->

                }
            }

            binding.whisperButton.setOnClickListener {
                user?.let {
                    viewModel.setWhisperingTo(it)
                    dismiss()
                }
            }

            val builder = AlertDialog.Builder(it)
            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}