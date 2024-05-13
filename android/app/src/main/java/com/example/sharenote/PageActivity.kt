package com.example.sharenote

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
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



        webView.apply {
            webViewClient = WebViewClient() // 하이퍼링크 클릭시 새창 띄우기 방지
            webChromeClient =
                WebChromeClient() // 크롬환경에 맞는 세팅을 해줌. 특히, 알람등을 받기위해서는 꼭 선언해주어야함 (alert같은 경우)
            settings.javaScriptEnabled = true // 자바스크립트 허용
            settings.javaScriptCanOpenWindowsAutomatically = false
            // 팝업창을 띄울 경우가 있는데, 해당 속성을 추가해야 window.open() 이 제대로 작동 , 자바스크립트 새창도 띄우기 허용여부
            settings.setSupportMultipleWindows(false) // 새창 띄우기 허용 여부 (멀티뷰)
            settings.loadsImagesAutomatically = true // 웹뷰가 앱에 등록되어 있는 이미지 리소스를 자동으로 로드하도록 설정하는 속성
            settings.useWideViewPort = true // 화면 사이즈 맞추기 허용 여부
            settings.loadWithOverviewMode = true // 메타태그 허용 여부
            settings.setSupportZoom(true) // 화면 줌 허용여부
            settings.builtInZoomControls = false // 화면 확대 축소 허용여부
            settings.displayZoomControls = false // 줌 컨트롤 없애기.
            settings.cacheMode = WebSettings.LOAD_NO_CACHE // 웹뷰의 캐시 모드를 설정하는 속성으로써 5가지 모드


            settings.domStorageEnabled =
                true // 로컬 스토리지 사용 여부를 설정하는 속성으로 팝업창등을 '하루동안 보지 않기' 기능 사용에 필요
            settings.allowContentAccess // 웹뷰 내에서 파일 액세스 활성화 여부
            settings.userAgentString = "app" // 웹에서 해당 속성을 통해 앱에서 띄운 웹뷰로 인지 할 수 있도록 합니다.
            settings.defaultTextEncodingName = "UTF-8" // 인코딩 설정
            settings.databaseEnabled = true //Database Storage API 사용 여부 설정
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

        val imageUrl = intent.getStringExtra(UrlTestActivity.EXTRA_IMAGE_URL)


        // WebView가 로드되면 이미지를 업로드하는 함수 호출
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 이미지를 업로드하는 함수 호출
                uploadImageToEditor(imageUrl)
            }
        }



        // SharedPreferencesUtil을 사용하여 WorkSpaceId와 NoteId를 불러옵니다.
        val workspaceId = SharedPreferencesUtil.getRecentWorkspaceId(this)
        val noteId = SharedPreferencesUtil.getRecentNoteId(this)
        val pageId = SharedPreferencesUtil.getRecentPageId(this)

        webView.loadUrl("https://sharenote.shop/organization/$workspaceId/$noteId/$pageId")

        // 플로팅 버튼 클릭시 에니메이션 동작 기능
        floating.setOnClickListener {
            toggleFab()
            Toast.makeText(this, "Image URL: $imageUrl", Toast.LENGTH_SHORT).show()
        }



        fabDraw.setOnClickListener {
            val intent = Intent(this, UrlTestActivity::class.java)
            startActivityForResult(intent, REQUEST_IMAGE_SELECTION)
        }
    }

    /*
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_SELECTION && resultCode == Activity.RESULT_OK) {
            val imageUriString = data?.getStringExtra(UrlTestActivity.EXTRA_IMAGE_URI)
            Log.d("PageActivity", "Received image URI: $imageUriString")
            // 로그 확인
            Log.d("PageActivity", "Selected image URI: $imageUriString")
            // 작업 중인 텍스트 줄에 이미지 URL 삽입
            if (!imageUriString.isNullOrEmpty()) {
                Toast.makeText(this, "Selected image URL: $imageUriString", Toast.LENGTH_SHORT).show()
                val script = """
                    var img = document.createElement('img');
                    img.src = '$imageUriString';
                    document.body.appendChild(img);
                """.trimIndent()
                webView.evaluateJavascript(script, null)
            }
        }
    }*/

    private fun uploadImageToEditor(imageUrl: String?) {
        if (imageUrl != null) {
            // 이미지 URL을 JavaScript 함수에 전달
            val jsFunction = "uploadImageToEditor('editorRef.current.view', '$imageUrl')"
            webView.evaluateJavascript(jsFunction, null)
        } else {
            // 이미지 URL이 null인 경우 처리
            Toast.makeText(this, "이미지 URL을 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        private const val REQUEST_IMAGE_SELECTION = 100
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            //웹사이트에서 뒤로갈 페이지가 존재 한다면 수행
            webView.goBack() // 웹사이트 뒤로가기

        } else {
            super.onBackPressed() // 본래의 백버튼 수행(안드로이드)
        }
    }

    private fun toggleFab() {
        // 플로팅 액션 버튼 닫기 - 열려있는 플로팅 버튼 집어넣는 애니메이션 세팅
        if (isFabOpen) {
            ObjectAnimator.ofFloat(fabDraw, "translationY", 0f).apply { start() }
            floating.setImageResource(R.drawable.ic_floating_add)

            // 플로팅 액션 버튼 열기 - 닫혀있는 플로팅 버튼 꺼내는 애니메이션 세팅
        } else {
            ObjectAnimator.ofFloat(fabDraw, "translationY", -200f,).apply { start() }
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