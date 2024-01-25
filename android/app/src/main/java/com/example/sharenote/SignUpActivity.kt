package com.example.sharenote

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 중복 확인 버튼
        val checkDuplicateButton = findViewById<Button>(R.id.checkDuplicateButton)
        checkDuplicateButton.setOnClickListener {
            checkDuplicateUsername()
        }

        // 회원가입 버튼
        val signUpButton = findViewById<Button>(R.id.signupButton)
        signUpButton.setOnClickListener {
            signUp()
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // 로그인하기 텍스트 클릭 시 LoginActivity로 이동
        val loginLink = findViewById<TextView>(R.id.loginLink)
        loginLink.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    private fun checkDuplicateUsername() {
        // 중복 체크 로직 추가
        // 중복되지 않은 경우에는 사용 가능한 닉네임이라고 사용자에게 알려줄 수 있습니다.
        // 중복된 경우에는 다른 닉네임을 입력하도록 유도할 수 있습니다.
        // 이 부분은 실제로 서버에 요청하여 중복 여부를 확인하는 로직이 필요합니다.
        // 여기서는 단순히 Toast 메시지로 대체했습니다.
        Toast.makeText(this, "사용 가능한 닉네임입니다.", Toast.LENGTH_SHORT).show()
    }

    private fun signUp() {
        val username = findViewById<EditText>(R.id.usernameEditText).text.toString()
        val id = findViewById<EditText>(R.id.idEditText).text.toString()
        val email = findViewById<EditText>(R.id.emailEditText).text.toString()
        val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
        val confirmPassword = findViewById<EditText>(R.id.confirmPasswordEditText).text.toString()

        // 회원가입 로직 추가
        // 여기에는 Firebase Authentication 및 Firestore에 사용자 정보를 저장하는 로직을 추가해야 합니다.
        // 이는 각자의 프로젝트 설정에 따라 다르므로 자세한 내용은 Firebase 문서를 참조하세요.

        // 여기서는 단순히 Toast 메시지로 가입 성공 여부를 알려줍니다.
        Toast.makeText(this, "회원가입에 성공하였습니다.", Toast.LENGTH_SHORT).show()
    }
}
