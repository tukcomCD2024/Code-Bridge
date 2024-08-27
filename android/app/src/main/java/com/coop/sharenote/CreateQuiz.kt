package com.coop.sharenote

import SearchNoteListAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coop.sharenote.RetrofitClient.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateQuiz : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private var choiceIndex = 1
    private lateinit var noteListAdapter: SearchNoteListAdapter
    private var noteList: MutableList<Note> = mutableListOf()

    private val checkBoxList = mutableListOf<CheckBox>()

    private val answerEditTextList = mutableListOf<EditText>()

    private lateinit var backTextView: TextView

    // 선택된 노트 ID를 저장하는 변수
    private var selectedNoteId: String = ""

    // 선택된 객관식 문항의 인덱스를 저장하는 변수
    private var selectedChoiceIndex: Int = -1

    private var answerEditTextIdCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_quiz)

        container = findViewById(R.id.container)
        val plusText: TextView = findViewById(R.id.plusText)
        val noteEditText: TextView = findViewById(R.id.noteEditText)
        val titleEditText: EditText = findViewById(R.id.titleEditText)

        backTextView = findViewById(R.id.backTextView)
        
        backTextView.setOnClickListener {
            onBackPressed()
        }

        plusText.setOnClickListener {
            addNewChoiceLayout()
        }

        noteEditText.setOnClickListener {
            showNoteListDialog(noteEditText)
        }

        val submitButton: Button = findViewById(R.id.submitButton)
        submitButton.setOnClickListener {
            val orgId = SharedPreferencesUtil.getRecentWorkspaceId(this) ?: ""
            val userId = SharedPreferencesUtil.getUserId(this) ?: ""
            createQuizMongoDB(orgId, userId,titleEditText.text.toString())

            val intent = Intent(this, QuizActivity::class.java)
            startActivity(intent)
            finish()
        }

        val recentWorkspaceId = getRecentWorkspaceId()
        recentWorkspaceId?.let {
            val userId = SharedPreferencesUtil.getUserId(this) ?: ""
            loadNotesFromMongoDB(it, userId)
        }
    }

    private fun addNewChoiceLayout() {
        val inflater = LayoutInflater.from(this)
        val newChoiceLayout = inflater.inflate(R.layout.add_choice, container, false)

        val answerEditText: EditText = newChoiceLayout.findViewById(R.id.answerEditText)
        // 각 answerEditText에 고유한 아이디를 부여
        answerEditText.id = View.generateViewId()

        // 고유한 아이디를 리스트에 저장
        answerEditTextList.add(answerEditText)

        val choiceCheckBox: CheckBox = newChoiceLayout.findViewById(R.id.choiceCheckBox)
        answerEditText.hint = "객관식 ${choiceIndex}번 문항"
        choiceCheckBox.tag = choiceIndex

        choiceCheckBox.setOnClickListener {
            val index = it.tag as Int
            selectedChoiceIndex = index
            checkBoxList.forEach { checkBox ->
                if (checkBox != choiceCheckBox) {
                    checkBox.isChecked = false
                }
            }
        }

        checkBoxList.add(choiceCheckBox)
        container.addView(newChoiceLayout)
        choiceIndex++
    }

    private fun showNoteListDialog(editText: TextView) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.quiz_note_list, null)
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.recyclerView)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setNegativeButton("취소", null)

        // AlertDialog 객체를 미리 정의
        val alertDialog = builder.create()

        // noteListAdapter 초기화
        noteListAdapter = SearchNoteListAdapter { selectedNote ->
            // 클릭한 노트의 제목으로 설정
            val clickedNote = noteList.find { it.Id == selectedNote }
            clickedNote?.let {
                editText.text = it.title
                selectedNoteId = it.Id
            }
            // 다이얼로그 닫기
            alertDialog.dismiss()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = noteListAdapter

        noteListAdapter.setNotes(noteList)

        alertDialog.show()
    }


    private fun loadNotesFromMongoDB(recentWorkspaceId: String, userId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val notes = mutableListOf<Note>()
                val accessToken = SharedPreferencesUtil.getAccessToken(this@CreateQuiz) ?: ""
                val response = RetrofitClient.apiService.getOrganization(userId, accessToken)

                val matchingOrganization = response.find { it.id == recentWorkspaceId }

                if (matchingOrganization == null) {
                    return@launch
                }

                val organizationNotes = matchingOrganization.notes

                for (noteData in organizationNotes) {
                    val note = Note(
                        Id = noteData.id,
                        createUser = matchingOrganization.owner,
                        title = noteData.title,
                        noteImageUrl = noteData.noteImageUrl,
                    )
                    notes.add(note)
                }

                withContext(Dispatchers.Main) {
                    noteList = notes
                    noteListAdapter.setNotes(noteList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createQuizMongoDB(recentWorkspaceId: String, userId: String, problem: String){

        val solutions = answerEditTextList.map { it.text.toString() }

        // 퀴즈 생성 요청에 필요한 데이터를 생성
        val quizRequest = QuizRequest(
            organizationId = recentWorkspaceId,
            noteId = selectedNoteId,
            userId = userId,
            quizType = "객관식",
            problem = problem,
            answer = selectedChoiceIndex,
            problems = solutions
        )

        val accessToken = SharedPreferencesUtil.getAccessToken(this@CreateQuiz) ?: ""
        // 퀴즈 생성 요청 보내기
        apiService.createQuiz(quizRequest, accessToken).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 퀴즈 생성 성공 메시지 표시
                    Toast.makeText(this@CreateQuiz, "퀴즈 생성 성공", Toast.LENGTH_SHORT).show()
                } else {
                    // 서버 응답이 성공적이지 않은 경우 오류 메시지 표시
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@CreateQuiz, "퀴즈 생성 실패: $errorBody", Toast.LENGTH_SHORT).show()
                    Log.d("Quizerror", "error: $errorBody")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // 네트워크 오류 등으로 통신 실패
                Toast.makeText(this@CreateQuiz, "통신 실패: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun getRecentWorkspaceId(): String? {
        return SharedPreferencesUtil.getRecentWorkspaceId(this)
    }

    private fun saveRecentNoteId(noteId: String) {
        SharedPreferencesUtil.saveRecentNoteId(this, noteId)
    }
}
