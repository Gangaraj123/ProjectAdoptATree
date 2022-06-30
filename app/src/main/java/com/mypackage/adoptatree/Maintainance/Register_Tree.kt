package com.mypackage.adoptatree.Maintainance

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.mypackage.adoptatree.*
import com.mypackage.adoptatree.R
import com.mypackage.adoptatree.models.Tree
import com.mypackage.adoptatree.models.Tree_Location
import java.util.*


class Register_Tree : AppCompatActivity() {
    private lateinit var back_btn: Button
    private lateinit var scan_view_before: LinearLayout
    private lateinit var scan_btn: Button
    private lateinit var result_loading: LinearLayout
    private lateinit var result_success: TextView
    private lateinit var result_error: TextView
    private lateinit var mdbRef: DatabaseReference
    private lateinit var locationRequest: LocationRequest
    private val REQUEST_CODE_LOCATION = 444
    private val REQUEST_ASK_Location = 443
    private var current_location: Location? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_tree)

        mdbRef = FirebaseDatabase.getInstance().reference
        back_btn = findViewById(R.id.go_back_btn)
        scan_view_before = findViewById(R.id.before_scan_view)
        scan_btn = findViewById(R.id.scan_btn)
        result_loading = findViewById(R.id.scan_result_loading)
        result_success = findViewById(R.id.result_success)
        result_error = findViewById(R.id.result_error)
        val back_btn = findViewById<ImageButton>(R.id.back_btn)
        back_btn.setOnClickListener {
            onBackPressed()
        }
        locationRequest = LocationRequest.create();
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        locationRequest.interval = 5000;
        locationRequest.fastestInterval = 2000;
        getCurrentLocation()

        scan_btn.setOnClickListener {
            val intent = Intent(this, QRCodeScanner::class.java)
            qr_cod_result_launcher.launch(intent)

        }

        back_btn.setOnClickListener {
            onBackPressed()
        }
    }

    private var qr_cod_result_launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val qr_result = data.getStringExtra("scan_result")
                    if (qr_result != null && is_valid_firebase_path(qr_result)) {
                        show_Loading()
                        // verify qr code here
                        //check if already a tree is present with this id
                        mdbRef.child(Trees).child(Adopted_trees).orderByChild(Tree_qr_value)
                            .equalTo(qr_result)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        showErrorMessage()
                                    } else {
                                        // check in registered trees
                                        mdbRef.child(Trees).child(Registered_trees).orderByChild(
                                            Tree_qr_value
                                        ).equalTo(qr_result).addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists()) {
                                                    showErrorMessage()
                                                } else {
                                                    Add_tree_in_database(qr_result)
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {}
                                        })
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    } else showErrorMessage()
                }
            }
        }

    private fun Add_tree_in_database(qr_value: String) {
        val geocoder = Geocoder(this, Locale.getDefault())

        var location_addr: Address? = null
        try {
            location_addr =
                geocoder.getFromLocation(
                    current_location!!.latitude,
                    current_location!!.longitude,
                    1
                )
                    .get(0)
        } catch (e: Exception) {
            Log.d(TAG, "Can't get location ")
        }
        val tree_id = mdbRef.child(Trees).child(Registered_trees).push().key.toString()
        val new_tree = Tree(tree_id)
        if (location_addr != null)
            new_tree.location = Tree_Location(location_addr)
        mdbRef.child(Trees).child(Registered_trees).child(tree_id).child(Tree_details)
            .setValue(new_tree)
            .addOnSuccessListener {
                mdbRef.child(Trees).child(Registered_trees).child(tree_id).child(Tree_qr_value)
                    .setValue(qr_value).addOnSuccessListener {
                        showSuccesMessage()
                    }.addOnFailureListener {
                        showErrorMessage()
                    }
            }.addOnFailureListener {
                showErrorMessage()
            }
    }

    private fun show_Loading() {
        result_loading.visibility = View.VISIBLE
        if (result_error.visibility == View.VISIBLE)
            result_error.visibility = View.GONE
        if (result_success.visibility == View.VISIBLE)
            result_success.visibility = View.GONE
        scan_view_before.visibility = View.GONE
    }

    private fun showErrorMessage() {
        scan_view_before.visibility = View.VISIBLE
        scan_btn.text = "scan again"
        result_loading.visibility = View.GONE
        result_error.visibility = View.VISIBLE
        back_btn.visibility = View.VISIBLE
    }

    private fun showSuccesMessage() {
        if (result_error.visibility == View.VISIBLE)
            result_error.visibility = View.GONE
        result_success.visibility = View.VISIBLE
        back_btn.visibility = View.VISIBLE
        result_loading.visibility = View.GONE
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        } else {
            if (isGPSEnabled()) {
                update_location()
            } else {
                TurnOnGPS()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun update_location() {
        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    LocationServices.getFusedLocationProviderClient(this@Register_Tree)
                        .removeLocationUpdates(this)
                    if (locationResult.locations.size > 0) {
                        val index = locationResult.locations.size - 1
                        current_location = locationResult.locations[index]
                    }
                }
            }, Looper.getMainLooper())
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_LOCATION -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Location access is needed", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    getCurrentLocation()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ASK_Location) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Location access is needed", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Log.d(TAG, "user turned on location")
                update_location()
            }
        }
    }

    private fun isGPSEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var isenabled = false
        isenabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        return isenabled
    }

    private fun TurnOnGPS() {

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: Task<LocationSettingsResponse> = LocationServices.getSettingsClient(
            applicationContext
        )
            .checkLocationSettings(builder.build())

        result.addOnCompleteListener(OnCompleteListener<LocationSettingsResponse?> { task ->
            try {
                val response = task.getResult(ApiException::class.java)
                Log.d(TAG, "Already location is on")
            } catch (e: ApiException) {
                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val resolvableApiException = e as ResolvableApiException
                        resolvableApiException.startResolutionForResult(this, REQUEST_ASK_Location)

                    } catch (ex: SendIntentException) {
                        ex.printStackTrace()
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        })
    }

    private fun is_valid_firebase_path(str: String): Boolean {
        if (str.isEmpty()) return false
        val invalid_characters = listOf('.', '#', '[', ']', '$')
        for (i in str) {
            if (i in invalid_characters)
                return false
        }
        return true
    }
}