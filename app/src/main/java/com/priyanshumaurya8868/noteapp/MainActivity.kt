package com.priyanshumaurya8868.noteapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.priyanshumaurya8868.noteapp.databinding.ActivityMainBinding
import com.priyanshumaurya8868.noteapp.utils.LoadingState

class MainActivity : AppCompatActivity() {
    lateinit var viewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
      val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val  fAuth = FirebaseAuth.getInstance()
        if (fAuth.currentUser == null){
            navController.popBackStack()
            navController.navigate(R.id.action_global_login)
        }

    }

}