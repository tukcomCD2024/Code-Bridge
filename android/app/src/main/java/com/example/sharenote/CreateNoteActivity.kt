package com.example.sharenote

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateNoteActivity : AppCompatActivity() {

    private lateinit var noteTitleEditText: EditText
    private lateinit var continueButton: Button
    private lateinit var backTextView: TextView

    private var organizationId: String = ""

    private lateinit var userId: String // 현재 UserId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_note)

        // View 초기화
        noteTitleEditText = findViewById(R.id.Notename)
        continueButton = findViewById(R.id.continueButton)
        backTextView = findViewById(R.id.backTextView)

        // 현재 워크스페이스 ID를 가져와서 organizationId에 저장
        organizationId = getRecentWorkspaceId() ?: ""

        // 현재 로그인한 사용자의 ID 가져오기
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        backTextView.setOnClickListener {
            onBackPressed()
        }

        continueButton.setOnClickListener {
            val noteTitle = noteTitleEditText.text.toString().trim()

            if (noteTitle.isNotEmpty()) {
                // 노트 생성 및 저장
                createNoteInFirestore(noteTitle)
            } else {
                Toast.makeText(this, "노트 제목을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createNoteInFirestore(noteTitle: String) {
        // Firestore에 새로운 노트 추가
        val db = FirebaseFirestore.getInstance()
        val notesCollection = db.collection("notes")

        // 새로운 노트의 ID 생성
        val newNoteId = notesCollection.document().id

        // 새로운 노트 생성 및 데이터 추가
        val newNote = hashMapOf(
            "noteId" to newNoteId,
            "title" to noteTitle,
            "organizationId" to organizationId,
            "userId" to userId
            // 기타 필요한 필드 추가
        )

        // notes 컬렉션에 새로운 노트 추가
        notesCollection.document(newNoteId)
            .set(newNote)
            .addOnSuccessListener {
                // 성공적으로 노트가 Firestore에 추가됨
                // 여기에 추가 작업 또는 UI 업데이트를 수행할 수 있음
                Toast.makeText(this, "노트가 생성되었습니다.", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                // 노트 생성 실패 처리
                Toast.makeText(this, "노트 생성에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getRecentWorkspaceId(): String? {
        return SharedPreferencesUtil.getRecentWorkspaceId(this)
    }
}
