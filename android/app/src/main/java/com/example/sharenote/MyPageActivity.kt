package com.example.sharenote

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class MyPageActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var updateButton: Button
    private lateinit var checkDuplicateButton: Button
    private lateinit var profileImageView: ImageView

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var imageUri: Uri? = null

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
    }

    private var isUsernameAvailable = false // 중복된 닉네임 여부를 저장하는 변수

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
        profileImageView = findViewById(R.id.profileImageView)

        // 파이어베이스 초기화
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // 사용자 정보 가져와서 화면에 보여주기
        fetchUserData()

        // 중복 확인 버튼 클릭 리스너 설정
        checkDuplicateButton.setOnClickListener {
            checkDuplicateUsername()
        }

        // 수정하기 버튼 클릭 리스너 설정
        updateButton.setOnClickListener {
            if (isUsernameAvailable) {
                updateUserProfile()
            } else {
                Toast.makeText(this, "닉네임 중복 확인을 확인해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        // 프로필 이미지뷰 클릭 리스너 설정
        profileImageView.setOnClickListener {
            selectImageFromGallery()
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
                isUsernameAvailable = querySnapshot.isEmpty || querySnapshot.documents.any { it.id == firebaseAuth.currentUser?.uid } // 중복 여부를 저장
                if (isUsernameAvailable) {
                    // 중복된 닉네임이 없으면 사용 가능
                    Toast.makeText(this, "사용 가능한 닉네임입니다", Toast.LENGTH_SHORT).show()
                    updateButton.isEnabled = true // 수정하기 버튼 활성화
                } else {
                    // 중복된 닉네임이 존재하면 사용 불가능
                    Toast.makeText(this, "이미 사용 중인 닉네임입니다", Toast.LENGTH_SHORT).show()
                    updateButton.isEnabled = false // 수정하기 버튼 비활성화
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

                // 닉네임을 Firestore에도 업데이트
                val userId = currentUser.uid
                userId.let {
                    val userRef = firestore.collection("users").document(it)
                    val updates = hashMapOf<String, Any>(
                        "Name" to newUsername
                    )
                    userRef.update(updates)
                        .addOnSuccessListener {
                            // 사용자 닉네임 업데이트 성공
                            Toast.makeText(this, "사용자 닉네임이 업데이트되었습니다", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            // 사용자 닉네임 업데이트 실패
                            Toast.makeText(this, "사용자 닉네임 업데이트에 실패했습니다: $e", Toast.LENGTH_SHORT).show()
                        }
                }

            } else {
                // 닉네임 업데이트 실패
                Toast.makeText(this, "닉네임 업데이트에 실패했습니다", Toast.LENGTH_SHORT).show()
                val errorMessage = task.exception?.message ?: "업데이트 실패"
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
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
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // 현재 액티비티 종료
                } else {
                    // 비밀번호 업데이트 실패
                    Toast.makeText(this, "비밀번호 업데이트에 실패했습니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 사용자 프로필 이미지를 업로드하고 URL을 가져오는 함수
    private fun uploadProfileImage(imageUri: Uri) {
        // 파일 이름은 사용자 UID로 설정하거나 원하는 대로 설정할 수 있습니다.
        val profileImageRef = storage.reference.child("profile_images/${firebaseAuth.currentUser?.uid}")

        profileImageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                // 이미지 업로드 성공 후 이미지의 다운로드 URL 가져오기
                profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                    // URL을 사용자 프로필 정보에 저장
                    val currentUser = firebaseAuth.currentUser
                    currentUser?.let { user ->
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setPhotoUri(uri)
                            .build()

                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // 프로필 업데이트 성공
                                    Toast.makeText(this, "프로필 사진이 업데이트되었습니다", Toast.LENGTH_SHORT).show()
                                } else {
                                    // 프로필 업데이트 실패
                                    Toast.makeText(this, "프로필 사진 업데이트에 실패했습니다", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                // 이미지 업로드 실패
                Toast.makeText(this, "이미지 업로드에 실패했습니다: $e", Toast.LENGTH_SHORT).show()
            }
    }

    // 갤러리에서 이미지 선택 후 호출되는 함수
    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    // onActivityResult 메서드에서 이미지 선택 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            // 선택된 이미지의 URI 가져오기
            val selectedImageUri: Uri? = data?.data
            selectedImageUri?.let {
                // 이미지 업로드 함수 호출
                uploadProfileImage(it)
            }
        }
    }
}
