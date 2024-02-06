package com.example.sharenote

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class MyPageActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var updateButton: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        // 뷰 초기화
        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        updateButton = findViewById(R.id.updateButton)

        // 파이어베이스 초기화
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 현재 사용자의 정보 가져오기
        val currentUser = firebaseAuth.currentUser
        currentUser?.let {
            val userId = it.uid
            getUserInfoFromFirestore(userId)
        }

        // 수정하기 버튼 클릭 리스너 설정
        updateButton.setOnClickListener {
            updateUserProfile()
        }
    }

    private fun getUserInfoFromFirestore(userId: String) {
        // Firestore에서 사용자 정보 가져오기
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Firestore에서 가져온 사용자 정보를 화면에 설정
                    val username = document.getString("username")
                    val email = document.getString("email")
                    usernameEditText.setText(username)
                    emailEditText.setText(email)
                } else {
                    Toast.makeText(this, "사용자 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "사용자 정보를 불러오는데 실패했습니다", Toast.LENGTH_SHORT).show()
                Log.e("MyPageActivity", "Error getting user information", exception)
            }
    }


    private fun updateUserProfile() {
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // 입력 값 유효성 검사
        if (username.isEmpty() && email.isEmpty() && password.isEmpty()) {
            Toast.makeText(this, "수정할 정보를 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 현재 사용자 가져오기
        val user = firebaseAuth.currentUser

        // 사용자 정보 업데이트
        val profileUpdates = UserProfileChangeRequest.Builder().apply {
            if (username.isNotEmpty()) setDisplayName(username)
        }.build()
        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 닉네임 업데이트 성공
                    Toast.makeText(this, "닉네임이 업데이트되었습니다", Toast.LENGTH_SHORT).show()
                } else {
                    // 닉네임 업데이트 실패
                    Toast.makeText(this, "닉네임 업데이트에 실패했습니다", Toast.LENGTH_SHORT).show()
                }
            }

        // 이메일 업데이트
        if (email.isNotEmpty()) {
            user?.updateEmail(email)
                ?.addOnCompleteListener { emailTask ->
                    if (emailTask.isSuccessful) {
                        // 이메일 업데이트 성공
                        Toast.makeText(this, "이메일이 업데이트되었습니다", Toast.LENGTH_SHORT).show()
                    } else {
                        // 이메일 업데이트 실패
                        Toast.makeText(this, "이메일 업데이트에 실패했습니다", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // 비밀번호 업데이트
        if (password.isNotEmpty()) {
            user?.updatePassword(password)
                ?.addOnCompleteListener { passwordTask ->
                    if (passwordTask.isSuccessful) {
                        // 비밀번호 업데이트 성공
                        Toast.makeText(this, "비밀번호가 업데이트되었습니다", Toast.LENGTH_SHORT).show()
                    } else {
                        // 비밀번호 업데이트 실패
                        Toast.makeText(this, "비밀번호 업데이트에 실패했습니다", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}

