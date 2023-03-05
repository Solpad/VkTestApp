package com.example.vktestapp.screen.login

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vktestapp.MainActivity
import com.example.vktestapp.R
import com.example.vktestapp.databinding.FragmentLoginBinding
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.auth.VKScope
import com.vk.api.sdk.exceptions.VKApiExecutionException
import com.vk.api.sdk.requests.VKRequest
import com.vk.sdk.api.docs.dto.DocsSaveResponse
import com.vk.sdk.api.users.dto.UsersUserXtrCounters

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val mBinding get() = _binding!!
    private lateinit var mViewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        mViewModel = (activity as MainActivity).loginViewModel
        return mBinding.root
    }

    override fun onStart() {
        if (mViewModel.auth) {
            findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
        }

        mBinding.authVkButton.setOnClickListener {
            VK.login(activity as MainActivity, arrayListOf(VKScope.DOCS))
        }

        mBinding.continueButton.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
        }
        super.onStart()
    }
}