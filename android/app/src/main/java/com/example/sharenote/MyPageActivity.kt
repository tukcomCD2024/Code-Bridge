package com.example.sharenote

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class MyPageActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var db: FirebaseFirestore

    private lateinit var editNickname: EditText
    private lateinit var textEmail: TextView
    private lateinit var editPassword: EditText
    private lateinit var editPasswordConfirm: EditText
    private lateinit var btnChangeProfile: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser!!
        db = FirebaseFirestore.getInstance()

        editNickname = findViewById(R.id.editNickname)
        textEmail = findViewById(R.id.textEmail)
        editPassword = findViewById(R.id.editPassword)
        editPasswordConfirm = findViewById(R.id.editPasswordConfirm)
        btnChangeProfile = findViewById(R.id.btnChangeProfile)

        // 화면에 현재 사용자의 정보 표시
        textEmail.text = currentUser.email

        // 변경 버튼 클릭 시 프로필 변경 시도
        btnChangeProfile.setOnClickListener {
            changeUserProfile()
        }
    }

    private fun changeUserProfile() {
        val newNickname = editNickname.text.toString()
        val newPassword = editPassword.text.toString()
        val newPasswordConfirm = editPasswordConfirm.text.toString()

        // 닉네임, 비밀번호 등 유효성 검사
        if (newNickname.isEmpty()) {
            editNickname.error = "닉네임을 입력하세요."
            return
        }

        if (newPassword.isNotEmpty() && newPassword != newPasswordConfirm) {
            editPasswordConfirm.error = "비밀번호가 일치하지 않습니다."
            return
        }

        // 프로필 변경 메서드 호출
        updateProfile(newNickname, newPassword)
    }

    private fun updateProfile(newNickname: String, newPassword: String) {
        // 사용자의 프로필 업데이트
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newNickname)
            .build()

        currentUser.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 프로필 업데이트 성공
                    Toast.makeText(this, "프로필이 업데이트되었습니다.", Toast.LENGTH_SHORT).show()

                    // 이후 필요한 작업 수행
                    // 예: 닉네임, 이메일, 비밀번호 등의 데이터를 Firestore에 업데이트
                    updateFirestoreUserData(newNickname, newPassword)
                } else {
                    // 실패 시 메시지 표시
                    Toast.makeText(this, "프로필 업데이트에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun updateFirestoreUserData(newNickname: String, newPassword: String) {
        // Firestore에 사용자 데이터 업데이트
        val userDocument = db.collection("users").document(currentUser.uid)

        // 예: 이름 필드 업데이트
        userDocument.update("Name", newNickname)
            .addOnSuccessListener {
                // 업데이트 성공
                Toast.makeText(this, "Firestore에 데이터가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // 업데이트 실패 시 메시지 표시
                Toast.makeText(this, "Firestore 업데이트 실패: $e", Toast.LENGTH_SHORT).show()
            }
    }
}

