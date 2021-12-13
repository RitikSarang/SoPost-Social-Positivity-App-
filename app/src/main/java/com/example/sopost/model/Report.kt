package com.example.sopost.model

data class Report(
    val uid: String="",
    val postId : String="",
    val report: ArrayList<String> = ArrayList()
){
    override fun toString(): String {
        return "$uid $postId ${report.size}"
    }
}