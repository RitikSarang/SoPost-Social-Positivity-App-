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
    private lateinit var myAdapter: PersonalPostAdapter
    private var currentUserUid = auth.currentUser!!.uid

    private var _likesMutableLiveData: MutableLiveData<Int> = MutableLiveData()
    var likesLiveDataRepo: LiveData<Int> = _likesMutableLiveData

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

    fun getPosts(profileRecyclerview: RecyclerView, txtFeed: TextView) {

        if (postArrayList.isNotEmpty()) {
            postArrayList.clear()
        }
        postCollection
            .document(auth.currentUser!!.uid)
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
                    //profileRecyclerview.adapter = PersonalPostAdapter(postArrayList)
                    //myAdapter.notifyDataSetChanged()
                }

            })


        //another code to get data from firestore but not realTime
        /* postCollection.document(auth.currentUser!!.uid)
             .collection("1").get()
             .addOnSuccessListener(OnSuccessListener {
                 if(!it.isEmpty){
                     for(snap in it.documents){
                         val posts = snap.toObject(Post::class.java)
                         posts?.let {
                             Log.d(TAG, "Current data: ${snap.data}")
                             postArrayList.add(it)
                         }
                     }
                     myAdapter = PersonalPostAdapter(postArrayList)
                     profileRecyclerview.adapter = myAdapter

                 }
             })*/

    }

    fun getPostsForOthers(othersRecyclerview: RecyclerView, uid: String, txtFeed: TextView) {
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
                    //othersRecyclerview.adapter = PersonalPostAdapter(postArrayList)
                    //myAdapter.notifyDataSetChanged()
                }

            })

    }

    private fun getUserById(uid: String): Task<DocumentSnapshot> {
        return userCollection.document(uid).get()
    }

    fun countLikes(uid: String) {
        var likesCount = 0

        postCollection.document(uid).collection("1")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val dataPost = document.toObject(Post::class.java)
                    likesCount += dataPost.likedBy.size
                }
                _likesMutableLiveData.value = likesCount

                getUserById(uid).addOnCompleteListener {
                    if(it.isComplete){
                        val data = it.result?.toObject(User::class.java)
                        data?.let {
                            likesCollection.document(uid)
                                .set(Likes(uid, likesCount.toString(), data.name, data.imageURL))
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("likesCount", "Error getting documents: ", exception)
            }
}

private fun saveLatestLikesCount(likesCount: Int) {
    val pref = application.getSharedPreferences("LIKES", Context.MODE_PRIVATE)
    with(pref.edit()) {
        putInt("likesCount", likesCount)
            .apply()
    }
}
}
