package com.mypackage.adoptatree.utilities

import android.os.Bundle
import android.transition.Fade
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.mypackage.adoptatree.R

class ImageViewer : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_viewr)

        // setting transition
        val fade = Fade()
        fade.excludeTarget(android.R.id.statusBarBackground, true);
        fade.excludeTarget(android.R.id.navigationBarBackground, true);
        window.enterTransition = fade
        window.exitTransition = fade

        // We need to pass imageUrl to this from intent
        val image_url = intent.getStringExtra("Image_url")
        val zoomable_img_view = findViewById<ImageView>(R.id.zoomable_image_view)
        ImageManager.loadImageIntoView(zoomable_img_view,image_url)

        // close when back button is pressed
        findViewById<ImageButton>(R.id.back_btn).setOnClickListener{
            onBackPressed() // or finish()
        }
    }
}