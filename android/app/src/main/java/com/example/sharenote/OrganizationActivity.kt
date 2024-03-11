package com.example.sharenote

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class OrganizationActivity : AppCompatActivity() {

    private lateinit var teamCheckBox: CheckBox
    private lateinit var personalCheckBox: CheckBox
    private lateinit var educationalCheckBox: CheckBox
    private lateinit var continueButton: Button
    private lateinit var backTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organization)

        teamCheckBox = findViewById(R.id.teamCheckBox)
        personalCheckBox = findViewById(R.id.personalCheckBox)
        educationalCheckBox = findViewById(R.id.educationalCheckBox)
        backTextView = findViewById(R.id.backTextView)
        continueButton = findViewById(R.id.continueButton)
        continueButton.isEnabled = false

        val checkBoxes = listOf(teamCheckBox, personalCheckBox, educationalCheckBox)


        checkBoxes.forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    checkBoxes.filter { it != checkBox }.forEach {
                        it.isChecked = false
                    }
                }
                updateContinueButtonActivation(checkBoxes)
            }
        }

        backTextView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }



    private fun updateContinueButtonActivation(checkBoxes: List<CheckBox>) {
        continueButton.isEnabled = checkBoxes.any { it.isChecked }
    }
}
