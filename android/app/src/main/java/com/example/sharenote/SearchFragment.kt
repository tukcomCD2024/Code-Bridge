package com.example.sharenote

import NoteListAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharenote.SharedPreferencesUtil.saveRecentNoteId
import java.util.Locale

class SearchFragment : Fragment() {
    private lateinit var searchEditText: EditText
    private lateinit var searchResultRecyclerView: RecyclerView
    private lateinit var noteListAdapter: NoteListAdapter
    private var noteList: MutableList<Note> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        searchEditText = view.findViewById(R.id.searchEditText)
        searchResultRecyclerView = view.findViewById(R.id.searchResultRecyclerView)

        // RecyclerView 설정
        noteListAdapter = NoteListAdapter { noteId ->
            // 노트 아이템 클릭 시 NoteActivity로 이동
            saveRecentNoteId(noteId) // 클릭된 노트의 ID를 저장합니다.
            val intent = Intent(requireContext(), NoteActivity::class.java)
            startActivity(intent)
        }
        searchResultRecyclerView.adapter = noteListAdapter
        searchResultRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // EditText에 텍스트 변경 리스너 추가
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 필요한 경우 구현
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // EditText의 텍스트가 변경될 때마다 호출되는 함수
                val searchText = s.toString().toLowerCase(Locale.getDefault())
                filterNotes(searchText)
            }

            override fun afterTextChanged(s: Editable?) {
                // 필요한 경우 구현
            }
        })

        return view
    }

    private fun filterNotes(searchText: String) {
        val filteredNotes = mutableListOf<Note>()
        for (note in noteList) {
            if (note.title.toLowerCase(Locale.getDefault()).contains(searchText)) {
                filteredNotes.add(note)
            }
        }
        noteListAdapter.setNotes(filteredNotes)
    }

    private fun saveRecentNoteId(noteId: String) {
        SharedPreferencesUtil.saveRecentNoteId(requireContext(), noteId)
    }
}
