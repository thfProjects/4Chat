package com.thf.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.thf.chat.databinding.HomeFragmentBinding
import com.thf.chat.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private val viewmodel: HomeViewModel by viewModels()
    private lateinit var binding: HomeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.usernameEditText.setText(viewmodel.usernameAttempt)

        binding.startChatButton.setOnClickListener {
            viewmodel.handleStartChatButton(binding.usernameEditText.text.toString())
        }

        viewmodel.invalidUsername.observe(viewLifecycleOwner) { invalid ->
            binding.invalidUsernameTextView.visibility = if (invalid) View.VISIBLE else View.INVISIBLE
        }

        viewmodel.navToChatCommand.observe(viewLifecycleOwner) {
            findNavController().navigate(R.id.action_homeFragment_to_chatFragment)
        }
    }
}