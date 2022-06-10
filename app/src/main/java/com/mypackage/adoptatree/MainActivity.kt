package com.mypackage.adoptatree

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mypackage.adoptatree.databinding.ActivityMainBinding

// declare common strings here
const val TAG="TAG_123" // used to Log while debugging


class MainActivity : AppCompatActivity() {

    private lateinit var fb: FirebaseAuth
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

        fb = FirebaseAuth.getInstance()
    }


}