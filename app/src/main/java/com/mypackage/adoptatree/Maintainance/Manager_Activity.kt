package com.mypackage.adoptatree.Maintainance

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.mypackage.adoptatree.R
import com.mypackage.adoptatree.TAG

class Manager_Activity : AppCompatActivity() {
     private lateinit var btn_add_tree:Button
    private lateinit var btn_register_tree:Button
    private lateinit var btn_update_tree_status:Button
    private lateinit var btn_water_tree:Button
     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager)

        btn_add_tree=findViewById(R.id.btn_add_new_tree)
        btn_register_tree=findViewById(R.id.btn_register_tree)
        btn_update_tree_status=findViewById(R.id.btn_update_status)
        btn_water_tree=findViewById(R.id.btn_water_tree)

        btn_update_tree_status.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this,QRCodeGenerator::class.java))
        })
        btn_water_tree.setOnClickListener{
            val intent= Intent(this,QRCodeScanner::class.java)
            qr_cod_result_launcher.launch(intent)
            Log.d(TAG,"started activity")
        }
    }

    private var qr_cod_result_launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                Log.d(TAG,"got = "+data.getStringExtra("scan_result"))
            }
        }
    }}