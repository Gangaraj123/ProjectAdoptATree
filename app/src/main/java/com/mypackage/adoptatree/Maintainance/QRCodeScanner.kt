package com.mypackage.adoptatree.Maintainance

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.*
import com.mypackage.adoptatree.R
import com.mypackage.adoptatree.TAG

// Class for implementing QR code scanner
class QRCodeScanner : AppCompatActivity() {

    private lateinit var scannerView: CodeScannerView   // scanner view from dependency
    private lateinit var codeScanner: CodeScanner            // scanner from dependency
    private val CAMERA_REQUEST_CODE = 23  // request for to ask camera permissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_scanner)

        setUpCameraPermissions() // to use camera

        // action bar settings -  adding back button
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "scan the code"

        // Initializing scanner paramters
        scannerView = findViewById(R.id.scanner_view)
        codeScanner = CodeScanner(this, scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE  // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {  // called on success
                makeVibration()
//                Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_SHORT).show()
                val intent=Intent()
                intent.putExtra("scan_result",it.text)
                setResult(RESULT_OK,intent)
                finish()
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {

            }
        }


    }

    private fun makeVibration() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    200,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            vibrator.vibrate(200)
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun setUpCameraPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest(); // If permission not granted, request user
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.CAMERA
            ), CAMERA_REQUEST_CODE
        )
    }

    // handling permission result
    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    val intent=Intent()
                    setResult(RESULT_CANCELED,intent)
                    finish() // Exit the activity
                } else {
                    Log.d(TAG, "Permission granted by user")
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) // if back button is clicked
        {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}