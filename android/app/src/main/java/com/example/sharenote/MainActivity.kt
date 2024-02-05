package com.example.sharenote

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
/*
        // 최근 방문한 페이지
        val recentNotesButton = findViewById<Button>(R.id.recentNotesButton)
        recentNotesButton.setOnClickListener {
            startActivity(Intent(this, RecentNotesActivity::class.java))
        }

        // 팀스페이스 목록
        val teamSpacesButton = findViewById<Button>(R.id.teamSpacesButton)
        teamSpacesButton.setOnClickListener {
            startActivity(Intent(this, TeamSpacesActivity::class.java))
        }

        // 개인 페이지 목록
        val personalNotesButton = findViewById<Button>(R.id.personalNotesButton)
        personalNotesButton.setOnClickListener {
            startActivity(Intent(this, PersonalNotesActivity::class.java))
        }

        // 설정
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // 멤버
        val membersButton = findViewById<Button>(R.id.membersButton)
        membersButton.setOnClickListener {
            startActivity(Intent(this, MembersActivity::class.java))
        }

        // 휴지통
        val trashButton = findViewById<Button>(R.id.trashButton)
        trashButton.setOnClickListener {
            startActivity(Intent(this, TrashActivity::class.java))
        }*/

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            // Firebase에서 로그아웃
            auth.signOut()

            // 로그인 화면으로 이동
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)

            // 현재 액티비티 종료 (메인 페이지에서 뒤로가기 시 로그인 화면으로 가도록)
            finish()
        }
    }
}
