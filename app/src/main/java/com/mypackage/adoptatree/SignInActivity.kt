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
import com.mypackage.adoptatree.Maintainance.Manager_Activity
import com.mypackage.adoptatree.User.AdoptedTreesActivity
import com.mypackage.adoptatree.databinding.ActivitySignInBinding
import com.mypackage.adoptatree.models.Manager
import com.mypackage.adoptatree.models.User
import com.mypackage.adoptatree.utilities.ImageManager

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var mAuth: FirebaseAuth
    private val REQUEST_ONE_TAP_LOGIN = 2
    private lateinit var gso: GoogleSignInOptions
    private lateinit var gsignigClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createRequest()

        mAuth = FirebaseAuth.getInstance()

        if (mAuth.uid != null) {
            if(mAuth.currentUser?.email.toString()=="202001107@daiict.ac.in")
            startActivity(Intent(this, Manager_Activity::class.java))
            else
            startActivity(Intent(this, AdoptedTreesActivity::class.java))
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

        val texview = binding.googleSigninBtn.getChildAt(0) as TextView
        texview.text = "Sign In with Google"
        binding.googleSigninBtn.setOnClickListener {
            val signinIntent = gsignigClient.signInIntent
            startActivityForResult(signinIntent, REQUEST_ONE_TAP_LOGIN)
        }
    }

    private fun createRequest() {
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.my_web_client_ID))
            .requestEmail().build()
        gsignigClient = GoogleSignIn.getClient(this, gso)
    }

    private fun login(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if (mAuth.currentUser!!.isEmailVerified) {
                        ImageManager.updateTokenInFirebase()
                        startActivity(Intent(this, AdoptedTreesActivity::class.java))
                        finish()
                    } else {
                        val intent = Intent(this, EmailVerificationActivity::class.java)
                        intent.putExtra("user", mAuth.currentUser)
                        startActivity(intent)
                        mAuth.signOut()
                    }
                } else {
                    Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ONE_TAP_LOGIN -> {
                binding.relativeLayoutLoad.visibility = View.VISIBLE
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    Login_with_Google(account);
                    Toast.makeText(this, "SignIn Success", Toast.LENGTH_SHORT).show()
                } catch (e: ApiException) {
                    e.printStackTrace()
                    binding.relativeLayoutLoad.visibility = View.GONE
                    Log.d(TAG, "FAiled due to " + e.message)
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
                        ImageManager.updateTokenInFirebase() // used for notifications
                        if(mAuth.currentUser?.email.toString()=="202001107@daiict.ac.in")
                            startActivity(Intent(this, Manager_Activity::class.java))
                        else
                            startActivity(Intent(this, AdoptedTreesActivity::class.java))
                        finish()
                    }
                })
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
}