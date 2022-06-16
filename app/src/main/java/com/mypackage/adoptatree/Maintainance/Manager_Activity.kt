package com.mypackage.adoptatree.Maintainance

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView
import com.google.firebase.database.*
import com.google.firebase.database.core.utilities.Tree
import com.mypackage.adoptatree.*
import com.mypackage.adoptatree.Maintainance.Update.Update_Activity
import com.mypackage.adoptatree.R

class Manager_Activity : AppCompatActivity() {
    private lateinit var btn_generate_barcode: MaterialCardView
    private lateinit var btn_register_tree: MaterialCardView
    private lateinit var btn_update_tree_status: Button
    private lateinit var btn_water_tree: MaterialCardView
    private lateinit var mdbRef: DatabaseReference
    private lateinit var main_linear_layout: LinearLayout
    private lateinit var verifying_layout: LinearLayout
    private lateinit var success_btn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager)

        mdbRef = FirebaseDatabase.getInstance().reference
        main_linear_layout = findViewById(R.id.main_layout)
        verifying_layout = findViewById(R.id.verifying_qr_code)
        btn_generate_barcode = findViewById(R.id.btn_generate_bar_code)
        btn_register_tree = findViewById(R.id.btn_register_tree)
        btn_update_tree_status = findViewById(R.id.btn_update_status)
        btn_water_tree = findViewById(R.id.btn_water_tree)
        success_btn = findViewById(R.id.success_button)
        findViewById<ImageButton>(R.id.imgbtn_generate_barcode).isEnabled = false
        findViewById<ImageButton>(R.id.imgbtn_water_Tree).isEnabled = false
        findViewById<ImageButton>(R.id.imgbtn_register_tree).isEnabled = false
        btn_generate_barcode.setOnClickListener {
            startActivity(Intent(this, QRCodeGenerator::class.java))
            overridePendingTransition(
                R.anim.animate_slide_left_enter,
                R.anim.animate_slide_left_exit
            )
        }
        btn_register_tree.setOnClickListener {
            startActivity(Intent(this, Register_Tree::class.java))
        }
        btn_update_tree_status.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, QRCodeScanner::class.java)
            update_tree_result_launcher.launch(intent)
        })
        btn_water_tree.setOnClickListener {

            val intent = Intent(this, QRCodeScanner::class.java)
            water_tree_result_launcher.launch(intent)
            Log.d(TAG, "started activity")
        }
        success_btn.setOnClickListener {
            main_linear_layout.visibility = View.VISIBLE
            verifying_layout.visibility = View.GONE
        }
    }


    private var water_tree_result_launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val qr_result = result.data?.getStringExtra("scan_result")
                main_linear_layout.visibility = View.GONE
                verifying_layout.visibility = View.VISIBLE
                mdbRef.child(Trees).child(Adopted_trees).orderByChild(Tree_qr_value)
                    .equalTo(qr_result).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                val key = snapshot.children.elementAt(0).key!!
                                Log.d(TAG, "Key = " + key.toString())
                                Update_watered_time(key, true)
                            } else {
                                // search in registered trees
                                mdbRef.child(Trees).child(Registered_trees).orderByChild(
                                    Tree_qr_value
                                ).equalTo(qr_result)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists()) {
                                                val key = snapshot.children.elementAt(0).key!!
                                                Update_watered_time(key, false)
                                            } else {
                                                verifying_layout.findViewById<LinearLayout>(R.id.qr_verify_load).visibility =
                                                    View.GONE
                                                verifying_layout.findViewById<LinearLayout>(R.id.invalid_code_view).visibility =
                                                    View.VISIBLE
                                                success_btn.visibility = View.VISIBLE
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {

                                        }
                                    })
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
            }
        }
    private var update_tree_result_launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val qr_result = result.data?.getStringExtra("scan_result")
                // validate this qr_result and if tree is there
                // then start Updateactivity class and pass the tree id to it
                // else display error message to user
                val intent = Intent(this, Update_Activity::class.java)
                // search and return tree id here
                intent.putExtra("tree_id", qr_result)
                startActivity(intent)
            }
        }

    private fun Update_watered_time(uid: String, isadopted: Boolean) {
        val time = System.currentTimeMillis()
        var path = if (isadopted)
            Adopted_trees
        else Registered_trees
        mdbRef.child(Trees).child(path).child(uid)
            .child(Last_watered_time).setValue(time)
            .addOnSuccessListener {
                verifying_layout.findViewById<LinearLayout>(R.id.qr_verify_load)
                    .visibility = View.GONE
                verifying_layout.findViewById<TextView>(R.id.success_code).visibility = View.VISIBLE
                success_btn.visibility = View.VISIBLE

            }

        if(isadopted){
            mdbRef.child(Trees).child(Adopted_trees).child(uid).child(Tree_details).child("adopted_by").addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user_id = snapshot.value
                    mdbRef.child("users").child(user_id as String).child(Adopted_trees).child(uid).child(
                        Last_watered_time).setValue(time)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }
}