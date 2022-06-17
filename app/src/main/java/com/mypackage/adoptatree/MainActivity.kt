package com.mypackage.adoptatree

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mypackage.adoptatree.databinding.ActivityMainBinding

// declare common strings here
const val TAG = "TAG_123" // used to Log while debugging
const val Trees = "Trees"    // section for storing trees info
const val Registered_trees = "Registered Trees" // trees that are registered and not yet adopted
const val Adopted_trees = "Adopted Trees" // trees that are adopted
const val Tree_details = "Tree details"  // child in a tree node that has tree details
const val Tree_photos_list = "Tree Photos" // child that stores list of photos
const val Tree_question_list_answered =
    "Answered Tree Questions" // child that stores list of queries and answers for that tree
const val Tree_question_list_unanswered =
    "UnAnswered Tree Questions" // child that stores list of queries and answers for that tree
const val Tree_qr_value = "QR Value" // storing QR code value in tree node
const val timestamp = "updated time" // for maintaining time of additions or updations
const val Last_watered_time = "last_watered" // for maintaining last watered time
const val Users="users"
const val User_details="user details"
const val Tree_Nick_Name="Tree Nick Name"
const val Adopted_on="adopted_on"

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