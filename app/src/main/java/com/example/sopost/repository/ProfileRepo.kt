package com.example.sopost.repository

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.sopost.Adapter.PersonalPostAdapter
import com.example.sopost.Adapter.PostAdapter
import com.example.sopost.model.Likes
import com.example.sopost.model.Post
import com.example.sopost.model.User
import com.example.sopost.model.UserPostsLikes
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.EventListener


class ProfileRepo(private val application: Application) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")
    private val postCollection = db.collection("postsbyuser")
    private val likesCollection = db.collection("Likes")
    var _userDetails: MutableLiveData<MutableMap<String, Any>> =
        MutableLiveData<MutableMap<String, Any>>()

    val userDetails: LiveData<MutableMap<String, Any>> = _userDetails
    var postArrayList: ArrayList<Post> = ArrayList<Post>()

    private var _likesMutableLiveData: MutableLiveData<Int> = MutableLiveData()
    var likesLiveDataRepo: LiveData<Int> = _likesMutableLiveData

    private var _countForFeeds : MutableLiveData<Int> = MutableLiveData()
    var countForFeeds : LiveData<Int> = _countForFeeds

    private var _followersCount : MutableLiveData<Int> = MutableLiveData()
    var followersCount : LiveData<Int> = _followersCount

    private var _followingCount : MutableLiveData<Int> = MutableLiveData()
    var followingCount : LiveData<Int> = _followingCount

    init {
        getUserDetails()
    }

    private fun getUserDetails() {
        val docRef = userCollection.document(auth.currentUser!!.uid)
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.i("profile", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                _userDetails.value = snapshot.data
            } else {
                Log.i("profile", "Current data: null")
            }
        }
    }

    fun updateProfile(
        img: Uri?,
        name: String,
        bio: String,
        website: String,
        navController: NavController,
        context: Context
    ) {
        // Set the "isCapital" field of the city 'DC'
        val data = userCollection.document(auth.currentUser!!.uid)
        var success = true
        val photoRef = FirebaseStorage.getInstance()
            .getReference(userDetails.value?.get("imageURL").toString())
        //Log.i("profile", "updateProfile:$photoRef")
        if (img != null) {
            //delete image and upload once again

            photoRef.delete()
                .addOnSuccessListener(OnSuccessListener<Void?> { // File deleted successfully
                    val formatter = SimpleDateFormat("yyyy_MM_dd_mm_ss", Locale.getDefault())
                    val now = Date()
                    val fileName = formatter.format(now)

                    val storageReference = FirebaseStorage.getInstance()
                        .getReference("profImages/${auth.currentUser!!.uid}/$fileName")

                    storageReference.putFile(img).addOnSuccessListener {

                        data.update("imageURL", "profImages/${auth.currentUser!!.uid}/$fileName")
                        //Toast.makeText(application, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()

                    }.addOnFailureListener {
                        Toast.makeText(application, "${it.message}", Toast.LENGTH_SHORT).show()
                    }
                    //Log.i("profile", "updateProfile: $storageReference , $photoRef")


                }).addOnFailureListener {
                    Toast.makeText(application, "${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
        if (name.isNotBlank()) {
            data
                .update("name", name)
                .addOnSuccessListener {
                    success = true
                }
                .addOnFailureListener { e ->
                    success = false
                }

        }
        if (bio.isNotBlank()) {
            data
                .update("bio", bio)
                .addOnSuccessListener {
                    success = true
                }
                .addOnFailureListener { e ->
                    success = false
                }
        }
        if (website.isNotBlank()) {
            data
                .update("website", website)
                .addOnSuccessListener {
                    success = true
                }
                .addOnFailureListener { e ->
                    success = false
                }
        }
        if (success) {
            Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            navController.popBackStack()
        } else {
            Toast.makeText(context, "error occurred", Toast.LENGTH_SHORT).show()
        }
    }



    fun getPostsForOthers(uid: String, txtFeed: TextView) {
        if (postArrayList.isNotEmpty()) {
            postArrayList.clear()
        }
        postCollection
            .document(uid)
            .collection("1")
            .addSnapshotListener(object :
                com.google.firebase.firestore.EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e(TAG, "onEvent: ${error.message.toString()}")
                        return
                    }

                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            postArrayList.add(dc.document.toObject(Post::class.java))
                            /* Log.i(TAG, "onEvent: ${postArrayList.size}")
                             Log.i(TAG, "onEvent: ${dc.document.toObject(Post::class.java)}")*/
                        }
                    }
                    txtFeed.text = postArrayList.size.toString()
                }

            })

    }


    fun getPostsForFeedCounts() {

        var count=0
        postCollection
            .document(auth.currentUser!!.uid)
            .collection("1")
            .addSnapshotListener(object :
                com.google.firebase.firestore.EventListener<QuerySnapshot> {
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        //Log.e(com.example.sopost.repository.TAG, "onEvent: ${error.message.toString()}")
                        return
                    }

                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            count++
                        }
                    }
                    _countForFeeds.value = count
                }

            })

    }

    fun getFollowing() {
        val mineUid = auth.currentUser?.uid.toString()
        val collectionReference = FirebaseFirestore.getInstance()
            .collection("Follow").document(mineUid)
            .collection("Following")

        collectionReference.get().addOnSuccessListener {
            _followingCount.value = it.documents.size
        }
    }

    fun getFollowers() {
        val mineUid = auth.currentUser?.uid.toString()
        val collectionReference = FirebaseFirestore.getInstance()
            .collection("Follow").document(mineUid)
            .collection("Followers")

        collectionReference.get().addOnSuccessListener {
            _followersCount.value = it.documents.size
        }
    }
}
