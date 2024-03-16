package com.example.sharenote

import HomeFragment
import MyPageFragment
import SettingsFragment
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


        auth = FirebaseAuth.getInstance()


        // BottomNavigationView 초기화
        bottomNavigationView = findViewById(R.id.bottom_navigation_view)

        // BottomNavigationView 아이템 클릭 리스너 설정
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.fragment_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, HomeFragment())
                        .commit()
                    true
                }
                R.id.fragment_search -> {
                    // SearchFragment로 이동하는 코드 작성
                    true
                }
                R.id.fragment_alert -> {
                    // AlertFragment로 이동하는 코드 작성
                    true
                }
                R.id.fragment_settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, SettingsFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.fragment_home
    }


    private fun createNote() {
        val intent = Intent(this, NoteActivity::class.java)
        startActivity(intent)
    }
}
