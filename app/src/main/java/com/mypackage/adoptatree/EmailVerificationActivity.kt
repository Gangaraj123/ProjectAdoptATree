package com.mypackage.adoptatree

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mypackage.adoptatree.databinding.ActivityEmailVerificationBinding

class EmailVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailVerificationBinding
    private lateinit var fAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEmailVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val user: FirebaseUser = intent.extras?.get("user") as FirebaseUser

        binding.sendEmailButton.setOnClickListener {

            user.sendEmailVerification()
                .addOnSuccessListener {
                    Toast.makeText(this, "Verification email sent", Toast.LENGTH_SHORT).show()
                    binding.sendEmailButton.text = "Done"
                    binding.sendEmailButton.setOnClickListener {
                        finish()
                    }
                }
                .addOnFailureListener {
                    Log.d(TAG, "Failed to send email!")
                }
        }

    }
}