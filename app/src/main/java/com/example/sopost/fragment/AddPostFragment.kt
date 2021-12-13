package com.example.sopost.fragment

import android.app.Activity
import android.content.Context
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
import com.example.sopost.R
import com.example.sopost.createFile
import com.example.sopost.model.Post
import com.example.sopost.viewmodel.PostViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_add_post.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

const val GALLERY_PHOTO_REQUEST2 = 2;
class AddPostFragment : Fragment(R.layout.fragment_add_post) {
    lateinit var viewModel: PostViewModel
    lateinit var imageURI : Uri
    private var count = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(PostViewModel::class.java)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        img_post.setImageResource(R.drawable.ic_baseline_person_24)
        img_post.setOnClickListener {
            pickPhoto()
        }

        img_proceed.setOnClickListener {
            val title = tfTitle.text.toString().trim()
            val desc = tfDescp.text.toString().trim()

            if (title.isNotEmpty()) tlTitle.error = null
            if (desc.isNotEmpty()) tlDescp.error = null

            when {
                img_post.tag == null -> {
                    Toast.makeText(context, "Please select photo", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                title.isEmpty() -> {
                    tlTitle.error = "Please enter title"
                    return@setOnClickListener
                }
                desc.isEmpty() -> {
                    tlDescp.error = "Please enter description"
                    return@setOnClickListener
                }
                else -> {
                    //database entry
                    viewModel.insertPost(img_post.tag as Uri,title,desc,count.toString())
                    findNavController().popBackStack()
                }
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 100 && resultCode == Activity.RESULT_OK){
            imageURI = data?.data!!
            img_post.setImageURI(imageURI)
            img_post.tag = imageURI
        }
    }

    private fun pickPhoto() {
      /*  val intent = Intent()
        intent.type = "images/*"
        intent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(intent,100)
         */
       */
       val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
         startActivityForResult(gallery, 100)
    }
}