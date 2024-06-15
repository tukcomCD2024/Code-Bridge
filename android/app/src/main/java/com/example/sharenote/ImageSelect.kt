package com.example.sharenote

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView

import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


class ImageSelect : AppCompatActivity() {
    // 클래스 멤버 변수로 선언
    private lateinit var imageViews: List<ImageView>
    private lateinit var completeButton: Button
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_select)

        // Intent에서 이미지 URL 리스트 받아오기
        val urlList = intent.getStringArrayListExtra("urlList")

        completeButton = findViewById(R.id.completeButton)

        imageViews = listOf(
            findViewById<ImageView>(R.id.imageView1),
            findViewById<ImageView>(R.id.imageView2),
            findViewById<ImageView>(R.id.imageView3),
            findViewById<ImageView>(R.id.imageView4),
            findViewById<ImageView>(R.id.imageView5),
            findViewById<ImageView>(R.id.imageView6),
            findViewById<ImageView>(R.id.imageView7),
            findViewById<ImageView>(R.id.imageView8),
            findViewById<ImageView>(R.id.imageView9),
            findViewById<ImageView>(R.id.imageView10)
        )


        // 시간이 걸릴 수 잇으니까 로딩 스피너 추가하는게 좋대

        // urlList가 null이 아닌 경우에만 로직 수행
        urlList?.let {
            // zip 함수는 두 리스트 (imageViews 와 urlList)의 요소를 한 쌍씩 묶어 새로운 컬렉션 생성

            imageViews.zip(it).forEach { (imageView, url) ->
                loadImage(imageView, url)
                setupClickListener(imageView)
            }
        }

        completeButton.setOnClickListener {
            val imageUrl = getSelectedImageUrl()
            val resultIntent = Intent()
            if (imageUrl != null) {
                resultIntent.putExtra("selectedImageUrl", imageUrl)
                setResult(Activity.RESULT_OK, resultIntent)
            } else {
                setResult(Activity.RESULT_CANCELED)
            }
            finish() // 액티비티를 종료하고 결과를 호출한 액티비티로 반환
        }
    }

    // Glide : 이미지 뷰에 비동기적으로 이미지를 로드하고 표시 가능.
    // RequestListener : 이미지 로드 상태를 확인하여 콜백 함수를 통해 로딩 스피너 기능 추가 가능 할듯
    // 문제는 AI 서버로 부터의 응답 시간이 더 걸릴거라는 점
    private fun loadImage(imageView: ImageView, url: String?) {
        if (url != null) {
            imageView.setTag(url)
            Glide.with(this)
                .load(url)
                .apply(RequestOptions().fitCenter())
                .into(imageView)
        }
    }

    private fun setupClickListener(imageView: ImageView) {
        imageView.setOnClickListener {
            toggleSelection(imageView)
            updateBackground(imageView)
        }
    }
    // Q. 토클 왜 해줌? -> 마지막에 토글 되어 있는 놈의 imageUrl 반환하려고
    private fun toggleSelection(view: ImageView) {
        // 모든 이미지 뷰의 isSelected 속성을 false로 설정
        imageViews.forEach() {
            it.isSelected = false
            updateBackground(it)
        }

        view.isSelected = !view.isSelected // 선택 상태 토글
    }

    private fun updateBackground(imageView: ImageView) {
        if (imageView.isSelected) {
            imageView.setBackgroundResource(R.drawable.image_border) // 선택된 배경 설정
        } else {
            imageView.setBackgroundResource(0) // 배경 제거
        }
    }

    private fun getSelectedImageUrl(): String? {
        // 선택된 이미지 뷰를 찾아서 이미지 URL 반환
        // find로 imageViews 순회하면서 isSelected 속성이 true인 이미지 뷰를 찾음
        return imageViews.find { it.isSelected }?.let {
            it.getTag() as String
        }
    }
}
