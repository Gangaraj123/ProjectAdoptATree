package com.mypackage.adoptatree.utilities

import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.ktx.storage
import com.mypackage.adoptatree.R
import com.mypackage.adoptatree.TAG

internal object ImageManager {
    fun loadImageIntoView(view: ImageView, url: String?) {
        if (url != null) {
            if (url.startsWith("gs://")) {
                val storageReference = Firebase.storage.getReferenceFromUrl(url)
                storageReference.downloadUrl
                    .addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()
                        Glide.with(view.context)
                            .load(downloadUrl).placeholder(R.drawable.image_not_available)
                            .into(view)
                    }
                    .addOnFailureListener { e ->
                        Log.d(TAG, "Failed to load image")
                    }
            } else {
                Glide.with(view.context).load(url).placeholder(R.drawable.image_not_available)
                    .into(view)
            }
        } else {
            view.setImageResource(R.drawable.image_not_available)
        }
    }

    fun updateTokenInFirebase() {
        val mdbRef = FirebaseDatabase.getInstance().reference
        val uid = FirebaseAuth.getInstance().uid
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                if (uid != null) {
                    mdbRef.child("users").child(uid).child("Notification_token")
                        .setValue(it.result)
                }
                Log.d(TAG,"token = "+it.result)
            }
        }
            .addOnFailureListener {
                Log.d(TAG,"Can't get token")
            }
    }
}