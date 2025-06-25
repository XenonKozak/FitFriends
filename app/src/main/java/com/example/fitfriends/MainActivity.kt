package com.example.fitfriends

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.fitfriends.databinding.ActivityMainBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Diagnostyka NavHostFragment/NavController
        Log.d("MainActivity", "Przed pobraniem NavHostFragment")
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        Log.d("MainActivity", "NavHostFragment: $navHostFragment")
        val navController = navHostFragment?.navController
        Log.d("MainActivity", "NavController: $navController")
        if (navHostFragment == null || navController == null) {
            Log.e("MainActivity", "NavHostFragment lub NavController jest null!")
        }
        binding.bottomNavigation.setupWithNavController(navController!!)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            navController.navigate(R.id.authFragment)
        } else {
            navController.navigate(R.id.navigation_home)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.authFragment) {
                binding.bottomNavigation.visibility = android.view.View.GONE
            } else {
                binding.bottomNavigation.visibility = android.view.View.VISIBLE
            }
        }
    }
}