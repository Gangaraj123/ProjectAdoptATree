package com.mypackage.adoptatree

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.mypackage.adoptatree.databinding.ActivityMainBinding
import com.mypackage.adoptatree.databinding.ActivitySignUpBinding
import com.mypackage.adoptatree.models.Manager
import com.mypackage.adoptatree.models.User

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        //Sign Up Button listener
        binding.button.setOnClickListener {
            val name = binding.nameEt.text.toString()
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            val confirmPass = binding.confirmPassEt.text.toString()
            val isManager = binding.jobSwitch.isChecked

            if (checkEmailAndPassword(name, email, pass, confirmPass)) {
                signUp(name, email, pass, isManager)
            }
        }

        //Sign In Instead
        binding.textView.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    private fun signUp(name: String, email: String, pass: String, isManager: Boolean) {
        mAuth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    addToDatabase(name, email, mAuth.currentUser?.uid!!, isManager)
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Log.d("SignUpError!", "Cannot create account!")
                    Toast.makeText(
                        this, "Email already exists", Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun addToDatabase(name: String, email: String, uid: String, isManager: Boolean) {
        val mDbRef = FirebaseDatabase.getInstance().reference
        if (isManager) {
            //store the manager's name, email
            mDbRef.child("managers").child(uid).child("manager_details")
                .setValue(User(name, email, uid))
        } else {
            //store the user's name, email
            mDbRef.child("users").child(uid).child("user_details")
                .setValue(Manager(name, email, uid))
        }
    }

    private fun checkEmailAndPassword(
        name: String,
        email: String,
        pass: String,
        confirmPass: String
    ): Boolean {
        if (name.length < 3) {
            Toast.makeText(this, "Name should have atleast 3 characters", Toast.LENGTH_SHORT).show()
            return false
        }

        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                .matches()
        ) {
            Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show()
            return false
        }
        if (pass != confirmPass) {
            Toast.makeText(this, "Password and Confirm Password are not same", Toast.LENGTH_SHORT)
                .show()
            return false
        }
        if (pass.length < 5) {
            Toast.makeText(
                this,
                "Password should be atleast 5 characters long",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }
}