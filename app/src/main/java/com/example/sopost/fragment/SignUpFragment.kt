package com.example.sopost.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.android.synthetic.main.fragment_sign_up.backButton
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import com.example.sopost.viewmodel.AuthViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.sopost.R
import com.example.sopost.createFile
import com.google.firebase.auth.FirebaseAuth
import java.util.*

const val GALLERY_PHOTO_REQUEST = 1;

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {
    lateinit var viewModel: AuthViewModel
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(AuthViewModel::class.java)


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        img_photo.setImageResource(R.drawable.signprof)
        img_photo.setOnClickListener {
            pickPhoto()
        }

        btnRegister.setOnClickListener {

            val email = tfEmailSignUp.text.toString().trim()
            val password = tfPasswordSignUp.text.toString().trim()
            val name = tfName.text.toString().trim()

            if (name.isNotEmpty()) tlName.error = null
            if (email.isNotEmpty()) tlEmailSignUp.error = null
            if (password.isNotEmpty()) tlPasswordSignUp.error = null

            if (name.isEmpty()) {
                tlName.error = "Please fill name"
                return@setOnClickListener
            } else if (img_photo.tag == null) {
                Toast.makeText(context, "Please select photo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tlEmailSignUp.error = "Invalid Email"
                return@setOnClickListener
            } else if (password.isEmpty()) {
                tlPasswordSignUp.error = "Please fill password"
                return@setOnClickListener
            } else {

                viewModel.register(
                    email,
                    password,
                    name,
                    img_photo.tag as Uri,
                    requireContext(),
                    findNavController()
                )

            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_PHOTO_REQUEST -> {
                    data?.data?.also { uri ->
                        val photoFile: File? = try {
                            createFile(requireActivity(), Environment.DIRECTORY_PICTURES, "jpg")
                        } catch (e: IOException) {
                            Toast.makeText(
                                context,
                                "Error occured while creating file : ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            null
                        }

                        photoFile?.also {
                            try {
                                val resolver = requireActivity().applicationContext.contentResolver
                                resolver.openInputStream(uri).use { stream ->
                                    val output = FileOutputStream(photoFile)
                                    stream!!.copyTo(output)
                                }

                                val fileUri = Uri.fromFile(photoFile)
                                img_photo.setImageURI(fileUri)
                                img_photo.tag = fileUri
                            } catch (e: FileNotFoundException) {
                                e.printStackTrace()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun pickPhoto() {
        val pickPhotoRequest =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickPhotoRequest.resolveActivity(requireActivity().packageManager)?.also {
            startActivityForResult(pickPhotoRequest, GALLERY_PHOTO_REQUEST)
        }
    }
}