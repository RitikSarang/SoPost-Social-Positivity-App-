package com.example.sopost.repository

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sopost.Adapter.IPostAdapter
import com.example.sopost.Adapter.PostAdapter
import com.example.sopost.model.Likes
import com.example.sopost.model.Post
import com.example.sopost.model.Report
import com.example.sopost.model.User
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

const val TAG = "tesingfsf"

class PostRepo(private val application: Application) {
    private val db = FirebaseFirestore.getInstance()
    private val reportCollection = db.collection("Report")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val postCollection = db.collection("posts")
    private val userCollection = db.collection("users")
    private val postCollectionForUser = db.collection("postsbyuser")
    private val likesCollection = db.collection("Likes")
    val storageReference = FirebaseStorage.getInstance()

    private var _topUsers: MutableLiveData<ArrayList<Likes>> = MutableLiveData()
    var topUsers: LiveData<ArrayList<Likes>> = _topUsers


    fun insertPost(uri: Uri, title: String, desc: String, postCount: String) {
        val formatter = SimpleDateFormat("yyyy_MM_dd_mm_ss", Locale.getDefault())
        val now = Date()
        val fileName = formatter.format(now)

        val currentTime = System.currentTimeMillis()

        val filePath = "postImages/${auth.currentUser!!.uid}/$fileName"
        val storageReference = FirebaseStorage.getInstance()
            .getReference(filePath)

        GlobalScope.launch {
            getUserById(auth.currentUser!!.uid).addOnCompleteListener() {
                if (it.isComplete) {
                    //Log.i("posttesting", " inner - insertPost: ${it.result}")
                    val users = it.result?.toObject(User::class.java)
                    users?.let {
                        storageReference.putFile(uri).addOnSuccessListener {
                            //setimage uri to null
                            storageReference.downloadUrl.addOnSuccessListener { imgURL ->

                                val uniqueID = UUID.randomUUID().toString()
                                val post = Post(
                                    auth.currentUser!!.uid,
                                    uniqueID,
                                    users,
                                    currentTime,
                                    imgURL.toString(),
                                    title,
                                    desc,
                                    filePath
                                )
                                val formatDateForPosts =
                                    SimpleDateFormat("yyyy_MM", Locale.getDefault())
                                val dateForPosts = formatDateForPosts.format(now)


                                postCollection.document(dateForPosts)
                                    .collection(postCount).document(uniqueID).set(post)
                                    .addOnSuccessListener {
                                        postCollectionForUser.document(auth.currentUser!!.uid)
                                            .collection(postCount).document(uniqueID).set(post)
                                            .addOnSuccessListener {

                                                val report = Report(post.uid, post.postId)
                                                reportCollection.document(post.postId).set(report)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(
                                                            application,
                                                            "Post Uploaded Successfully",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }
                                    }


                            }
                        }.addOnFailureListener {
                            Toast.makeText(application, "${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            }
        }

    }

    fun getPosts(home_recyclerView: RecyclerView, fragmentActivity: FragmentActivity) {

    }


    private fun getUserById(uid: String): Task<DocumentSnapshot> {
        return userCollection.document(uid).get()
    }

    fun getPostById(postId: String): Task<DocumentSnapshot> {
        val formatDateForPosts = SimpleDateFormat("yyyy_MM", Locale.getDefault())
        val dateForPosts = formatDateForPosts.format(Date())

        return postCollection.document(dateForPosts)
            .collection("1")
            .document(postId)
            .get()
    }

    fun getPostReportById(postId: String): Task<DocumentSnapshot> {
        val formatDateForPosts = SimpleDateFormat("yyyy_MM", Locale.getDefault())
        val dateForPosts = formatDateForPosts.format(Date())

        return reportCollection.document(postId).get()
    }

    fun setLikePosts(postId: String) {
        val formatDateForPosts = SimpleDateFormat("yyyy_MM", Locale.getDefault())
        val dateForPosts = formatDateForPosts.format(Date())

        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            getPostById(postId).addOnCompleteListener() {
                if (it.isComplete) {
                    val post = it.result?.toObject(Post::class.java)
                    post?.let {

                        val isLiked = post.likedBy.contains(currentUserId)
                        if (isLiked) {
                            post.likedBy.remove(currentUserId)
                            /*Log.i("itemView", "Enter in remove ${currentUserId}: ")
                            Log.i("itemView", "Enter in remove ${post.likedBy.contains(currentUserId)}: ")*/
                        } else {
                            post.likedBy.add(currentUserId)
                            /* Log.i("itemView", "Enter in add ${currentUserId}: ")
                             Log.i("itemView", "Enter in remove ${post.likedBy.contains(currentUserId)}: ")*/
                        }
                        postCollection.document(dateForPosts)
                            .collection("1")
                            .document(postId)
                            .set(post)

                        if (currentUserId == post.uid) {
                            postCollectionForUser.document(currentUserId).collection("1")
                                .document(postId).set(post)
                        } else {
                            postCollectionForUser.document(post.uid).collection("1")
                                .document(postId).set(post)
                        }
                    }
                }
            }
        }
    }

    fun setReport(postId: String) {

        val formatDateForPosts = SimpleDateFormat("yyyy_MM", Locale.getDefault())
        val dateForPosts = formatDateForPosts.format(Date())

        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            getPostById(postId).addOnCompleteListener() {
                if (it.isComplete) {
                    val post = it.result?.toObject(Post::class.java)
                    post?.let {

                        val isReported = it.report.contains(currentUserId)
                        if (currentUserId != it.uid) {
                            if (isReported) {
                                it.report.remove(currentUserId)
                                /*Log.i("itemView", "Enter in remove ${currentUserId}: ")
                            Log.i("itemView", "Enter in remove ${post.likedBy.contains(currentUserId)}: ")*/
                            } else {
                                it.report.add(currentUserId)
                                /* Log.i("itemView", "Enter in add ${currentUserId}: ")
                             Log.i("itemView", "Enter in remove ${post.likedBy.contains(currentUserId)}: ")*/
                            }
                            postCollection.document(dateForPosts)
                                .collection("1")
                                .document(postId)
                                .set(post)

                            if (currentUserId == post.uid) {
                                postCollectionForUser.document(currentUserId).collection("1")
                                    .document(postId).set(post)
                            } else {
                                postCollectionForUser.document(post.uid).collection("1")
                                    .document(postId).set(post)
                            }
                        }
                    }

                }
            }
        }
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            getPostReportById(postId).addOnCompleteListener() {
                if (it.isComplete) {
                    val report = it.result?.toObject(Report::class.java)
                    report?.let { reportImage ->
                        val isReported = reportImage.report.contains(currentUserId)
                        if (currentUserId != reportImage.uid) {
                            if (isReported) {
                                reportImage.report.remove(currentUserId)
                                /*Log.i("itemView", "Enter in remove ${currentUserId}: ")
                            Log.i("itemView", "Enter in remove ${post.likedBy.contains(currentUserId)}: ")*/
                            } else {
                                reportImage.report.add(currentUserId)
                                /* Log.i("itemView", "Enter in add ${currentUserId}: ")
                             Log.i("itemView", "Enter in remove ${post.likedBy.contains(currentUserId)}: ")*/
                            }

                            //write delete post funciton
                            if (reportImage.report.size == 2 || reportImage.report.size > 2) {

                                getPostById(postId).addOnCompleteListener {
                                    if (it.isComplete) {
                                        val post = it.result?.toObject(Post::class.java)
                                        post?.let { postImage ->
                                            deletePost(
                                                reportImage.postId,
                                                reportImage.uid,
                                                postImage.imageStorageRef
                                            )
                                        }
                                    }
                                }
                            }
                            reportCollection.document(postId).set(report)
                        }
                    }
                }
            }
        }
    }


    fun deletePost(postId: String, uid: String, filePath: String) {

        Log.i("postdeleted", "deletePost: $postId $uid")
        val formatDateForPosts = SimpleDateFormat("yyyy_MM", Locale.getDefault())
        val dateForPosts = formatDateForPosts.format(Date())



        postCollection.document(dateForPosts).collection("1")
            .document(postId).delete().addOnCompleteListener {
                if (it.isComplete) {
                    postCollectionForUser.document(uid).collection("1")
                        .document(postId).delete().addOnCompleteListener {
                            storageReference.getReference(filePath).delete().addOnCompleteListener {
                                Toast.makeText(
                                    application,
                                    "Posted has been removed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }


    }

    fun getTopUsers() {
        val db = FirebaseFirestore.getInstance()
        val likesCollection = db.collection("Likes")
        likesCollection.get().addOnSuccessListener {
            for (i in it) {
                val data = i.toObject(
                    Likes::class.java
                )
                if (data.likes.toInt() > 2) {
                    //top users
                    _topUsers.value?.add(data)
                }

            }
        }

    }
}
