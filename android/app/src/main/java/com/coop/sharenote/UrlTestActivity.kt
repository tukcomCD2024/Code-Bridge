package com.coop.sharenote

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView


class UrlTestActivity : Activity() {

    private lateinit var imageView: ImageView
    private var selectedImageUri: Uri? = null

    companion object {
        private const val REQUEST_CODE_SELECT_IMAGE = 100
        const val EXTRA_IMAGE_URL = "extra_image_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_url_test)

        imageView = findViewById(R.id.imageView)

        val buttonChooseImage: Button = findViewById(R.id.buttonChooseImage)
        val buttonCheck: Button = findViewById(R.id.check)

        buttonChooseImage.setOnClickListener {
            // 갤러리에서 이미지를 선택하기 위한 인텐트 생성
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
        }

        buttonCheck.setOnClickListener {
            val imageUrl = "https://sharenotebucket.s3.ap-northeast-2.amazonaws.com/1ba6e58b-1092-4e27-b552-c0cb1a1f4cb8.png"
            val intent = Intent(this, PageActivity::class.java)
            intent.putExtra(EXTRA_IMAGE_URL, imageUrl)
            startActivityForResult(intent, 100)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            // 이미지 선택 결과 처리
            data?.data?.let { uri ->
                imageView.setImageURI(uri)
                imageView.visibility = ImageView.VISIBLE
                selectedImageUri = uri
            }
        }
    }
}

