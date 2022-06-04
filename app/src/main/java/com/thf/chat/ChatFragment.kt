package com.thf.chat

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChatFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val chatMessageAdapter = ChatMessageRecyclerAdapter()

        chatMessageAdapter.onMessageSenderClick = {
            if (it != viewModel.username) showUserActionDialog(it)
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
                    if (it != viewModel.username) showUserActionDialog(it)
                }
            }
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
}