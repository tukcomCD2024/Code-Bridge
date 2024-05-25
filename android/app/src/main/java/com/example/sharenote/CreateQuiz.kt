package com.example.sharenote

import NoteListAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateQuiz : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private var choiceIndex = 1
    private lateinit var noteListAdapter: NoteListAdapter
    private var noteList: MutableList<Note> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_quiz)

        container = findViewById(R.id.container)
        val plusText: TextView = findViewById(R.id.plusText)
        val noteEditText: TextView = findViewById(R.id.noteEditText)

        plusText.setOnClickListener {
            addNewChoiceLayout()
        }

        noteEditText.setOnClickListener {
            showNoteListDialog(noteEditText)
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
        answerEditText.hint = "객관식 ${choiceIndex}번 문항"

        container.addView(newChoiceLayout)

        choiceIndex++
    }

    private fun showNoteListDialog(editText: TextView) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.quiz_note_list, null)
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.recyclerView)
        noteListAdapter = NoteListAdapter { selectedNote ->
            // 클릭한 노트의 제목으로 설정
            val clickedNote = noteList.find { it.Id == selectedNote }
            clickedNote?.let {
                editText.text = it.title
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = noteListAdapter

        noteListAdapter.setNotes(noteList)

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        builder.setNegativeButton("취소", null)
        builder.show()
    }

    private fun loadNotesFromMongoDB(recentWorkspaceId: String, userId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val notes = mutableListOf<Note>()

                val response = RetrofitClient.apiService.getOrganization(userId)

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

    private fun getRecentWorkspaceId(): String? {
        return SharedPreferencesUtil.getRecentWorkspaceId(this)
    }

    private fun saveRecentNoteId(noteId: String) {
        SharedPreferencesUtil.saveRecentNoteId(this, noteId)
    }
}
