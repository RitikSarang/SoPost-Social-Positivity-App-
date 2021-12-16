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
    private val postCollection = db.collection("posts")
    private val reportCollection = db.collection("Report")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userCollection = db.collection("users")
    private val postCollectionForUser = db.collection("postsbyuser")
    val storageReference = FirebaseStorage.getInstance()



    fun countLikes(uid: String) {
        var likesCount = 0
        val db = FirebaseFirestore.getInstance()
        val postCollection = db.collection("postsbyuser")
        val likesCollection = db.collection("Likes")

        postCollection.document(uid).collection("1")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val dataPost = document.toObject(Post::class.java)
                    likesCount += dataPost.likedBy.size
                }
                getUserById(uid).addOnCompleteListener {
                    if (it.isComplete) {
                        val data = it.result?.toObject(User::class.java)
                        data?.let {
                            likesCollection.document(uid)
                                .set(Likes(uid, likesCount.toString(), data.name, data.imageURL))
                        }
                    }
                }

            }
            .addOnFailureListener { exception ->
                //Log.d("likesCount", "Error getting documents: ", exception)
            }
    }

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


    private fun getUserById(uid: String): Task<DocumentSnapshot> {
        return userCollection.document(uid).get()
    }


    private fun getPostById(postId: String): Task<DocumentSnapshot> {
        val formatDateForPosts = SimpleDateFormat("yyyy_MM", Locale.getDefault())
        val dateForPosts = formatDateForPosts.format(Date())

        return postCollection.document(dateForPosts)
            .collection("1")
            .document(postId)
            .get()
    }

    private fun getPostReportById(postId: String): Task<DocumentSnapshot> {
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
                            countLikes(currentUserId)
                        } else {
                            postCollectionForUser.document(post.uid).collection("1")
                                .document(postId).set(post)
                            countLikes(post.uid)
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


    private fun deletePost(postId: String, uid: String, filePath: String) {

       //Log.i("postdeleted", "deletePost: $postId $uid")
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

}
