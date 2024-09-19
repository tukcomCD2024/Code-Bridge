package com.coop.sharenote

import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PageActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var floating: FloatingActionButton
    private lateinit var fabDraw: FloatingActionButton
    private var isFabOpen = false
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page)

        webView = findViewById(R.id.webView)
        floating = findViewById(R.id.floating)
        fabDraw = findViewById(R.id.fabDraw)

        // WebView 설정은 최초 생성 시에만 수행
        if (savedInstanceState == null) {
            webView.apply {
                webViewClient = WebViewClient() // 하이퍼링크 클릭시 새창 띄우기 방지
                webChromeClient = WebChromeClient() // 크롬환경에 맞는 세팅
                settings.javaScriptEnabled = true // 자바스크립트 허용
                settings.javaScriptCanOpenWindowsAutomatically = false
                settings.setSupportMultipleWindows(false) // 새창 띄우기 허용 여부 (멀티뷰)
                settings.loadsImagesAutomatically = true // 이미지 자동 로드
                settings.useWideViewPort = true // 화면 사이즈 맞추기 허용 여부
                settings.loadWithOverviewMode = true // 메타태그 허용 여부
                settings.setSupportZoom(true) // 화면 줌 허용여부
                settings.builtInZoomControls = false // 확대 축소 허용여부
                settings.displayZoomControls = false // 줌 컨트롤 없애기
                settings.cacheMode = WebSettings.LOAD_NO_CACHE // 캐시 모드 설정
                settings.domStorageEnabled = true // 로컬 스토리지 사용 여부
                settings.allowContentAccess = true // 파일 액세스 활성화 여부
                settings.userAgentString = "app" // 사용자 에이전트 설정
                settings.defaultTextEncodingName = "UTF-8" // 인코딩 설정
                settings.databaseEnabled = true // Database Storage API 사용 여부
            }

            val nickname = getUserName()
            val userId = getUserId()
            val email = getUserEmail()

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    val script = """
                        localStorage.setItem('nickname', '$nickname');
                        localStorage.setItem('userId', '$userId');
                        localStorage.setItem('email', '$email');
                    """.trimIndent()
                    webView.evaluateJavascript(script, null)
                }
            }

            // SharedPreferencesUtil을 사용하여 WorkSpaceId와 NoteId를 불러옵니다.
            val workspaceId = SharedPreferencesUtil.getRecentWorkspaceId(this)
            val noteId = SharedPreferencesUtil.getRecentNoteId(this)
            val pageId = SharedPreferencesUtil.getRecentPageId(this)

            webView.loadUrl("https://sharenote.shop/organization/$workspaceId/$noteId/$pageId")
        }

        // 플로팅 버튼 클릭시 에니메이션 동작 기능
        floating.setOnClickListener {
            toggleFab()
        }

        fabDraw.setOnClickListener {
            val intent = Intent(this, PaintActivity::class.java)
            startActivity(intent)
        }

        // 이미지 URL 처리
        val imageUrl = intent.getStringExtra(IMAGE_URL)
        if (imageUrl != null) {
            uploadImageToEditor(imageUrl)
        }
    }

    private fun uploadImageToEditor(imageUrl: String?) {
        if (imageUrl != null) {
            Log.d("PageActivity", "Image URL: $imageUrl")
            // 이미지 URL을 JavaScript 함수에 전달
            val jsFunction = "uploadImageToEditor('$imageUrl')"
            webView.evaluateJavascript(jsFunction, null)

            // 기존 PageActivity 재사용을 위해 startActivity 호출 제거
            // 이로 인해 무한 루프가 발생하지 않음
        }
    }

    companion object {
        const val IMAGE_URL = "image_url"
    }

    // 새롭게 전달된 intent를 받아오는 함수
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            // PaintActivity에서 전달된 새로운 이미지 URL을 가져옴
            val imageUrl = it.getStringExtra(IMAGE_URL)
            uploadImageToEditor(imageUrl)
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            // 웹사이트에서 뒤로갈 페이지가 존재한다면 수행
            webView.goBack() // 웹사이트 뒤로가기
            yjsDisconnect()
        } else {
            super.onBackPressed() // 본래의 백버튼 수행(안드로이드)
            yjsDisconnect()
        }
    }

    private fun yjsDisconnect(){
        val jsCode = "yjsDisconnect()"
        webView.evaluateJavascript(jsCode, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        webView.apply {
            stopLoading()
            loadUrl("about:blank")
            clearHistory()
            removeAllViews()
            destroy()
        }
    }

    private fun toggleFab() {
        // 플로팅 액션 버튼 닫기 - 열려있는 플로팅 버튼 집어넣는 애니메이션 세팅
        if (isFabOpen) {
            ObjectAnimator.ofFloat(fabDraw, "translationY", 0f).apply { start() }
            floating.setImageResource(R.drawable.ic_floating_add)

            // 플로팅 액션 버튼 열기 - 닫혀있는 플로팅 버튼 꺼내는 애니메이션 세팅
        } else {
            ObjectAnimator.ofFloat(fabDraw, "translationY", -200f).apply { start() }
            floating.setImageResource(R.drawable.ic_close)
        }

        isFabOpen = !isFabOpen
    }

    private fun getUserId(): String? {
        return SharedPreferencesUtil.getUserId(this)
    }

    private fun getUserName(): String? {
        return SharedPreferencesUtil.getUserName(this)
    }

    private fun getUserEmail(): String? {
        return SharedPreferencesUtil.getUserEmail(this)
    }
}
