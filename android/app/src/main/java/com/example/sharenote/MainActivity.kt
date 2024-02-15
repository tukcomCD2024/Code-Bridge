package com.example.sharenote

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.sharenote.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

        //initNavigationBar() //네이게이션 바의 각 메뉴 탭을 눌렀을 때 화면이 전환되도록 하는 함수.

        auth = FirebaseAuth.getInstance()

        val buttonCreateNote = findViewById<Button>(R.id.buttonCreateNote)
        buttonCreateNote.setOnClickListener {
            createNote()
        }

        val myPageButton = findViewById<Button>(R.id.myPageButton)
        myPageButton.setOnClickListener {
            val myPageIntent = Intent(this, MyPageActivity::class.java)
            startActivity(myPageIntent)
        }

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            // Firebase에서 로그아웃
            auth.signOut()

            // 로그인 화면으로 이동
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)

            // 현재 액티비티 종료 (메인 페이지에서 뒤로가기 시 로그인 화면으로 가도록)
            finish()
        }


        // BottomNavigationView 초기화
        bottomNavigationView = findViewById(R.id.bottom_navigation_view)

        // BottomNavigationView 아이템 클릭 리스너 설정
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.fragment_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.fragment_search -> {

                    true
                }
                R.id.fragment_alert -> {

                    true
                }
                R.id.fragment_settings -> {
                    startActivity(Intent(this, MyPageActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
    /*
    fun initNavigationBar() {
        binding.bottomNavigationView.run {
            setOnNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.fragment_home -> {
                        changeFragment(HomeFragment())
                    }
                    R.id.fragment_search -> {
                        changeFragment(SearchFragment())
                    }
                    R.id.fragment_alert -> {
                        changeFragment(AlertFragment())
                    }
                    R.id.fragment_settings -> {
                        changeFragment(MyPageFragment())
                    }
                }
                true
            }
            selectedItemId = R.id.fragment_home
        }
    }

    fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(binding.mainContainer.id, fragment).commit()
    }*/

    private fun createNote() {
        val intent = Intent(this, NoteActivity::class.java)
        startActivity(intent)
    }
}
