package com.example.sharenote

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*



class NoteActivity : AppCompatActivity() {

    private lateinit var editTextNote: EditText
    private lateinit var buttonAddImage: Button
    private lateinit var buttonSaveNote: Button
    private lateinit var imagePreview: ImageView

    private var selectedImageUri: Uri? = null

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.data?.let { uri ->
                selectedImageUri = uri
                imagePreview.setImageURI(selectedImageUri)
                imagePreview.visibility = ImageView.VISIBLE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        editTextNote = findViewById(R.id.editTextNote)
        buttonAddImage = findViewById(R.id.buttonAddImage)
        buttonSaveNote = findViewById(R.id.buttonSaveNote)
        imagePreview = findViewById(R.id.imagePreview)

        buttonAddImage.setOnClickListener {
            openGallery()
        }

        buttonSaveNote.setOnClickListener {
            saveNote()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getContent.launch(intent)
    }

    private fun saveNote() {
        val noteText = editTextNote.text.toString().trim()
        val imageId = UUID.randomUUID().toString()

        if (noteText.isEmpty()) {
            Toast.makeText(this, "노트를 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val note = hashMapOf(
            "text" to noteText,
            "imageId" to imageId
        )

        db.collection("notes")
            .add(note)
            .addOnSuccessListener { documentReference ->
                uploadImage(imageId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "노트 저장 실패: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImage(imageId: String) {
        if (selectedImageUri != null) {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val imageRef = storageRef.child("images/$imageId")

            imageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    // 이미지 업로드 성공
                    Toast.makeText(this, "이미지 업로드 성공", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    // 이미지 업로드 실패
                    Toast.makeText(this, "이미지 업로드 실패: $e", Toast.LENGTH_SHORT).show()
                }
        } else {
            // 선택한 이미지가 없는 경우
            finish()
        }
    }
}
