package com.thf.chat

import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.thf.chat.databinding.UserActionsDialogFragmentBinding
import com.thf.chat.model.Blocked
import com.thf.chat.viewmodel.ChatViewModel

class UserActionsDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            arguments?.getString("user")?.let { user ->

                val binding = UserActionsDialogFragmentBinding.inflate(layoutInflater)
                val viewModel: ChatViewModel by viewModels({requireParentFragment()})

                val blockArray = Blocked.values()

                binding.blockToggle.apply {
                    items = blockArray
                    setSelected(viewModel.getBlocked(user))
                    onToggle { which ->
                        viewModel.setBlocked(user, blockArray[which])
                    }
                }

                binding.whisperButton.setOnClickListener {
                    viewModel.setWhisperingTo(user)
                    dismiss()
                }

                val titleView = layoutInflater.inflate(R.layout.alert_dialog_title, null)
                titleView.findViewById<TextView>(R.id.dialogTitle).text = user

                val builder = AlertDialog.Builder(it)
                builder.setView(binding.root).setCustomTitle(titleView)
                builder.create()
            } ?: throw Exception("User cannot be null")
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}