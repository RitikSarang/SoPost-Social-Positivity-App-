package com.example.sopost

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sopost.Adapter.PostAdapter
import com.example.sopost.Adapter.TopUserAdapter
import com.example.sopost.model.Likes
import com.example.sopost.model.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.home_recyclerView
import kotlinx.android.synthetic.main.fragment_top_monthly.*
import java.text.SimpleDateFormat
import java.util.*


class TopMonthlyFragment : Fragment(R.layout.fragment_top_monthly) {
    private lateinit var adapter: TopUserAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecyclerView()
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }




    private fun setRecyclerView() {

        val db = FirebaseFirestore.getInstance()
        val likesCollection = db.collection("Likes")

        val query = likesCollection
            .orderBy("likes", Query.Direction.DESCENDING)
        val recyclerViewOptions =
            FirestoreRecyclerOptions.Builder<Likes>().setQuery(query, Likes::class.java).build()

        adapter = TopUserAdapter(recyclerViewOptions)

        topusers_recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        topusers_recyclerView.setHasFixedSize(true)
        topusers_recyclerView.adapter = adapter


    }
}