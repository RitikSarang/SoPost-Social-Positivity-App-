package com.example.sopost.fragment

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sopost.Adapter.IPostAdapter
import com.example.sopost.Adapter.PostAdapter
import com.example.sopost.R
import com.example.sopost.model.*
import com.example.sopost.viewmodel.AuthViewModel
import com.example.sopost.viewmodel.PostViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.progressBar
import kotlinx.android.synthetic.main.fragment_home.toolbar
import kotlinx.android.synthetic.main.fragment_profile.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

const val POSTID = "POSTID"

class HomeFragment : Fragment(R.layout.fragment_home), IPostAdapter {
    private lateinit var viewModel: AuthViewModel
    private lateinit var postViewModel: PostViewModel
    lateinit var fabOpen: Animation
    lateinit var fabClose: Animation
    lateinit var rForward: Animation
    lateinit var rBackward: Animation
    var isOpen: Boolean = false
    private lateinit var adapter: PostAdapter

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(AuthViewModel::class.java)

        postViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        ).get(PostViewModel::class.java)

        if (auth.currentUser == null) {
            findNavController().navigate(R.id.loginFragment)
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // If the user presses the back button, exit the app
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val db = FirebaseFirestore.getInstance()
        val likesCollection = db.collection("Likes")

        val query = likesCollection
            .orderBy("likes", Query.Direction.DESCENDING).limit(3)
        val now = Date()
        val formatDateForPosts =
            SimpleDateFormat("yyyy_MM", Locale.getDefault())
        val dateForPosts = formatDateForPosts.format(now)
        query.get().addOnCompleteListener {
            if (it.isComplete) {
                if (!it.result?.isEmpty!!) {

                    val topUser = it.result?.toObjects(Likes::class.java)?.get(0)
                    if (topUser?.uid == uid) {

                        if(topUser.likes != "0") {
                            Log.i("likes", "onViewCreated: I'm Mod")

                            val modCollection = db.collection("Mod")

                            val user = UserMod(uid)
                            modCollection.document(dateForPosts)
                                .collection(uid)
                                .document(uid).set(user)
                        }
                    }
                }
            }
        }


        db.collection("Mod").document(dateForPosts)
            .collection(uid)
            .get().addOnCompleteListener {
                if (it.isComplete) {
                    txtMod.isVisible = !it.result!!.isEmpty
                }
            }


        linearProgressHome.isVisible = false
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val builder = AlertDialog.Builder(context)
            with(builder) {
                setTitle("Exit")
                setMessage("Are you sure you want to exit")
                setPositiveButton("Yes") { _, _ ->
                    requireActivity().finish()
                }
                setNegativeButton("No") { _, _ ->

                    Snackbar.make(
                        view,
                        "Huh! We are glad that you are not leaving us.",
                        Snackbar.LENGTH_SHORT
                    )
                        .setAnimationMode(ANIMATION_MODE_SLIDE)
                        .show()
                }
            }

            builder.show()

        }
        setRecyclerView()
        postViewModel.countLikes(FirebaseAuth.getInstance().currentUser!!.uid)
    }

    private fun setRecyclerView() {
        linearProgressHome.isVisible = true
        val formatDateForPosts = SimpleDateFormat("yyyy_MM", Locale.getDefault())
        val dateForPosts = formatDateForPosts.format(Date())

        val db = FirebaseFirestore.getInstance()
        val postCollection = db.collection("posts")
        val query = postCollection
            .document(dateForPosts)
            .collection("1")
            .orderBy("createAt", Query.Direction.DESCENDING)

        val recyclerViewOptions =
            FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()

        home_recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        home_recyclerView.setHasFixedSize(true)

        adapter = PostAdapter(recyclerViewOptions, this, requireActivity())
        home_recyclerView.adapter = adapter
        val querys = postCollection
            .document(dateForPosts)
            .collection("1")
            .orderBy("createAt", Query.Direction.DESCENDING).get().addOnCompleteListener {
                if (it.isComplete) {
                    linearProgressHome.isVisible = false
                }
            }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        fabOpen = AnimationUtils.loadAnimation(context, R.anim.fab_open)
        fabClose = AnimationUtils.loadAnimation(context, R.anim.fab_close)

        rForward = AnimationUtils.loadAnimation(context, R.anim.rotate_forward)
        rBackward = AnimationUtils.loadAnimation(context, R.anim.rotate_backward)

        fab.setOnClickListener {
            animateFab()
        }

        fabAccount.setOnClickListener {
            animateFab()
            findNavController().navigate(R.id.profileFragment)
        }

        fabAddPost.setOnClickListener {
            animateFab()
            findNavController().navigate(R.id.addPostFragment)
        }

        /*fabFav.setOnClickListener {
            animateFab()
            findNavController().navigate(R.id.favFragment)
        }*/

        toolbar.inflateMenu(R.menu.menu_item)
        toolbar.setOnMenuItemClickListener {
            handleMenuItemClick(it)
        }

        img_search.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }
    }

    private fun animateFab() {
        if (isOpen) {
            fab.startAnimation(rForward)
            fabAccount.startAnimation(fabClose)
            fabAddPost.startAnimation(fabClose)
            //fabFav.startAnimation(fabClose)

            fabAccount.isClickable = false
            fabAddPost.isClickable = false
            //fabFav.isClickable = false
            isOpen = false

        } else {

            fab.startAnimation(rBackward)
            fabAccount.startAnimation(fabOpen)
            fabAddPost.startAnimation(fabOpen)
            //fabFav.startAnimation(fabOpen)

            fabAccount.isClickable = true
            fabAddPost.isClickable = true
            //fabFav.isClickable = true
            isOpen = true

        }
    }

    private fun handleMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.top_list -> {
                findNavController().navigate(R.id.topMonthlyFragment)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onLikeClicked(postId: String) {
        postViewModel.setLikePosts(postId)
    }

    override fun onProfileClicked(uid: String) {
        val pref = requireContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        with(pref.edit()) {
            putString("profileUID", uid)
                .apply()
        }
        findNavController().navigate(R.id.othersProfileFragment)
    }

    override fun onPostClicked(postId: String) {
        //Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show()
        //open pop-up or direct report page
        //Toast.makeText(context, "${postId}", Toast.LENGTH_SHORT).show()
        val bundle = Bundle()
        bundle.putString(POSTID, postId)
        findNavController().navigate(R.id.action_navhomeFragment_to_postBottomSheetFragment, bundle)
    }


    override fun onStart() {
        super.onStart()
        //linearProgressHome.isVisible = true
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        linearProgressHome.isVisible = false
        adapter.stopListening()
    }
}