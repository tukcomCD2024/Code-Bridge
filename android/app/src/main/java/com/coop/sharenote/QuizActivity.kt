package com.coop.sharenote

import QuizAdapter
import SearchNoteListAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log

class QuizActivity : AppCompatActivity(), QuizAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var quizAdapter: QuizAdapter

    private lateinit var createQuizButton: Button
    private lateinit var noteListAdapter: SearchNoteListAdapter

    private lateinit var backTextView: TextView
    private lateinit var noteEditText: TextView

    private var noteList: MutableList<Note> = mutableListOf()

    private var quizList: MutableList<QuizList> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)


        noteEditText = findViewById(R.id.noteEditText)
        createQuizButton = findViewById(R.id.createQuiz)

        backTextView = findViewById(R.id.backTextView)

        backTextView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        noteEditText.setOnClickListener {
            showNoteListDialog(noteEditText)
        }

        createQuizButton.setOnClickListener {
            val intent = Intent(this, CreateQuiz::class.java)
            startActivity(intent)
        }

        // 초기 프래그먼트 설정 (전체 보기)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, AllQuizFragment())
            .commit()

        val btnAll: TextView = findViewById(R.id.btnAll)
        val btnUnsolved: TextView = findViewById(R.id.btnUnsolved)
        val btnSolved: TextView = findViewById(R.id.btnSolved)

        btnAll.setOnClickListener {
            btnAll.setBackgroundResource(R.drawable.rectangle_bright_blue)
            btnUnsolved.setBackgroundResource(R.drawable.rectangle)
            btnSolved.setBackgroundResource(R.drawable.rectangle)

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, AllQuizFragment())
                .commit()
        }

        btnUnsolved.setOnClickListener {
            btnAll.setBackgroundResource(R.drawable.rectangle)
            btnUnsolved.setBackgroundResource(R.drawable.rectangle_bright_blue)
            btnSolved.setBackgroundResource(R.drawable.rectangle)

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, UnsolvedQuizFragment())
                .commit()
        }

        btnSolved.setOnClickListener {
            btnAll.setBackgroundResource(R.drawable.rectangle)
            btnUnsolved.setBackgroundResource(R.drawable.rectangle)
            btnSolved.setBackgroundResource(R.drawable.rectangle_bright_blue)

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, SolvedQuizFragment())
                .commit()
        }

        // 최근 작업 공간 ID를 가져와 노트 데이터를 로드
        val recentWorkspaceId = getRecentWorkspaceId()
        recentWorkspaceId?.let {
            val userId = SharedPreferencesUtil.getUserId(this) ?: ""
            loadNotesFromMongoDB(it, userId)
        }

        // 저장된 노트 제목을 설정
        val recentNoteTitle = getRecentNoteTitle()
        if (recentNoteTitle != null) {
            noteEditText.text = recentNoteTitle
        }
    }

    override fun onItemClick(quizId: String) {

        SharedPreferencesUtil.saveRecentQuizId(this, quizId)

        val intent = Intent(this, QuizDetailActivity::class.java)
        startActivity(intent)
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
                SharedPreferencesUtil.saveRecentNoteId(this, it.Id)
                SharedPreferencesUtil.saveRecentNoteTitle(this, it.title) // 제목을 SharedPreferences에 저장
                // 다이얼로그 닫기
                alertDialog.dismiss()
                // 액티비티 재실행
                restartQuizActivity()
            }
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

                val accessToken = SharedPreferencesUtil.getAccessToken(this@QuizActivity) ?: ""

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

    private fun restartQuizActivity() {
        val intent = Intent(this, QuizActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun getRecentWorkspaceId(): String? {
        return SharedPreferencesUtil.getRecentWorkspaceId(this)
    }

    private fun getRecentNoteTitle(): String? {
        return SharedPreferencesUtil.getRecentNoteTitle(this)
    }
}
