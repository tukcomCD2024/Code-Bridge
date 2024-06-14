package com.example.sharenote

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharenote.RetrofitClient.apiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class LoginActivity : AppCompatActivity() {
    private var auth: FirebaseAuth? = null
    private var fcmToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        // FCM 토큰 받아오기
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                fcmToken = task.result
                Log.d("LoginActivity", "FCM Token: $fcmToken")
            } else {
                Log.w("LoginActivity", "Fetching FCM token failed", task.exception)
            }
        }

        // 회원가입 창으로 이동
        findViewById<View>(R.id.signupLink).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // 로그인 버튼
        findViewById<View>(R.id.loginButton).setOnClickListener {
            val email = findViewById<EditText>(R.id.idEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
            login(email, password)
        }

        // Google 로그인 버튼
        val googleLoginButton = findViewById<ImageView>(R.id.googleLoginButton)
        googleLoginButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.idEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
            login(email, password)
        }
    }

    // HTTP 통신을 통한 로그인 시도
    private fun login(email: String, password: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val userData = UserData("", "", email, password)
                val response = apiService.login(userData, fcmToken ?: "")
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null) {
                        val name = userResponse.name
                        val id = userResponse.userId
                        val accessToken = response.headers()["access"] ?: ""
                        val refreshToken = response.headers()["refresh"] ?: ""

                        // 로그 확인
                        Log.d("LoginActivity", "Access Token: $accessToken")
                        Log.d("LoginActivity", "Refresh Token: $refreshToken")

                        // SharedPreferences에 저장
                        SharedPreferencesUtil.saveUserData(this@LoginActivity, name, id, email)
                        SharedPreferencesUtil.saveAccessToken(this@LoginActivity, accessToken)
                        SharedPreferencesUtil.saveRefreshToken(this@LoginActivity, refreshToken)

                        // 로그인 성공 시 MainActivity로 이동
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@LoginActivity,
                            "로그인에 실패하였습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@LoginActivity,
                        "${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
