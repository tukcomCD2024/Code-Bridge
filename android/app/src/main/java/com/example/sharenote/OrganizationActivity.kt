package com.example.sharenote

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class OrganizationActivity : AppCompatActivity() {

    private lateinit var continueButton: Button
    private lateinit var backTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization)

        backTextView = findViewById(R.id.backTextView)
        continueButton = findViewById(R.id.continueButton)


        continueButton.setOnClickListener {
            val intent = Intent(this, WorkSpaceActivity::class.java)
            startActivity(intent)
        }

        backTextView.setOnClickListener {
            onBackPressed()
        }

    }

}
