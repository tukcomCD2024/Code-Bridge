package com.example.sharenote

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Note(
    val text: String,
    val imageUrl: String
)

class NoteActivity : AppCompatActivity() {

    private lateinit var editTextNote: EditText
    private lateinit var imageViewPreview: ImageView

    private var selectedImageUri: Uri? = null

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        editTextNote = findViewById(R.id.editTextNote)
        imageViewPreview = findViewById(R.id.imagePreview)
        val buttonAddImage = findViewById<Button>(R.id.buttonAddImage)
        val buttonSaveNote = findViewById<Button>(R.id.buttonSaveNote)

        buttonAddImage.setOnClickListener {
            openImageChooser()
        }

        buttonSaveNote.setOnClickListener {
            saveNote()
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun saveNote() {
        val noteText = editTextNote.text.toString()
        if (noteText.isEmpty()) {
            Toast.makeText(this, "노트를 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val note = Note(noteText, selectedImageUri.toString())

            // Firestore에 노트 저장
            firestore.collection("notes").document(userId).set(note)
                .addOnSuccessListener {
                    Toast.makeText(this, "노트가 저장되었습니다", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "노트 저장에 실패했습니다: $e", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            selectedImageUri = data?.data
            imageViewPreview.setImageURI(selectedImageUri)
            imageViewPreview.visibility = ImageView.VISIBLE
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}
