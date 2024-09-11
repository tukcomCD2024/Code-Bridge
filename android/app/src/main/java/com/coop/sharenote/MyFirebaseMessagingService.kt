package com.coop.sharenote

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
        val title = remoteMessage.notification?.title ?: "알림"
        val body = remoteMessage.notification?.body ?: "알림이 도착했습니다."

        // 알림 표시, body가 null인데(백엔드에서 그렇게 줌) 이러면 알림이 안생길 가능성이 높대
        showNotification(title, body)

        Log.d("FCM", "Message received: $title")
        Log.d("FCM", "Message received: $body")
    }

    private fun showNotification(title: String?, body: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 채널 생성 (Android O 이상 필요)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //default보다 High로 설정 시도
            val channel = NotificationChannel("default", "Default Channel", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 클릭 시 실행할 액티비티 설정
        val intent = Intent(this, QuizActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("fromNotification", true)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

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
