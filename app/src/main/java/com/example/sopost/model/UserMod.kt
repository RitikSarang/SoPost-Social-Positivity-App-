package com.example.sopost.model

data class UserMod(
    val uid: String=""
){
    override fun toString(): String {
        return "$uid"
    }
}