package com.example.sopost.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sopost.Adapter.UserAdapter
import com.example.sopost.R
import com.example.sopost.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_search.*


class SearchFragment : Fragment(R.layout.fragment_search) {
    private var userAdapter: UserAdapter? = null
    private var mUser: MutableList<User>? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(recyclerview_search) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            mUser = ArrayList()
            userAdapter = context?.let { UserAdapter(it, mUser as ArrayList<User>, true,findNavController())}
            adapter = userAdapter
        }

        et_search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (et_search.text.toString() == "") {

                } else {
                    recyclerview_search.visibility = View.VISIBLE
                    retreiveUsers(p0)
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }


    private fun retreiveUsers(str: CharSequence?) {
        val userRef = FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("name", str.toString()).get()
        userRef.addOnSuccessListener { documents ->
            mUser?.clear()
            for (document in documents) {

                val name = document.get("name").toString()
                val email = document.get("email").toString()
                val img = document.get("imageURL").toString()
                val bio = document.get("bio").toString()
                val website = document.get("website").toString()

                if(document != null){
                    val user = User(document.id,img,name,email, bio, website)
                    Log.d("profile", "${user.email}")
                    mUser?.add(user)
                }

            }

            userAdapter?.notifyDataSetChanged()

        }.addOnFailureListener { exception ->
            Log.w("profile", "Error getting documents: ", exception)
        }

    }
}
