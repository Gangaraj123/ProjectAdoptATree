package com.mypackage.adoptatree.User

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.FirebaseDatabase
import com.mypackage.adoptatree.*
import com.mypackage.adoptatree.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var tree_id: String
    private lateinit var tree_name: String
    private lateinit var myfabbutton: FloatingActionButton
    private lateinit var map_type_selection: ConstraintLayout
    private lateinit var map_type_terrain_background: View
    private lateinit var map_type_terrain_text: TextView
    private lateinit var map_type_satellite_background: View
    private lateinit var map_type_satellite_text: TextView
    private lateinit var map_type_default_background: View
    private lateinit var map_type_default_text: TextView
    private lateinit var map_type_terrain: ImageButton
    private lateinit var map_type_satellite: ImageButton
    private lateinit var map_type_default: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        tree_id = intent.getStringExtra("tree_id").toString()
        tree_name = intent.getStringExtra("tree_name").toString()
        hideSystemBars()
        myfabbutton = findViewById(R.id.choose_map_type_fab)
        map_type_selection = findViewById(R.id.map_type_selection)
        map_type_default = findViewById(R.id.map_type_default)
        map_type_default_background = findViewById(R.id.map_type_default_background)
        map_type_default_text = findViewById(R.id.map_type_default_text)
        map_type_terrain = findViewById(R.id.map_type_terrain)
        map_type_terrain_text = findViewById(R.id.map_type_terrain_text)
        map_type_terrain_background = findViewById(R.id.map_type_terrain_background)
        map_type_satellite = findViewById(R.id.map_type_satellite)
        map_type_satellite_text = findViewById(R.id.map_type_satellite_text)
        map_type_satellite_background = findViewById(R.id.map_type_satellite_background)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val mdbref = FirebaseDatabase.getInstance().reference
        mdbref.child(Trees).child(Adopted_trees).child(tree_id)
            .child(Tree_details).child("location")
            .get().addOnSuccessListener {
                try {
                    Log.d(TAG, it.value.toString())
                    val lat = it.child("latitude").value as Double
                    val long = it.child("longitude").value as Double
                    val tree_loc = LatLng(lat, long)
                    mMap.addMarker(MarkerOptions().position(tree_loc).title(tree_name))
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(tree_loc, 6.5f))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        // When map is initially loaded, determine which map type option to 'select'
        when {
            mMap.mapType == GoogleMap.MAP_TYPE_SATELLITE -> {
                map_type_satellite_background.visibility = View.VISIBLE
                map_type_satellite_text.setTextColor(Color.BLUE)
            }
            mMap.mapType == GoogleMap.MAP_TYPE_TERRAIN -> {
                map_type_terrain_background.visibility = View.VISIBLE
                map_type_terrain_text.setTextColor(Color.BLUE)
            }
            else -> {
                map_type_default_background.visibility = View.VISIBLE
                map_type_default_text.setTextColor(Color.BLUE)
            }
        }

        // Set click listener on FAB to open the map type selection view
        myfabbutton.setOnClickListener {

            // Start animator to reveal the selection view, starting from the FAB itself
            val anim = ViewAnimationUtils.createCircularReveal(
                map_type_selection,
                map_type_selection.width - (myfabbutton.width / 2),
                myfabbutton.height / 2,
                myfabbutton.width / 2f,
                map_type_selection.width.toFloat()
            )
            anim.duration = 200
            anim.interpolator = AccelerateDecelerateInterpolator()

            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    map_type_selection.visibility = View.VISIBLE
                }
            })

            anim.start()
            myfabbutton.visibility = View.INVISIBLE

        }

        // Set click listener on the map to close the map type selection view
        mMap.setOnMapClickListener {
            // Conduct the animation if the FAB is invisible (window open)
            if (myfabbutton.visibility == View.INVISIBLE) {
                hide_map_type_chooser()
            }
        }

        // Handle selection of the Default map type
        map_type_default.setOnClickListener {
            map_type_default_background.visibility = View.VISIBLE
            map_type_satellite_background.visibility = View.INVISIBLE
            map_type_terrain_background.visibility = View.INVISIBLE
            map_type_default_text.setTextColor(Color.BLUE)
            map_type_satellite_text.setTextColor(Color.parseColor("#808080"))
            map_type_terrain_text.setTextColor(Color.parseColor("#808080"))
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            hide_map_type_chooser()
        }

        // Handle selection of the Satellite map type
        map_type_satellite.setOnClickListener {
            map_type_default_background.visibility = View.INVISIBLE
            map_type_satellite_background.visibility = View.VISIBLE
            map_type_terrain_background.visibility = View.INVISIBLE
            map_type_default_text.setTextColor(Color.parseColor("#808080"))
            map_type_satellite_text.setTextColor(Color.BLUE)
            map_type_terrain_text.setTextColor(Color.parseColor("#808080"))
            mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            hide_map_type_chooser()
        }

        // Handle selection of the terrain map type
        map_type_terrain.setOnClickListener {
            map_type_default_background.visibility = View.INVISIBLE
            map_type_satellite_background.visibility = View.INVISIBLE
            map_type_terrain_background.visibility = View.VISIBLE
            map_type_default_text.setTextColor(Color.parseColor("#808080"))
            map_type_satellite_text.setTextColor(Color.parseColor("#808080"))
            map_type_terrain_text.setTextColor(Color.BLUE)
            mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            hide_map_type_chooser()
        }
    }

    private fun hide_map_type_chooser() {// Start animator close and finish at the FAB position
        val anim = ViewAnimationUtils.createCircularReveal(
            map_type_selection,
            map_type_selection.width - (myfabbutton.width / 2),
            myfabbutton.height / 2,
            map_type_selection.width.toFloat(),
            myfabbutton.width / 2f
        )
        anim.duration = 200
        anim.interpolator = AccelerateDecelerateInterpolator()

        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                map_type_selection.visibility = View.INVISIBLE
            }
        })

        // Set a delay to reveal the FAB. Looks better than revealing at end of animation
        Handler(Looper.getMainLooper()).postDelayed({
            kotlin.run {
                myfabbutton.visibility = View.VISIBLE
            }
        }, 100)
        anim.start()

    }

    private fun hideSystemBars() {
        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT
    }
}
