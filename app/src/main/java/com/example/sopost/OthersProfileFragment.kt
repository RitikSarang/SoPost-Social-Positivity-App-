package com.example.sopost

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.sopost.model.FollowFollowing
import com.example.sopost.viewmodel.ProfileViewModel
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_others_profile.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.profImg
import kotlinx.android.synthetic.main.fragment_profile.progressBar
import kotlinx.android.synthetic.main.fragment_profile.toolbar
import kotlinx.android.synthetic.main.fragment_profile.txtBio
import kotlinx.android.synthetic.main.fragment_profile.txtFollowers
import kotlinx.android.synthetic.main.fragment_profile.txtFollowing
import kotlinx.android.synthetic.main.fragment_profile.txtName
import kotlinx.android.synthetic.main.fragment_profile.txtWebsite
import kotlinx.android.synthetic.main.list_profile_toolbar.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


class OthersProfileFragment : Fragment(R.layout.fragment_others_profile) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack(R.id.navhomeFragment, false)
        }


        val pref = requireActivity().getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val uid = pref.getString("profileUID", null)

        setUpRecyclerView(uid)
        progressBar.isVisible = true

        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController, appBarConfiguration)


        uid?.let {
            fetchUserData(uid)
            getFollowers(uid)
            getFollowing(uid)

            checkFollowingStatus(uid, followbutton)
            if (uid != "9ZjfJm0S99b0lrX5QNCHmDx9w4B3") {
                followbutton.visibility = View.VISIBLE
                followbutton.setOnClickListener {
                    if (followbutton.text.toString().toLowerCase() == "follow") {
                        firebaseUser?.uid?.let {
                            //it is my userid and user.uid is opp uid
                            FirebaseFirestore.getInstance()
                                .collection("Follow").document(it)
                                .collection("Following").document(uid)
                                .set(FollowFollowing(uid, true))
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d(
                                            "search",
                                            "onBindViewHolder: Success(${it})->(${uid})"
                                        )
                                        firebaseUser?.uid?.let {
                                            FirebaseFirestore.getInstance()
                                                .collection("Follow").document(uid)
                                                .collection("Followers").document(it)
                                                .set(FollowFollowing(uid, true))
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        Log.d(
                                                            "search",
                                                            "onBindViewHolder: Success(${uid})->(${it})"
                                                        )
                                                        checkFollowingStatus(
                                                            uid,
                                                            followbutton
                                                        )
                                                    }
                                                }
                                        }
                                    }
                                }
                        }
                    } else {
                        firebaseUser?.uid?.let {
                            FirebaseFirestore.getInstance()
                                .collection("Follow").document(it)
                                .collection("Following").document(uid)
                                .delete()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d("search", "onBindViewHolder: removed")
                                        firebaseUser?.uid?.let {
                                            FirebaseFirestore.getInstance()
                                                .collection("Follow").document(uid)
                                                .collection("Followers").document(it)
                                                .delete()
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        Log.d(
                                                            "search",
                                                            "onBindViewHolder: removed inside"
                                                        )
                                                        checkFollowingStatus(
                                                            uid,
                                                            followbutton
                                                        )
                                                    }
                                                }
                                        }
                                    }
                                }
                        }
                    }
                }
                checkFollowingStatus(uid, followbutton)
            } else {
                followbutton.visibility = View.INVISIBLE
            }
        }
        viewModel.likesLiveData.observe(viewLifecycleOwner) { likedCount ->
            Log.d("likesCount", "From fragment Others${likedCount}")

            /* 0 - Basic badge 0
            *  1 - Diamond badge 1-3 , 0-10
            *  2 - Legendary badge 4-6 , 10-20
            *  3 - Peace badge 7-9 , 20-30
            *  4 - God badge* 10-12 , 30-40 */

            when (likedCount) {
                in 1..2 -> {
                    img_badgeOthers.setImageDrawable(
                        ContextCompat.getDrawable(
                            img_badgeOthers.context,
                            R.drawable.diamond
                        )
                    )

                }
                in 3..4 -> {
                    img_badgeOthers.setImageDrawable(
                        ContextCompat.getDrawable(
                            img_badgeOthers.context,
                            R.drawable.legendary
                        )
                    )
                }
                in 5..6 -> {
                    img_badgeOthers.setImageDrawable(
                        ContextCompat.getDrawable(
                            img_badgeOthers.context,
                            R.drawable.peace
                        )
                    )
                }
                in 31..40 -> {

                    img_badgeOthers.setImageDrawable(
                        ContextCompat.getDrawable(
                            img_badgeOthers.context,
                            R.drawable.god
                        )
                    )
                }
                else -> {
                    img_badgeOthers.setImageDrawable(
                        ContextCompat.getDrawable(
                            img_badgeOthers.context,
                            R.drawable.basic
                        )
                    )

                }
            }
        }
    }

    private fun setUpRecyclerView(uid: String?) {
        uid?.let {
            others_recyclerView.layoutManager = GridLayoutManager(requireActivity(), 3)
            others_recyclerView.setHasFixedSize(true)
            //viewModel.countLikes(uid)
            viewModel.getPostsForOthers(uid, txtFeedOthers)
        }
    }


    private fun getFollowing(uid: String) {
        val mineUid = uid
        val q = FirebaseFirestore.getInstance()
            .collection("Follow").document(mineUid)
            .collection("Following")

        q.get().addOnSuccessListener {
            txtFollowing.text = it.documents.size.toString()
        }
    }

    private fun getFollowers(uid: String) {
        val mineUid = uid
        val q = FirebaseFirestore.getInstance()
            .collection("Follow").document(mineUid)
            .collection("Followers")

        q.get().addOnSuccessListener {
            txtFollowers.text = it.documents.size.toString()
        }
    }

    private fun fetchUserData(uid: String) {
        uid.let {
            FirebaseFirestore.getInstance().collection("users").document(it)
                .get()
                .addOnSuccessListener {

                    var name = ""
                    var index = 0
                    val userDetails = it.get("email").toString()
                    while (userDetails.get(index) != '@') {
                        name += userDetails.get(index)
                        index++
                    }

                    txtProfileTitle.text = name

                    txtName.text = it.get("name").toString()
                    txtBio.text = it.get("bio").toString()
                    txtWebsite.text = it.get("website").toString()
                    val fb =
                        FirebaseStorage.getInstance().reference.child(it["imageURL"].toString())

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
    }

    private fun checkFollowingStatus(uid: String, followbutton: Button) {
        firebaseUser?.uid?.let { mineUid ->
            val q = FirebaseFirestore.getInstance()
                .collection("Follow").document(mineUid)
                .collection("Following")

            q.get().addOnSuccessListener {
                it.documents.forEach {
                    //Log.d("searchi", "checkFollowingStatus: ${it.data} ${uid}")
                    val va = it.data?.get("uid")
                    if (va == uid) {
                        followbutton.text = "Following"
                        return@addOnSuccessListener
                    }
                }
            }
        }

        followbutton.text = "Follow"
    }
}
