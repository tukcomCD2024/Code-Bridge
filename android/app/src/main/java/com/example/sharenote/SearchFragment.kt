package com.example.sharenote

import NoteListAdapter
import SearchNoteListAdapter
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class SearchFragment : Fragment() {
    private lateinit var searchEditText: EditText
    private lateinit var searchResultRecyclerView: RecyclerView
    private lateinit var noteListAdapter: SearchNoteListAdapter
    private var noteList: MutableList<Note> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val recentWorkspaceId = getRecentWorkspaceId()

        searchEditText = view.findViewById(R.id.searchEditText)
        searchResultRecyclerView = view.findViewById(R.id.searchResultRecyclerView)

        searchResultRecyclerView.layoutManager = LinearLayoutManager(context)
        noteListAdapter = SearchNoteListAdapter { noteId ->
            // Handle item click
        }
        searchResultRecyclerView.adapter = noteListAdapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                noteListAdapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        recentWorkspaceId?.let {
            val userId = SharedPreferencesUtil.getUserId(requireContext()) ?: ""
            loadNotesFromMongoDB(it, userId)
        }

        return view
    }

    private fun loadNotesFromMongoDB(recentWorkspaceId: String, userId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val notes = mutableListOf<Note>()

                // Retrofit을 사용하여 HTTP 요청을 보냅니다.
                val response = RetrofitClient.apiService.getOrganization(userId)

                // 받아온 데이터에서 현재 organizationId와 일치하는 조직을 찾습니다.
                val matchingOrganization = response.find { it.id == recentWorkspaceId }

                // 현재 organizationId와 일치하는 조직이 없을 경우 처리합니다.
                if (matchingOrganization == null) {
                    // 처리할 내용을 추가하세요
                    return@launch
                }

                // 일치하는 조직의 노트 정보를 추출합니다.
                val organizationNotes = matchingOrganization.notes

                // 추출된 노트 정보를 Note 객체로 변환하여 리스트에 추가합니다.
                for (noteData in organizationNotes) {
                    val note = Note(
                        Id = noteData.id,
                        createUser = matchingOrganization.owner,
                        title = noteData.title,
                        noteImageUrl = noteData.noteImageUrl,
                    )
                    notes.add(note)
                }

                // 어댑터에 데이터 설정
                withContext(Dispatchers.Main) {
                    noteList = notes
                    noteListAdapter.setNotes(noteList)
                }
            } catch (e: Exception) {
                // 오류 처리
                e.printStackTrace()
                // 예상치 못한 오류가 발생했을 때
            }
        }
    }

    private fun getRecentWorkspaceId(): String? {
        return SharedPreferencesUtil.getRecentWorkspaceId(requireContext())
    }

    private fun saveRecentNoteId(noteId: String) {
        SharedPreferencesUtil.saveRecentNoteId(requireContext(), noteId)
    }
}
