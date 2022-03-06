package com.example.sopost.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.sopost.Adapter.PostAdapter
import com.example.sopost.model.Post
import com.example.sopost.model.Report
import com.example.sopost.model.User
import com.example.sopost.repository.AuthenticationRepository
import com.example.sopost.repository.PostRepo
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


class PostViewModel(application: Application) : AndroidViewModel(application) {
    private var repository: PostRepo = PostRepo(application)
    var reportStatus : LiveData<Report> = repository.reportStatus

    fun insertPost(uri: Uri, title: String, desc: String,postCount:String) {
        return repository.insertPost(uri, title, desc,postCount)
    }

    fun countLikes(uid:String){
        repository.countLikes(uid)
    }
    fun setLikePosts(postId:String){
        repository.setLikePosts(postId)
    }

    fun setReport(postId:String){
        repository.setReport(postId)
    }

    fun getReportTextStatus(postId: String){
        repository.getReportTextStatus(postId)
    }
}