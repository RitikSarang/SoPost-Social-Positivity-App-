package com.example.sopost.model

data class Post(
    val uid: String="",
    val postId : String="",
    val user : User = User(),
    val createAt : Long = 0L,
    val imageURL: String="",
    val title: String="",
    val desc: String="",
    val imageStorageRef : String="",
    val report: ArrayList<String> = ArrayList(),
    val likedBy : ArrayList<String> = ArrayList()
){
    override fun toString(): String {
        return "$uid $imageURL $title $desc"
    }
}