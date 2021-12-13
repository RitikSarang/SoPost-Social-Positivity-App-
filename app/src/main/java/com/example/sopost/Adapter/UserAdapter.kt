package com.example.sopost.Adapter

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sopost.R
import com.example.sopost.model.FollowFollowing
import com.example.sopost.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.list_item_post.view.*
import java.io.File

class UserAdapter(
    private var mContext: Context,
    private var mUser: List<User>,
    private var isFragment: Boolean = false,
    private var navController: NavController
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false)

        return UserAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val user = mUser[position]

        holder.itemView.setOnClickListener {
            val pref = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
            with(pref.edit()) {
                putString("profileUID", user.uid)
                    .apply()
            }
            navController.navigate(R.id.othersProfileFragment)
        }

        val fb = FirebaseStorage.getInstance().reference.child(user.imageURL)

        holder.userName.text = user.name
        //Picasso.get().load(fb.toString()).placeholder(R.drawable.signprof).into(holder.img)


        fb.downloadUrl.addOnSuccessListener { imgURL ->

            Glide
                .with(holder.itemView.context)
                .load(imgURL.toString())
                .centerCrop()
                .placeholder(R.drawable.ic_baseline_cloud_upload_24)
                .into(holder.img)

        }
        if (user.uid != "9ZjfJm0S99b0lrX5QNCHmDx9w4B3") {
            holder.followbutton.visibility = View.VISIBLE
            holder.followbutton.setOnClickListener {
                if (holder.followbutton.text.toString().toLowerCase() == "follow") {
                    firebaseUser?.uid?.let {
                        //it is my userid and user.uid is opp uid
                        FirebaseFirestore.getInstance()
                            .collection("Follow").document(it)
                            .collection("Following").document(user.uid)
                            .set(FollowFollowing(user.uid, true))
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(
                                        "search",
                                        "onBindViewHolder: Success(${it})->(${user.uid})"
                                    )
                                    firebaseUser?.uid?.let {
                                        FirebaseFirestore.getInstance()
                                            .collection("Follow").document(user.uid)
                                            .collection("Followers").document(it)
                                            .set(FollowFollowing(user.uid, true))
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    Log.d(
                                                        "search",
                                                        "onBindViewHolder: Success(${user.uid})->(${it})"
                                                    )
                                                    checkFollowingStatus(
                                                        user.uid,
                                                        holder.followbutton
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
                            .collection("Following").document(user.uid)
                            .delete()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("search", "onBindViewHolder: removed")
                                    firebaseUser?.uid?.let {
                                        FirebaseFirestore.getInstance()
                                            .collection("Follow").document(user.uid)
                                            .collection("Followers").document(it)
                                            .delete()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    Log.d(
                                                        "search",
                                                        "onBindViewHolder: removed inside"
                                                    )
                                                    checkFollowingStatus(
                                                        user.uid,
                                                        holder.followbutton
                                                    )
                                                }
                                            }
                                    }
                                }
                            }
                    }
                }
            }
            checkFollowingStatus(user.uid, holder.followbutton)
        } else {
            holder.followbutton.visibility = View.INVISIBLE
        }
    }

    private fun checkFollowingStatus(uid: String, followbutton: Button) {
        firebaseUser?.uid?.let { mineUid ->
            val q = FirebaseFirestore.getInstance()
                .collection("Follow").document(mineUid)
                .collection("Following")

            q.get().addOnSuccessListener {
                it.documents.forEach {
                    Log.d("searchi", "checkFollowingStatus: ${it.data} ${uid}")
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

    override fun getItemCount(): Int {
        return mUser.size
    }


    class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {

        var userName = itemView.findViewById<TextView>(R.id.item_txtName)
        var img = itemView.findViewById<ImageView>(R.id.item_profImg)
        var followbutton = itemView.findViewById<Button>(R.id.item_btnEditProfile)

    }

}