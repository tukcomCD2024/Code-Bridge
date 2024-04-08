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
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001 // Google 로그인 요청 코드

    private lateinit var Name: String
    private lateinit var Email: String
    private lateinit var Password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        // Google 로그인 구성
        configureGoogleSignIn()

        // 회원가입 창으로
        findViewById<View>(R.id.signupLink).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        // 로그인 버튼
        findViewById<View>(R.id.loginButton).setOnClickListener {
            val email = findViewById<EditText>(R.id.idEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
            signInWithEmail(email, password)
        }

        // Google 로그인 버튼
        val googleLoginButton = findViewById<ImageView>(R.id.googleLoginButton)
        googleLoginButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.idEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()
            login(email, password)
        }
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }


    // HTTP 통신을 통한 로그인 시도
    private fun login(email: String, password: String) {
        Email = email
        Password = password

        GlobalScope.launch(Dispatchers.IO) {
            try {
                // 이메일과 비밀번호로 사용자 인증을 시도
                val userData = UserData("", Email, Password) // 이름은 사용되지 않으므로 빈 문자열로 설정
                val response = apiService.login(userData)
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
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
                        "로그인 중 오류가 발생하였습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }




    private fun signInWithEmail(email: String, password: String) {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth?.signInWithEmailAndPassword(email, password)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            baseContext, "로그인에 성공 하였습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                        moveMainPage(auth?.currentUser)
                    } else {
                        Toast.makeText(
                            baseContext, "로그인에 실패 하였습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Google 로그인 결과 처리
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account?.idToken)
            } catch (e: ApiException) {
                Toast.makeText(
                    baseContext, "Google 로그인에 실패하였습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    moveMainPage(auth?.currentUser)
                } else {
                    Toast.makeText(
                        baseContext, "Firebase 인증에 실패하였습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("workSpaces")
                .whereEqualTo("owner", user.email)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        // 워크스페이스가 없는 경우 OrganizationActivity로 이동
                        startActivity(Intent(this, OrganizationActivity::class.java))
                    } else {
                        // 워크스페이스가 있는 경우 MainActivity로 이동
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    finish()
                }
                .addOnFailureListener { exception ->
                    // 쿼리 실패 시 에러 처리
                    Toast.makeText(
                        baseContext, "워크스페이스를 확인하는 중 오류가 발생하였습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
        }
    }
}
