package com.example.sharenote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 메시지 데이터 추출
        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body

        // 알림 표시
        showNotification(title, body)
    }

    private fun showNotification(title: String?, body: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 채널 생성 (Android O 이상 필요)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("default", "Default Channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 클릭 시 실행할 액티비티 설정
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE)


        // 알림 생성
        val notificationBuilder = NotificationCompat.Builder(this, "default")
            .setSmallIcon(R.drawable.ic_alert)  // 알림 아이콘 설정
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)  // 알림 클릭 후 자동 삭제
            .setContentIntent(pendingIntent)

        // 알림 표시
        notificationManager.notify(0, notificationBuilder.build())
    }
}
