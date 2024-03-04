package com.example.sharenote

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
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class NoteActivity : AppCompatActivity() {

    private lateinit var editTextNote: EditText
    private lateinit var editTextTitle: EditText
    private lateinit var buttonAddImage: Button
    private lateinit var buttonSaveNote: Button
    private lateinit var imagePreview: ImageView

    private var selectedImageUri: Uri? = null
    private var noteId: String? = null

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
        setContentView(R.layout.activity_note)

        editTextNote = findViewById(R.id.editTextNote)
        editTextTitle = findViewById(R.id.editTextTitle)
        buttonAddImage = findViewById(R.id.buttonAddImage)
        buttonSaveNote = findViewById(R.id.buttonSaveNote)
        imagePreview = findViewById(R.id.imagePreview)

        // 이전에 작성한 데이터가 있는지 확인하고 있으면 해당 데이터를 불러옴
        noteId = intent.getStringExtra("note_id")

        if (!noteId.isNullOrEmpty()) {
            val noteTitle = intent.getStringExtra("note_title")
            val noteText = intent.getStringExtra("note_text")
            val noteImageUri = intent.getStringExtra("note_image_uri")

            editTextTitle.setText(noteTitle)
            editTextNote.setText(noteText)
            selectedImageUri = Uri.parse(noteImageUri)
            Glide.with(this).load(selectedImageUri).into(imagePreview)
            imagePreview.visibility = ImageView.VISIBLE
        }

        buttonAddImage.setOnClickListener {
            openGallery()
        }

        buttonSaveNote.setOnClickListener {
            saveNote()
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

    private fun saveNote() {
        val noteTitle = editTextTitle.text.toString().trim()
        val noteText = editTextNote.text.toString().trim()

        if (noteText.isEmpty()) {
            Toast.makeText(this, "노트를 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()

        // 이전에 작성한 데이터가 있는 경우 해당 데이터의 ID를 사용하여 업데이트
        if (!noteId.isNullOrEmpty()) {
            val note = hashMapOf(
                "id" to noteId, // NoteId를 유지하도록 수정
                "title" to noteTitle,
                "text" to noteText,
                "imageUri" to selectedImageUri.toString()
            )

            db.collection("notes")
                .document(noteId!!)
                .set(note)
                .addOnSuccessListener {
                    Toast.makeText(this, "노트 업데이트 성공", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // 현재 액티비티 종료
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "노트 업데이트 실패: $e", Toast.LENGTH_SHORT).show()
                }
        } else {
            // 이전에 작성한 데이터가 없는 경우 새로운 노트 생성
            val newNoteId = UUID.randomUUID().toString()
            val note = hashMapOf(
                "id" to newNoteId, // 새로운 노트의 ID 생성
                "title" to noteTitle,
                "text" to noteText,
                "imageUri" to selectedImageUri.toString()
            )

            db.collection("notes")
                .document(newNoteId)
                .set(note)
                .addOnSuccessListener {
                    Toast.makeText(this, "노트 저장 성공", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // 현재 액티비티 종료
                }

                .addOnFailureListener { e ->
                    Toast.makeText(this, "노트 저장 실패: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
