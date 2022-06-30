package com.mypackage.adoptatree.utilities

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.transition.Fade
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.mypackage.adoptatree.R

class ImageViewer : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewr)
        hideSystemBars()
        // setting transition
        val fade = Fade()
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);

        window.enterTransition = fade
        window.exitTransition = fade

        // We need to pass imageUrl to this from intent
        val image_bitmap = intent.extras?.get("Image_bitmap") as Bitmap
        val zoomable_img_view = findViewById<ImageView>(R.id.zoomable_image_view)
        zoomable_img_view.setImageBitmap(image_bitmap)

        // close when back button is pressed
        findViewById<ImageButton>(R.id.back_btn).setOnClickListener {
            onBackPressed() // or finish()
        }
    }

    private fun hideSystemBars() {
        window?.decorView?.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        window.statusBarColor = Color.TRANSPARENT
    }
}