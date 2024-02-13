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

        val buttonCreateNote = findViewById<Button>(R.id.buttonCreateNote)
        buttonCreateNote.setOnClickListener {
            createNote()
        }

        val myPageButton = findViewById<Button>(R.id.myPageButton)
        myPageButton.setOnClickListener {
            val myPageIntent = Intent(this, MyPageActivity::class.java)
            startActivity(myPageIntent)
        }

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
    private fun createNote() {
        val intent = Intent(this, NoteActivity::class.java)
        startActivity(intent)
    }
}
