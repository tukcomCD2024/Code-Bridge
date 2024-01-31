package com.example.sharenote

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.auth.User
import java.util.jar.Attributes.Name

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var idEditText: EditText

    private lateinit var Name : String
    private lateinit var Id : String
    private lateinit var Email : String
    private lateinit var Password : String


    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        nameEditText = findViewById(R.id.usernameEditText)
        idEditText = findViewById(R.id.idEditText)

        val signUpButton = findViewById<Button>(R.id.signupButton)
        signUpButton.setOnClickListener {
            // 사용자 입력값을 가져와서 변수에 초기화
            Name = nameEditText.text.toString()
            Id = idEditText.text.toString()
            Email = emailEditText.text.toString()
            Password = passwordEditText.text.toString()

            signUpUser(Email, Password)
        }

        // 중복 확인 버튼
        val checkDuplicateButton = findViewById<Button>(R.id.checkDuplicateButton)
        checkDuplicateButton.setOnClickListener {
            checkDuplicateUsername()
        }

        // 로그인하기 텍스트 클릭 시 LoginActivity로 이동
        val loginLink = findViewById<TextView>(R.id.loginLink)
        loginLink.setOnClickListener {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        }
    }

    private fun signUpUser(email: String, password: String) {
        val auth = Firebase.auth

        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "계정 생성 완료.", Toast.LENGTH_SHORT).show()

                        // 회원가입이 성공한 경우 사용자 정보를 데이터베이스에 저장
                        saveUserDataToFirestore(Name, Id, Email, Password)

                        finish() // 가입창 종료
                    } else {
                        // 계정 생성 실패 원인을 확인하여 메시지 표시
                        val errorMessage = task.exception?.message ?: "계정 생성 실패"
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserDataToFirestore(name: String, id: String, email: String, password: String) {
        val user = hashMapOf(
            "Name" to name,
            "Id" to id,
            "Email" to email,
            "Password" to password
            // 다른 필요한 정보 추가
        )

        // 실제로 데이터를 저장할 Firestore 컬렉션 및 문서 경로를 지정
        val collectionPath = "users" // 사용자 정보를 저장할 컬렉션 이름
        db.collection(collectionPath).document(Firebase.auth.currentUser!!.uid).set(user)
            .addOnSuccessListener {
                // Firestore에 데이터가 성공적으로 추가된 경우
                Toast.makeText(
                    baseContext, "회원가입에 성공하였습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                // Firestore에 데이터 추가 중 오류 발생한 경우
                Toast.makeText(
                    baseContext, "회원가입에 실패하였습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun checkDuplicateUsername() {
        // 중복 확인 로직 추가
        // 중복되지 않은 경우에는 사용 가능한 닉네임이라고 사용자에게 알려줄 수 있습니다.
        // 중복된 경우에는 다른 닉네임을 입력하도록 유도할 수 있습니다.
        // 여기서는 단순히 Toast 메시지로 대체했습니다.
        Toast.makeText(this, "사용 가능한 닉네임입니다.", Toast.LENGTH_SHORT).show()
    }

    private fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
