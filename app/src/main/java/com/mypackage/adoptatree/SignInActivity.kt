package com.mypackage.adoptatree

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.mypackage.adoptatree.databinding.ActivitySignInBinding
import com.mypackage.adoptatree.databinding.ActivitySignUpBinding

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        if (mAuth.uid != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.textView.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val passwd = binding.passET.text.toString()

            if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                    .matches()
            ) {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (passwd.length < 5) {
                Toast.makeText(
                    this,
                    "Password should be atleast 5 characters long",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            login(email, passwd)
        }
    }

    private fun login(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }
    }
}