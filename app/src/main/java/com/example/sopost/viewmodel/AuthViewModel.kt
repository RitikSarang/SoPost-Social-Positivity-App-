package com.example.sopost.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.sopost.model.User
import com.example.sopost.repository.AuthenticationRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File


class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private var repository: AuthenticationRepository = AuthenticationRepository(application)
    private var userData: LiveData<FirebaseUser> = repository.getFirebaseUserMutableLiveData()
    private var loggedStatus: LiveData<Boolean> = repository.getUserLoggedMutableLiveData()

    fun getUserData(): LiveData<FirebaseUser> {
        return userData
    }

    fun getLoggedStatus(): LiveData<Boolean> {
        return loggedStatus
    }

    fun register(
        email: String,
        pass: String,
        name:String,
        tag: Uri,
        context: Context,
        navController: NavController
    ) {
        repository.register(email, pass,name,tag, context, navController)
    }

    fun signIn(email: String, pass: String) {
        repository.login(email, pass)
    }

    fun signOut() {
        repository.signOut()
    }

}