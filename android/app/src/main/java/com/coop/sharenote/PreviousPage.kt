package com.coop.sharenote

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.coop.sharenote.SharedPreferencesUtil.getRecentWorkspaceId
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class PreviousPage : AppCompatActivity() {

    private lateinit var editTextPage: EditText
    private lateinit var editTextTitle: EditText
    private lateinit var buttonAddImage: Button
    private lateinit var buttonSavePage: Button
    private lateinit var imagePreview: ImageView

    private var selectedImageUri: Uri? = null
    private var pageId: String? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                selectedImageUri = uri
                Glide.with(this).load(selectedImageUri).into(imagePreview)
                imagePreview.visibility = ImageView.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_previous_page)

        editTextPage = findViewById(R.id.editTextPage)
        editTextTitle = findViewById(R.id.editTextTitle)
        buttonAddImage = findViewById(R.id.buttonAddImage)
        buttonSavePage = findViewById(R.id.buttonSavePage)
        imagePreview = findViewById(R.id.imagePreview)



        // 이전에 작성한 데이터가 있는지 확인하고 있으면 해당 데이터를 불러옴
        pageId = intent.getStringExtra("page_id")

        if (!pageId.isNullOrEmpty()) {
            val pageTitle = intent.getStringExtra("page_title")
            val pageText = intent.getStringExtra("page_text")
            val pageImageUri = intent.getStringExtra("page_image_uri")

            editTextTitle.setText(pageTitle)
            editTextPage.setText(pageText)
            selectedImageUri = Uri.parse(pageImageUri)
            Glide.with(this).load(selectedImageUri).into(imagePreview)
            imagePreview.visibility = ImageView.VISIBLE
        }

        buttonAddImage.setOnClickListener {
            openGallery()
        }

        buttonSavePage.setOnClickListener {
            savePage()
        }

        val buttonDeleteImage = findViewById<Button>(R.id.buttonDeleteImage)
        buttonDeleteImage.setOnClickListener {
            deleteImage()
        }
    }

    private fun deleteImage() {
        selectedImageUri = null
        imagePreview.setImageResource(android.R.color.transparent) // 이미지 뷰를 투명 이미지로 설정하여 이미지 제거
        imagePreview.visibility = ImageView.GONE
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getContent.launch(intent)
    }

    private fun savePage() {
        val pageTitle = editTextTitle.text.toString().trim()
        val pageText = editTextPage.text.toString().trim()

        if (pageText.isEmpty()) {
            Toast.makeText(this, "노트를 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()

        val recentNoteId = SharedPreferencesUtil.getRecentNoteId(this)

        // 이전에 작성한 데이터가 있는 경우 해당 데이터의 ID를 사용하여 업데이트
        if (!pageId.isNullOrEmpty()) {
            // 이전에 작성한 데이터가 있는 경우 해당 데이터의 ID를 사용하여 업데이트
            val page = hashMapOf(
                "id" to pageId, // NoteId를 유지하도록 수정
                "title" to pageTitle,
                "text" to pageText,
                "imageUri" to selectedImageUri.toString(),
                "workSpaceId" to getRecentWorkspaceId(this), // 최근 워크스페이스 ID 추가
                "noteId" to recentNoteId
            )

            db.collection("pages")
                .document(pageId!!)
                .set(page)
                .addOnSuccessListener {
                    Toast.makeText(this, "노트 업데이트 성공", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, NoteActivity::class.java)
                    startActivity(intent)
                    finish() // 현재 액티비티 종료
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "노트 업데이트 실패: $e", Toast.LENGTH_SHORT).show()
                }
        } else {
            // 이전에 작성한 데이터가 없는 경우 새로운 노트 생성
            val newPageId = UUID.randomUUID().toString()
            val page = hashMapOf(
                "id" to newPageId, // 새로운 노트의 ID 생성
                "title" to pageTitle,
                "text" to pageText,
                "imageUri" to selectedImageUri.toString(),
                "workSpaceId" to getRecentWorkspaceId(this), // 최근 워크스페이스 ID 추가
                "noteId" to recentNoteId
            )

            db.collection("pages")
                .document(newPageId)
                .set(page)
                .addOnSuccessListener {
                    Toast.makeText(this, "노트 저장 성공", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, NoteActivity::class.java)
                    startActivity(intent)
                    finish() // 현재 액티비티 종료
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "노트 저장 실패: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }

}
