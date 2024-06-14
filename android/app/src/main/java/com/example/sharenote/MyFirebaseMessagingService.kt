package com.example.sharenote

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /*
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // 새로운 토큰이 생성될 때 호출됩니다.
        Log.d("FCM", "New token: $token")
        // SharedPreferences에 저장
        SharedPreferencesUtil.saveFcmToken(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // FCM 메시지가 도착했을 때 호출됩니다.
    }*/
}
