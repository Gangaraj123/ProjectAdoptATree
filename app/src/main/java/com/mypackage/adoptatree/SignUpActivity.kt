package com.mypackage.adoptatree

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.mypackage.adoptatree.databinding.ActivitySignUpBinding
import com.mypackage.adoptatree.models.Manager
import com.mypackage.adoptatree.models.User
import com.mypackage.adoptatree.utilities.ImageManager

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var gso: GoogleSignInOptions
    private lateinit var gsignigClient: GoogleSignInClient
    private val REQUEST_ONE_TAP_LOGIN = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createRequest()
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
        val texview = binding.googleSigninBtn.getChildAt(0) as TextView
        texview.text = "Sign In with Google"
        binding.googleSigninBtn.setOnClickListener {
            val signinIntent = gsignigClient.signInIntent
            startActivityForResult(signinIntent, REQUEST_ONE_TAP_LOGIN)
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
                    val user = mAuth.currentUser
                    if (user != null) {
                        user.sendEmailVerification().addOnSuccessListener {
                            Toast.makeText(this, "verification email has sent ", Toast.LENGTH_SHORT)
                                .show()
                        }
                            .addOnFailureListener {
                                Toast.makeText(this, "Invalid Email ", Toast.LENGTH_SHORT).show()
                                Log.d(TAG, "Email sending failure")
                            }
                    }
                    addToDatabase(name, email, mAuth.currentUser?.uid!!, isManager)
                    binding.relativeLayoutLoad2.setBackgroundColor(getColor(R.color.white))
                    binding.relativeLayoutLoad2.visibility = View.VISIBLE
                    binding.loadingSignup.visibility = View.GONE
                    binding.emailSentLayout.visibility = View.VISIBLE
                    binding.mainSignupLayout.visibility = View.GONE
                    Log.d(TAG, "done")
                    mAuth.signOut()
                } else {
                    Log.d(TAG, "Cannot create account!")
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

    private fun createRequest() {
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.my_web_client_ID))
            .requestEmail().build()
        gsignigClient = GoogleSignIn.getClient(this, gso)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ONE_TAP_LOGIN -> {
                binding.relativeLayoutLoad2.visibility = View.VISIBLE
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    Login_with_Google(account);
//                    Toast.makeText(this, "SignIn Success", Toast.LENGTH_SHORT).show()
                } catch (e: ApiException) {
                    e.printStackTrace()
                    Log.d(TAG, "FAiled due to " + e.message)
                    binding.relativeLayoutLoad2.visibility = View.GONE
                    Toast.makeText(this, "SignIn Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun Login_with_Google(account: GoogleSignInAccount?) {
        if (account != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, OnCompleteListener {
                    val user = mAuth.currentUser
                    if (user != null) {
                        addToDatabase(user.displayName!!, user.email!!, user.uid, false)
                        ImageManager.updateTokenInFirebase()
                        startActivity(Intent(this@SignUpActivity, Adopted_trees::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "SignIn Failed", Toast.LENGTH_SHORT).show()
                        binding.relativeLayoutLoad2.visibility = View.GONE
                    }
                })
                .addOnFailureListener {
                    Toast.makeText(this, "SignIn Failed", Toast.LENGTH_SHORT).show()
                    binding.relativeLayoutLoad2.visibility = View.GONE
                }
        }
    }
}