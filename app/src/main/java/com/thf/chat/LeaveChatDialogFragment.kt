package com.thf.chat

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.thf.chat.databinding.UserActionsDialogFragmentBinding
import com.thf.chat.model.Blocked
import com.thf.chat.viewmodel.ChatViewModel

class LeaveChatDialogFragment : DialogFragment() {

    var onPositiveButton: () -> Unit = {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder
                .setMessage("Leave chat?")
                .setPositiveButton("Yes") { dialogInterface, i -> onPositiveButton() }
                .setNegativeButton("No") { dialogInterface, i -> }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}