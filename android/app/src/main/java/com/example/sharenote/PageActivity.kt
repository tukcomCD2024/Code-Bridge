package com.example.sharenote

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.example.sharenote.R
import com.example.sharenote.SharedPreferencesUtil

class PageActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page)

        webView = findViewById(R.id.webView)

        // SharedPreferencesUtil을 사용하여 WorkSpaceId와 NoteId를 불러옵니다.
        val workspaceId = SharedPreferencesUtil.getRecentWorkspaceId(this)
        val noteId = SharedPreferencesUtil.getRecentNoteId(this)

        webView.loadUrl("sharenote.shop/organization/661944a532a485420decd9cc/661944b432a485420decd9cd")

        // 웹뷰가 수평으로 스크롤할 수 있도록 설정
        webView.setHorizontalScrollBarEnabled(true)
        webView.setScrollbarFadingEnabled(false)
    }
}
