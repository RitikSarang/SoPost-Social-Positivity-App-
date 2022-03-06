package com.example.sopost.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.sopost.model.User
import com.example.sopost.repository.AuthenticationRepository
import com.example.sopost.repository.ProfileRepo
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private var repository: ProfileRepo = ProfileRepo(application)
    var userDetails: LiveData<MutableMap<String, Any>> = repository.userDetails
    var likesLiveData: LiveData<Int> = repository.likesLiveDataRepo
    var countForFeeds : LiveData<Int> = repository.countForFeeds
    var followersCount : LiveData<Int> = repository.followersCount
    var followingCount : LiveData<Int> = repository.followingCount

    fun updateProfile(
        img: Uri?,
        name: String,
        bio: String,
        website: String,
        navController: NavController,
        context: Context
    ) {
        repository.updateProfile(img, name, bio, website, navController, context)
    }


    fun getPostsForOthers(uid: String, txtFeed: TextView) {
        repository.getPostsForOthers(uid, txtFeed)
    }

    fun getLikesCount(uid: String){
        repository.getLikesCount(uid)
    }

    fun getPostsForFeedCounts(){
        repository.getPostsForFeedCounts()
    }
    fun getOthersPostsForFeedCounts(uid:String){
        repository.getOthersPostsForFeedCounts(uid)
    }

    fun getFollowersCount(){
        repository.getFollowers()
    }
    fun getFollowingCount(){
        repository.getFollowing()
    }
}