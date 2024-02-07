package com.example.sharenote

import android.content.Intent
import android.os.Bundle
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
    private lateinit var checkDuplicateButton: Button

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        findViewById<Button>(R.id.backButton).setOnClickListener {
            onBackPressed()
        }

        // 뷰 초기화
        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        updateButton = findViewById(R.id.updateButton)
        checkDuplicateButton = findViewById(R.id.checkDuplicateButton)

        // 파이어베이스 초기화
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // 사용자 정보 가져와서 화면에 보여주기
        fetchUserData()

        // 중복 확인 버튼 클릭 리스너 설정
        checkDuplicateButton.setOnClickListener {
            checkDuplicateUsername()
        }

        // 수정하기 버튼 클릭 리스너 설정
        updateButton.setOnClickListener {
            updateUserProfile()
        }
    }

    private fun fetchUserData() {
        // 현재 사용자 가져오기
        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            // 사용자 정보 가져오기
            val userId = user.uid
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        // 사용자 정보를 가져와서 화면에 보여주기
                        val username = document.getString("Name")
                        val email = document.getString("Email")

                        usernameEditText.setText(username)
                        emailEditText.setText(email)
                    } else {
                        Toast.makeText(this, "사용자 정보를 가져오지 못했습니다", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "사용자 정보를 가져오는 데 실패했습니다: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun checkDuplicateUsername() {
        val newUsername = usernameEditText.text.toString().trim()
        if (newUsername.isEmpty()) {
            Toast.makeText(this, "닉네임을 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 중복 확인을 위한 파이어스토어 쿼리 수행
        firestore.collection("users").whereEqualTo("Name", newUsername).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // 중복된 닉네임이 존재함
                    Toast.makeText(this, "이미 사용 중인 닉네임입니다", Toast.LENGTH_SHORT).show()
                } else {
                    // 중복된 닉네임이 없음
                    Toast.makeText(this, "사용 가능한 닉네임입니다", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                // 쿼리 실패
                Toast.makeText(this, "중복 확인에 실패했습니다: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserProfile() {
        // 수정할 정보 가져오기
        val newUsername = usernameEditText.text.toString().trim()
        val newPassword = passwordEditText.text.toString().trim()

        // 입력 값 유효성 검사
        if (newUsername.isEmpty() && newPassword.isEmpty()) {
            Toast.makeText(this, "수정할 정보를 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        // 현재 사용자 가져오기
        val currentUser = firebaseAuth.currentUser

        // 사용자 정보 업데이트
        val profileUpdatesBuilder = UserProfileChangeRequest.Builder().apply {
            if (newUsername.isNotEmpty()) setDisplayName(newUsername)
        }

        val profileUpdates = profileUpdatesBuilder.build()

        currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 닉네임 업데이트 성공
                Toast.makeText(this, "닉네임이 업데이트되었습니다", Toast.LENGTH_SHORT).show()
            } else {
                // 닉네임 업데이트 실패
                Toast.makeText(this, "닉네임 업데이트에 실패했습니다", Toast.LENGTH_SHORT).show()
            }
        }

        // 비밀번호 업데이트
        if (newPassword.isNotEmpty()) {
            currentUser?.updatePassword(newPassword)?.addOnCompleteListener { passwordTask ->
                if (passwordTask.isSuccessful) {
                    // 비밀번호 업데이트 성공
                    Toast.makeText(this, "비밀번호가 업데이트되었습니다", Toast.LENGTH_SHORT).show()

                    // 파이어베이스 Firestore에서 사용자 비밀번호 업데이트
                    val userId = currentUser.uid
                    userId.let {
                        val userRef = firestore.collection("users").document(it)
                        val updates = hashMapOf<String, Any>(
                            "Password" to newPassword
                        )
                        userRef.update(updates)
                            .addOnSuccessListener {
                                // 사용자 비밀번호 업데이트 성공
                                Toast.makeText(this, "사용자 비밀번호가 업데이트되었습니다", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                // 사용자 비밀번호 업데이트 실패
                                Toast.makeText(this, "사용자 비밀번호 업데이트에 실패했습니다: $e", Toast.LENGTH_SHORT).show()
                            }
                    }
                    // 마이페이지로 이동
                    val intent = Intent(this, MyPageActivity::class.java)
                    startActivity(intent)
                } else {
                    // 비밀번호 업데이트 실패
                    Toast.makeText(this, "비밀번호 업데이트에 실패했습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
