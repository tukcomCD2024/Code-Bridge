// MainActivity.kt
package com.example.sharenote

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var logoutButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Firebase 인증 객체 초기화
        auth = FirebaseAuth.getInstance()

        // 레이아웃에서 뷰 참조
        welcomeText = findViewById(R.id.welcomeText)
        logoutButton = findViewById(R.id.logoutButton)

        // 환영 메시지 설정 (여기에서는 현재 로그인한 사용자의 이메일을 표시합니다)
        val currentUser = auth.currentUser
        welcomeText.text = "환영합니다, ${currentUser?.email}님!"

        // 로그아웃 버튼 클릭 시
        logoutButton.setOnClickListener {
            // Firebase에서 로그아웃
            auth.signOut()

            // 로그인 화면으로 이동
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
