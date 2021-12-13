package com.example.sopost

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import kotlinx.android.synthetic.main.fragment_follower_container.*
import kotlinx.android.synthetic.main.list_profile_toolbar.*


class FollowerContainerFragment : Fragment(R.layout.fragment_follower_container) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    var followersCount = 0
    var followingCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fetchFollowersCount()
    }

    private fun fetchFollowersCount() {
        db.collection("Follow").document(auth.currentUser!!.uid)
            .collection("Followers")
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot?> { task ->
                if (task.isSuccessful) {
                    task.result?.let {
                        followersCount = it.documents.size
                        Log.i("followercount", "fetchFollowersCount: ${followersCount}")
                        fetchFollowingCount()
                    }
                } else {
                    //Log.d(TAG, "Error getting documents: ", task.exception)
                }
            })
    }
    private fun fetchFollowingCount() {
        db.collection("Follow").document(auth.currentUser!!.uid)
            .collection("Following")
            .get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot?> { task ->
                if (task.isSuccessful) {
                    task.result?.let {
                        followingCount = it.documents.size
                        Log.i("followercount", "fetchFollowersCount: ${followingCount}")
                        setUpTabBar()
                    }
                } else {
                    //Log.d(TAG, "Error getting documents: ", task.exception)
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
            findNavController().popBackStack()
        }

        var name = ""
        var index = 0
        val userDetails = auth.currentUser!!.email
        while (userDetails!!.get(index) != '@') {
            name += userDetails.get(index)
            index++
        }
        val navController = findNavController()
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        toolbar.setupWithNavController(navController,appBarConfiguration)

        txtProfileTitle.text = name
    }

    private fun setUpTabBar() {
        val adapter = FragmentPagerItemAdapter(
            childFragmentManager,
            FragmentPagerItems.with(activity)
                .add("Followers ${followersCount}", FollowersFragment::class.java)
                .add("Following ${followingCount}",FollowingFragment::class.java)
                .create()
        )

        viewpager.adapter = adapter
        viewpagertab.setViewPager(viewpager)

    }
}