package com.example.sharenote

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContentProviderCompat.requireContext

class InviteActivity : AppCompatActivity() {

    private lateinit var continueButton: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invite)

        continueButton = findViewById(R.id.continueButton)

        continueButton.setOnClickListener {

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

}