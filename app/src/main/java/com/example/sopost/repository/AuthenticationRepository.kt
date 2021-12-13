package com.example.sopost.repository

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import com.example.sopost.R
import com.example.sopost.model.FollowFollowing
import com.example.sopost.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class AuthenticationRepository(private val application: Application) {
    private val firebaseUserMutableLiveData: MutableLiveData<FirebaseUser> = MutableLiveData()
    private val userLoggedMutableLiveData: MutableLiveData<Boolean> = MutableLiveData()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userCollection = db.collection("users")


    fun getFirebaseUserMutableLiveData(): LiveData<FirebaseUser> {
        return firebaseUserMutableLiveData
    }

    fun getUserLoggedMutableLiveData(): LiveData<Boolean> {
        return userLoggedMutableLiveData
    }

    fun register(
        email: String,
        pass: String,
        name: String,
        tag: Uri,
        context: Context,
        navController: NavController
    ) {
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                firebaseUserMutableLiveData.postValue(auth.currentUser)

                val formatter = SimpleDateFormat("yyyy_MM_dd_mm_ss", Locale.getDefault())
                val now = Date()
                val fileName = formatter.format(now)

                val storageReference = FirebaseStorage.getInstance()
                    .getReference("profImages/${task.result!!.user!!.uid}/$fileName")


                storageReference.putFile(tag).addOnSuccessListener {

                        val user = User(
                            task.result!!.user!!.uid,
                            "profImages/${auth.currentUser!!.uid}/$fileName",
                            name,
                            email,
                            bio = "",
                            website = ""
                        )


                        userCollection.document(task.result!!.user!!.uid).set(user)
                        Toast.makeText(
                            context,
                            "Registration Successful",
                            Toast.LENGTH_SHORT
                        ).show()
                        val u = "9ZjfJm0S99b0lrX5QNCHmDx9w4B3"
                        FirebaseFirestore.getInstance()
                            .collection("Follow").document(task.result!!.user!!.uid)
                            .collection("Following").document(u)
                            .set(FollowFollowing(u, true))

                        FirebaseFirestore.getInstance()
                            .collection("Follow").document(u)
                            .collection("Followers").document(user.uid)
                            .set(FollowFollowing(u, true))

                        navController.popBackStack()
                }.addOnFailureListener{

                }


            } else {
                Toast.makeText(application, task.exception?.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun login(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                firebaseUserMutableLiveData.postValue(auth.currentUser)
                userLoggedMutableLiveData.postValue(true)
            } else {
                Toast.makeText(application, task.exception?.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }

    init {
        if (auth.currentUser != null) {
            firebaseUserMutableLiveData.postValue(auth.currentUser)
        }
    }

}
