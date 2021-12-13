package com.example.sopost.model

data class Likes(
    val uid: String="",
    val likes: String ="",
    val name : String ="",
    val img_URL:String=""
){
    override fun toString(): String {
        return "$uid $likes"
    }
}