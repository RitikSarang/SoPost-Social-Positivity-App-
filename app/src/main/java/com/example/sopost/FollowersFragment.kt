package com.example.sopost

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sopost.Adapter.UserAdapter
import com.example.sopost.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_followers.*


class FollowersFragment : Fragment(R.layout.fragment_followers) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")
    private var userAdapter: UserAdapter? = null
    private var mUser: MutableList<User>? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchFolloweresUID()

        with(recyclerview_followers) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            mUser = ArrayList()
            userAdapter = context?.let { UserAdapter(it, mUser as ArrayList<User>, true,findNavController()) }
            adapter = userAdapter
        }



    }

    private fun fetchFolloweresUID() {
        val uid = auth.currentUser!!.uid
        mUser?.clear()
        FirebaseFirestore.getInstance().collection("Follow")
            .document(uid)
            .collection("Followers").get()
            .addOnSuccessListener { result ->
                if (result != null) {
                    for (document in result) {
                        if (document != null) {
                            fetchFollowersData(document.id.toString())
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                //Log.d(TAG, "Error getting documents: ", exception)
            }
    }
    private fun fetchFollowersData(uid: String) {
        userCollection.document(uid).get()
            .addOnSuccessListener { document ->

                val name = document.get("name").toString()
                val email = document.get("email").toString()
                val img = document.get("imageURL").toString()
                val bio = document.get("bio").toString()
                val website = document.get("website").toString()

                if (document != null) {
                    val user = User(document.id, img, name, email, bio, website)
                    //Log.d("followers", "${user.email}")
                    mUser?.add(user)
                    //Log.d("followers", "${mUser}")
                }
                userAdapter?.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                //Log.d(TAG, "get failed with ", exception)
            }
    }
}