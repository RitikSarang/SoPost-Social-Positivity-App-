package com.example.sopost.model

data class User(
    val uid: String="",
    val imageURL: String="",
    val name: String="",
    val email: String="",
    val bio:String="",
    val website:String=""
){
    override fun toString(): String {
        return "$uid $imageURL $name $email $bio $website"
    }
}