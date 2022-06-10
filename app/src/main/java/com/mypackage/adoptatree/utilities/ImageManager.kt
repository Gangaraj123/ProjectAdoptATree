package com.mypackage.adoptatree.utilities

import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
 import com.google.firebase.storage.ktx.storage
import com.mypackage.adoptatree.R
import com.mypackage.adoptatree.TAG

internal object ImageManager{
    fun loadImageIntoView(view: ImageView,url:String?)
    {
        if(url!=null)
        {
            if (url.startsWith("gs://")){
            val storageReference = Firebase.storage.getReferenceFromUrl(url)
            storageReference.downloadUrl
                .addOnSuccessListener { uri ->
                    val downloadUrl = uri.toString()
                    Glide.with(view.context)
                        .load(downloadUrl).placeholder(R.drawable.image_not_available)
                        .into(view)
                }
                .addOnFailureListener { e ->
                   Log.d(TAG,"Failed to load image")
                }}
            else{
                Glide.with(view.context).load(url).placeholder(R.drawable.image_not_available).into(view)
            }
        }
        else
        {
            view.setImageResource(R.drawable.image_not_available)
        }
    }
}