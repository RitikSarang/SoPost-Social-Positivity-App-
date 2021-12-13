package com.example.sopost.model

data class UserPostsLikes(
    val uid: String="",
    val prevLikes : String="",
    val currLikes : String=""
){
    override fun toString(): String {
        return "$uid $prevLikes $currLikes"
    }
}