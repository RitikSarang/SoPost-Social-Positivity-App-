package com.example.sopost

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

       /* bottomNavigationView.background = null
        //bottomNavigationView.menu.getItem(2).isEnabled = false

        val navController = findNavController(R.id.homeFragmentNav)
        //set toolbar with name of fragment
        *//*val appBarConfiguration = AppBarConfiguration(R.id.homeFragment,R.id.notificationFragment,R.id.addPostFragment,R.id.notificationFragment,R.id.profileFragment)
        setupActionBarWithNavController(navController,appBarConfiguration)*//*
        bottomNavigationView.setupWithNavController(navController)

        fab.setOnClickListener {
            navController.navigate(R.id.addPostFragment)
        }*/


    }
}