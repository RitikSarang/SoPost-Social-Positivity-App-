package com.example.sopost.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.sopost.R
import com.example.sopost.viewmodel.AuthViewModel
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.android.synthetic.main.fragment_login.*


const val LOGGEDIN = "LOGGEDIN"

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(AuthViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // If the user presses the back button, exit the app
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }

        linearProgress.isVisible = false

        txtSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
        }

        btnLogin.setOnClickListener {
            val email = tfEmail.text.toString().trim()
            val pass = tfPassword.text.toString().trim()

            if(email.contains("@")){
                if (email.isNotEmpty() && pass.isNotEmpty()) {
                    linearProgress.isVisible = true
                    viewModel.signIn(email, pass)
                }
            }else{
                val sendingEmail = "$email@gmail.com"
                if (sendingEmail.isNotEmpty() && pass.isNotEmpty()) {
                    linearProgress.isVisible = true
                    viewModel.signIn(sendingEmail, pass)
                }
            }

        }


        viewModel.loggedStatus.observe(viewLifecycleOwner) {
            if (it) {
                linearProgress.isVisible = false
                findNavController().navigate(R.id.navhomeFragment)
            }else{
                linearProgress.isVisible = false
            }
        }
    }
}