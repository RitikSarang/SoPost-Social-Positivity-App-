package com.example.sopost.fragment

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sopost.Adapter.IPersonalPostAdapter
import com.example.sopost.Adapter.PersonalPostAdapter
import com.example.sopost.Adapter.PostAdapter
import com.example.sopost.OthersProfileFragmentDirections
import com.example.sopost.PostId
import com.example.sopost.R
import com.example.sopost.model.Post
import com.example.sopost.repository.ProfileRepo
import com.example.sopost.viewmodel.ProfileViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.progressBar
import kotlinx.android.synthetic.main.fragment_profile.toolbar
import kotlinx.android.synthetic.main.list_item_post.view.*
import kotlinx.android.synthetic.main.list_profile_toolbar.*
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class ProfileFragment : Fragment(R.layout.fragment_profile), IPersonalPostAdapter {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var viewModel: ProfileViewModel
    private lateinit var adapter: PersonalPostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(ProfileViewModel::class.java)
        viewModel.getLikesCount(auth.currentUser!!.uid)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        setRecyclerView()
        progressBar.isVisible = true

        var name = ""
        var index = 0
        val userDetails = auth.currentUser!!.email
        while (userDetails!!.get(index) != '@') {
            name += userDetails.get(index)
            index++
        }

        txtProfileTitle.text = name

        fetchUserData()
        viewModel.getPostsForFeedCounts()
        viewModel.getFollowersCount()
        viewModel.getFollowingCount()

        btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.editProfileFragment)
        }

        txtFollowersLabel.setOnClickListener {
            findNavController().navigate(R.id.followerContainerFragment)
        }

        txtFollowingLabel.setOnClickListener {
            findNavController().navigate(R.id.followerContainerFragment)
        }

        btn_bottomSheet.setOnClickListener {
            findNavController().navigate(R.id.profileBottomSheetFragment)
        }


        viewModel.likesLiveData.observe(viewLifecycleOwner) { likedCount ->
            Log.d("likesCount", "From fragment${likedCount}")

            /* 0 - Basic badge 0
            *  1 - Diamond badge 1-3 , 0-10
            *  2 - Legendary badge 4-6 , 10-20
            *  3 - Peace badge 7-9 , 20-30
            *  4 - God badge* 10-12 , 30-40 */

            when (likedCount) {
                in 1..2 -> {
                    img_badge.setImageDrawable(
                        ContextCompat.getDrawable(
                            img_badge.context,
                            R.drawable.diamond
                        )
                    )

                }
                in 3..4 -> {
                    img_badge.setImageDrawable(
                        ContextCompat.getDrawable(
                            img_badge.context,
                            R.drawable.legendary
                        )
                    )
                }
                in 5..6 -> {
                    img_badge.setImageDrawable(
                        ContextCompat.getDrawable(
                            img_badge.context,
                            R.drawable.peace
                        )
                    )
                }
                in 31..40 -> {

                    img_badge.setImageDrawable(
                        ContextCompat.getDrawable(
                            img_badge.context,
                            R.drawable.god
                        )
                    )
                }
                else -> {

                    img_badge.setImageDrawable(
                        ContextCompat.getDrawable(
                            img_badge.context,
                            R.drawable.basic
                        )
                    )

                }
            }
        }

        viewModel.countForFeeds.observe(viewLifecycleOwner) {
            txtFeed.text = it.toString()
        }
        viewModel.followersCount.observe(viewLifecycleOwner) {
            txtFollowers.text = it.toString()
        }
        viewModel.followingCount.observe(viewLifecycleOwner) {
            txtFollowing.text = it.toString()
        }
    }

    private fun setRecyclerView() {
        val db = FirebaseFirestore.getInstance()
        val postCollection = db.collection("postsbyuser")

        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val query = postCollection
            .document(uid)
            .collection("1")
            .orderBy("createAt", Query.Direction.DESCENDING)
        val recyclerViewOptions =
            FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()

        adapter = PersonalPostAdapter(recyclerViewOptions, this)

        profile_recyclerView.layoutManager = GridLayoutManager(requireActivity(), 3)
        profile_recyclerView.setHasFixedSize(true)
        profile_recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }


    private fun fetchUserData() {
        viewModel.userDetails.observe(viewLifecycleOwner) {
            txtName.text = it.get("name").toString()
            txtBio.text = it.get("bio").toString()
            txtWebsite.text = it.get("website").toString()
            val fb = FirebaseStorage.getInstance().reference.child(it["imageURL"].toString())

            fb.downloadUrl.addOnSuccessListener { imgURL ->
                Glide
                    .with(requireContext())
                    .load(imgURL.toString())
                    .centerCrop()
                    .placeholder(R.drawable.ic_baseline_person_24)
                    .into(profImg)
                progressBar.isVisible = false
            }
        }
    }

    override fun onPostClicked(uid: String, postID: String) {
        val postId = PostId(postID,uid)
        val actions = ProfileFragmentDirections.actionProfileFragmentToPostDetailFragment(postId)
        findNavController().navigate(actions)
    }

}