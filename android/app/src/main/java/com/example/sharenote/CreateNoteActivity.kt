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
import com.google.firebase.firestore.auth.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        //organizationId = getRecentWorkspaceId() ?: ""

        // 현재 로그인한 사용자의 ID 가져오기
        //userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""


        backTextView.setOnClickListener {
            onBackPressed()
        }

        continueButton.setOnClickListener {
            val title = noteTitleEditText.text.toString().trim()

            if (title.isNotEmpty()) {
                val organizationId = getRecentWorkspaceId() ?: ""
                val userId = getUserId() ?: ""

                val noteImageUrl = "http~" // NoteImageUrl 값은 임시로 설정했습니다.
                val noteData = UserNote(organizationId, title, userId, noteImageUrl)
                sendNoteDataToMongoDB(noteData)
            } else {
                Toast.makeText(this, "노트 제목을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createNoteInFirestore(noteTitle: String, noteImageUrl: String) {
        // Firestore에 새로운 노트 추가
        val db = FirebaseFirestore.getInstance()
        val notesCollection = db.collection("notes")

        // 새로운 노트의 ID 생성
        val newNoteId = notesCollection.document().id

        val newNote = hashMapOf(
            "organizationId" to organizationId,
            "title" to noteTitle,
            "userId" to userId,
            "noteImageUrl" to noteImageUrl,
            "noteId" to newNoteId
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


    private fun sendNoteDataToMongoDB(note: UserNote) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val accessToken = SharedPreferencesUtil.getAccessToken(this@CreateNoteActivity) ?: ""
                val response = RetrofitClient.apiService.sendNoteData(note, accessToken)
                if (response.isSuccessful) {
                    // MongoDB에 데이터 저장 성공
                    val noteResponse = response.body()
                    if (noteResponse != null) {
                        val noteId = noteResponse.noteId

                        saveRecentNoteId(noteId) // noteId 저장


                        // 저장이 완료되면 메인 화면으로 이동
                        val intent = Intent(this@CreateNoteActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // 반환된 데이터가 없을 경우 에러 처리
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@CreateNoteActivity,
                                "노트 정보를 받아오지 못했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    // MongoDB에 데이터 저장 실패
                    withContext(Dispatchers.Main) {
                        val errorMessage = "노트 정보를 저장하는 데 실패했습니다. 오류 코드: ${response.code()}"
                        Toast.makeText(
                            this@CreateNoteActivity,
                            errorMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                // 네트워크 오류 등 예외 처리
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@CreateNoteActivity,
                        "네트워크 오류가 발생했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun getRecentWorkspaceId(): String? {
        return SharedPreferencesUtil.getRecentWorkspaceId(this)
    }

    private fun getUserId(): String? {
        return SharedPreferencesUtil.getUserId(this)
    }

    private fun saveRecentNoteId(noteId: String) {
        SharedPreferencesUtil.saveRecentNoteId(this, noteId)
    }

}
