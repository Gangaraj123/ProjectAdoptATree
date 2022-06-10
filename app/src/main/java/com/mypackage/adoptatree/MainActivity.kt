package com.mypackage.adoptatree

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.mypackage.adoptatree.Maintainance.QRCodeScanner

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fb=FirebaseAuth.getInstance()
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.scan_btn).setOnClickListener(View.OnClickListener {
            startActivity(Intent(this,QRCodeScanner::class.java))
        })
     }
}