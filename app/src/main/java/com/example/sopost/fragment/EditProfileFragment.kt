package com.example.sopost.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.sopost.R
import com.example.sopost.createFile
import com.example.sopost.viewmodel.ProfileViewModel
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_edit_profile.toolbar
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

const val TAG = "profile"
class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {
    lateinit var viewModel: ProfileViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(ProfileViewModel::class.java)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController,appBarConfiguration)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Log.i(TAG, "onViewCreated: $name, $bio, $website ")
        btnUpdate.setOnClickListener {
            val name = etName.text.toString()
            val bio = etBio.text.toString()
            val website = etWebsite.text.toString()
            var img = editProfImg.tag
            if(editProfImg.tag == null){
                viewModel.updateProfile(null,name,bio,website,findNavController(),requireContext())
            }else{
                viewModel.updateProfile(img as Uri,name,bio,website,findNavController(),requireContext())
                editProfImg.tag = ""
            }
        }

        editProfImg.setImageResource(R.drawable.signprof)
        editProfImg.setOnClickListener {
            editProfImg.tag = ""
            pickPhoto()
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
                                editProfImg.setImageURI(fileUri)
                                editProfImg.tag = fileUri
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