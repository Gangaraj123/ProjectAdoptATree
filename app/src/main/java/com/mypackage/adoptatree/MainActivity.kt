package com.mypackage.adoptatree

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fb=FirebaseAuth.getInstance()
        setContentView(R.layout.activity_main)
     }
}