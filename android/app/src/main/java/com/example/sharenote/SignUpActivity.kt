package com.example.sharenote

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
    private var isUsernameAvailable = false
    private val TAG = "SignUpActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        findViewById<Button>(R.id.checkDuplicateButton).setOnClickListener {
            val username = findViewById<EditText>(R.id.usernameEditText).text.toString()
            checkDuplicateUsername(username)
        }

        findViewById<Button>(R.id.signupButton).setOnClickListener {
            val email = findViewById<EditText>(R.id.emailEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
            val username = findViewById<EditText>(R.id.usernameEditText).text.toString()

            if (isUsernameAvailable) {
                signUp(email, password, username)
            } else {
                Toast.makeText(
                    baseContext, "닉네임 중복을 확인해주세요.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkDuplicateUsername(username: String) {
        // 중복 확인 로직을 구현
        // 사용 가능하면 isUsernameAvailable 변수를 true로 설정
        // 중복된 경우 Toast 메시지를 표시하고 isUsernameAvailable 변수를 false로 설정
        // 예시로 항상 사용 가능한 상태로 설정
        isUsernameAvailable = true
    }

    private fun signUp(email: String, password: String, username: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    saveUserData(user?.uid, username, email)
                    Toast.makeText(
                        baseContext, "회원가입에 성공하였습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                } else {
                    Toast.makeText(
                        baseContext, "회원가입에 실패하였습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun saveUserData(userId: String?, username: String, email: String) {
        val db = FirebaseFirestore.getInstance()
        val user = hashMapOf(
            "username" to username,
            "email" to email
            // 추가 필요한 정보
        )

        if (userId != null) {
            db.collection("users")
                .document(userId)
                .set(user)
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully written!")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error writing document", e)
                }
        }
    }
}
