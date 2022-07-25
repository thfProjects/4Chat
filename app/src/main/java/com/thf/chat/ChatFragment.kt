package com.thf.chat

import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.thf.chat.adapter.ChatMessageRecyclerAdapter
import com.thf.chat.databinding.ChatFragmentBinding
import com.thf.chat.viewmodel.ChatViewModel
import com.thf.chat.views.BannerView

class ChatFragment : Fragment() {

    private lateinit var binding: ChatFragmentBinding
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            showLeaveChatDialog()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChatFragmentBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val chatMessageAdapter = ChatMessageRecyclerAdapter(requireActivity())

        chatMessageAdapter.onMessageSenderClick = {
            viewModel.handleMessageSenderClick(it)
        }

        val userAdapter = ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1)

        binding.chatRecycler.apply {
            adapter = chatMessageAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.usersList.apply {
            adapter = userAdapter
            setOnItemClickListener { adapterView, view, i, l ->
                userAdapter.getItem(i)?.let {
                    viewModel.handleUserClick(it)
                }
            }
        }

        binding.rootLayout.addOnClickOutsideViewCallback(binding.newChatButton) {
            viewModel.handleClickOutsideNewChatButton()
        }

        binding.newChatButton.setOnClickListener {
            viewModel.handleNewChatButtonClick()
        }

        binding.sendButton.setOnClickListener {
            viewModel.handleSendButtonClick(binding.messageEditText.text.toString())
            binding.messageEditText.setText("")
        }

        binding.whisperingBanner.onDismiss {
            viewModel.setWhisperingTo(null)
        }

        viewModel.messages.observe (viewLifecycleOwner) {
            chatMessageAdapter.dataset = it
        }

        viewModel.users.observe(viewLifecycleOwner) {
            userAdapter.clear()
            userAdapter.addAll(it)
            userAdapter.notifyDataSetChanged()
        }

        viewModel.newChatButtonClicked.observe(viewLifecycleOwner) {
            if (it) {
                binding.newChatButton.text = "Really?"
            }
            else {
                binding.newChatButton.text = "New Chat"
            }
        }

        viewModel.whisperingTo.observe(viewLifecycleOwner) {
            it?.let {
                binding.drawerLayout.closeDrawer(GravityCompat.END)
                binding.whisperingBanner.apply {
                    text = "Whispering to ${it}"
                    visibility = View.VISIBLE
                }
            }?: run {
                binding.whisperingBanner.apply {
                    visibility = View.GONE
                }
            }
        }

        viewModel.showUserActionDialogCommand.observe(viewLifecycleOwner) {
            showUserActionDialog(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.chat_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.users -> {
                if (!binding.drawerLayout.isDrawerOpen(GravityCompat.END)) binding.drawerLayout.openDrawer(GravityCompat.END)
                else binding.drawerLayout.closeDrawer(GravityCompat.END)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showUserActionDialog(user: String) {
        val dialog = UserActionsDialogFragment()
        val args = bundleOf("user" to user)
        dialog.arguments = args
        dialog.show(childFragmentManager, "userActions")
    }

    private fun showLeaveChatDialog() {
        val dialog = LeaveChatDialogFragment()
        dialog.onPositiveButton = {
            findNavController().popBackStack()
        }
        dialog.show(childFragmentManager, "leave")
    }
}