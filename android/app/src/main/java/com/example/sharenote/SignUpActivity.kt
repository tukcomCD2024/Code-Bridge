package com.example.sharenote

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var nameEditText: EditText

    private lateinit var Name: String
    private lateinit var Email: String
    private lateinit var Password: String

    private val db = FirebaseFirestore.getInstance()

    private var isUsernameAvailable = false // 중복된 닉네임 여부를 저장하는 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        nameEditText = findViewById(R.id.usernameEditText)

        val signUpButton = findViewById<Button>(R.id.signupButton)
        signUpButton.setOnClickListener {
            // 사용자 입력값을 가져와서 변수에 초기화
            Name = nameEditText.text.toString()
            Email = emailEditText.text.toString()
            Password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            // 중복된 닉네임 확인 후 회원가입 진행
            if (isUsernameAvailable && Password == confirmPassword) {
                signUpUser()
            } else if (!isUsernameAvailable) {
                Toast.makeText(this, "사용 중인 닉네임입니다. 다른 닉네임을 선택하세요.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 중복 확인 버튼
        val checkDuplicateButton = findViewById<Button>(R.id.checkDuplicateButton)
        checkDuplicateButton.setOnClickListener {
            // 중복 확인 버튼을 눌렀을 때 중복 여부 확인
            checkDuplicateUsername()
        }

        // 로그인하기 텍스트 클릭 시 LoginActivity로 이동
        val loginLink = findViewById<TextView>(R.id.loginLink)
        loginLink.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    private fun signUpUser() {
        val auth = FirebaseAuth.getInstance()

        if (Email.isNotEmpty() && Password.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // 회원가입이 성공한 경우 사용자 정보를 Firestore에 저장
                        saveUserDataToFirestore()

                        // 회원가입 성공 메시지 표시
                        Toast.makeText(this@SignUpActivity, "계정 생성 완료.", Toast.LENGTH_SHORT).show()

                        // 가입창 종료
                        finish()
                    } else {
                        // 계정 생성 실패
                        Toast.makeText(this@SignUpActivity, "계정 생성 실패", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveUserDataToFirestore() {
        val user = hashMapOf(
            "Name" to Name,
            "Email" to Email,
            "Password" to Password
        )

        // Firestore에 사용자 정보 저장
        val collectionPath = "users" // 사용자 정보를 저장할 컬렉션 이름
        db.collection(collectionPath).document(FirebaseAuth.getInstance().currentUser!!.uid)
            .set(user)
            .addOnSuccessListener {
                // Firestore에 데이터가 성공적으로 추가된 경우
                Toast.makeText(this, "회원가입에 성공하였습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Firestore에 데이터 추가 중 오류 발생한 경우
                Toast.makeText(this, "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkDuplicateUsername() {
        val enteredName = nameEditText.text.toString()

        if (enteredName.isNotEmpty()) {
            // 닉네임이 비어 있지 않은 경우에만 중복 확인 쿼리 수행
            db.collection("users")
                .whereEqualTo("Name", enteredName)
                .get()
                .addOnSuccessListener { documents ->
                    isUsernameAvailable = documents.isEmpty // 중복 여부를 저장
                    if (isUsernameAvailable) {
                        // 중복된 닉네임이 없으면 사용 가능
                        Toast.makeText(this, "사용 가능한 닉네임입니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        // 중복된 닉네임이 존재하면 사용 불가능
                        Toast.makeText(this, "이미 사용 중인 닉네임입니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    // 쿼리 중 오류 발생
                    Toast.makeText(this, "중복 확인 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
        } else {
            // 닉네임이 비어 있는 경우에 대한 처리
            Toast.makeText(this, "닉네임을 입력하세요.", Toast.LENGTH_SHORT).show()
        }
    }
}

