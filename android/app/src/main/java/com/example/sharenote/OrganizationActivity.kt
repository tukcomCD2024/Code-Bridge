package com.example.sharenote

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
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

        continueButton.setOnClickListener {
            // teamCheckBox가 선택되어 있는지 확인
            if (teamCheckBox.isChecked) {
                val intent = Intent(this, WorkSpaceActivity::class.java)
                startActivity(intent)
            } else {
                // teamCheckBox가 선택되어 있지 않은 경우 사용자에게 메시지 표시
                Toast.makeText(this, "팀 선택이 필요합니다.", Toast.LENGTH_SHORT).show()
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
