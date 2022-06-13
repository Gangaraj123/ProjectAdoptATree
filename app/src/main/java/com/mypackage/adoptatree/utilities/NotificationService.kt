package com.mypackage.adoptatree.utilities

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.remoteMessage
import com.mypackage.adoptatree.Maintainance.QRCodeGenerator
import com.mypackage.adoptatree.TAG

class NotificationService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        ImageManager.updateTokenInFirebase()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG,"Got a message")
        val intent=Intent(applicationContext,QRCodeGenerator::class.java)
        startActivity(intent)
    }
}