package com.example.sharenote


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharenote.RetrofitClient.apiService
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoginActivity : AppCompatActivity() {
    private var auth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()


        // 회원가입 창으로
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
                val response = apiService.login(userData)
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    if (userResponse != null) {

                        val name = userResponse.name
                        val id = userResponse.userId
                        // SharedPreferences에 저장
                        SharedPreferencesUtil.saveUserData(this@LoginActivity, name, id, email)
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
