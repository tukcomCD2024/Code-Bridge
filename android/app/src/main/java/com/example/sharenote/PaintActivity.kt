package com.example.sharenote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class PaintActivity : AppCompatActivity() {
    private lateinit var backButton : Button
    private lateinit var check : Button
    private lateinit var check1 : Button
    private lateinit var check2 : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paint)

        backButton = findViewById(R.id.backButton)
        check = findViewById(R.id.check)
        check1 = findViewById(R.id.check1)
        check2 = findViewById(R.id.check2)


        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }


        check.setOnClickListener {
            val userName = SharedPreferencesUtil.getUserName(this)
            val userId = SharedPreferencesUtil.getUserId(this)
            val userEmail = SharedPreferencesUtil.getUserEmail(this)

            val message = "사용자 이름: $userName\n사용자 ID: $userId\n이메일: $userEmail"
            Toast.makeText(this@PaintActivity, message, Toast.LENGTH_LONG).show()
        }

        check1.setOnClickListener {

            val OranizationId = SharedPreferencesUtil.getRecentWorkspaceId(this)

            val message = "Organization: $OranizationId"
            Toast.makeText(this@PaintActivity, message, Toast.LENGTH_LONG).show()
        }

        check2.setOnClickListener {

            val NoteId = SharedPreferencesUtil.getRecentNoteId(this)

            val message = "Note: $NoteId"
            Toast.makeText(this@PaintActivity, message, Toast.LENGTH_LONG).show()
        }


    }


}