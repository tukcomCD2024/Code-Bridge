package com.example.sharenote

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView


class MobileActivity : AppCompatActivity() {

    private lateinit var back : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mobile)

        back = findViewById(R.id.back)

        back.setOnClickListener {
            onBackPressed()
        }

    }
}