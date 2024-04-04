package com.example.sharenote

import PageListAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class NoteActivity : AppCompatActivity(), PageListAdapter.OnPageClickListener {

    private lateinit var backTextView: TextView
    private lateinit var createPageButton: ImageButton

    private lateinit var recyclerView: RecyclerView
    private lateinit var pageListAdapter: PageListAdapter

    private var pages: MutableList<Page> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        backTextView = findViewById(R.id.backTextView)
        createPageButton = findViewById(R.id.CreatePage)

        recyclerView = findViewById(R.id.recyclerViewPages)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        pageListAdapter = PageListAdapter(pages, this)
        recyclerView.adapter = pageListAdapter

        backTextView.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        createPageButton.setOnClickListener {
            // PageActivity로 이동하는 Intent 생성
            val intent = Intent(this, PageActivity::class.java)
            startActivity(intent)
        }

        // 최근에 사용한 노트의 ID 가져오기
        val recentNoteId = SharedPreferencesUtil.getRecentNoteId(this)

        // 페이지 데이터를 불러오는 함수 호출
        recentNoteId?.let {
            loadPagesFromFirestore(it)
        }
    }

    override fun onPageClick(page: Page) {
        val intent = Intent(this, PageActivity::class.java)
        intent.putExtra("page_id", page.id)
        intent.putExtra("page_title", page.title)
        intent.putExtra("page_text", page.text)
        intent.putExtra("page_image_uri", page.imageUri)
        startActivity(intent)
    }

    private fun loadPagesFromFirestore(recentNoteId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pages")
            .whereEqualTo("noteId", recentNoteId) // 해당 워크스페이스 ID와 일치하는 노트만 가져오기
            .get()
            .addOnSuccessListener { result ->
                pages.clear()
                for (document in result) {
                    val pageID = document.getString("id") ?: ""
                    val pageTitle = document.getString("title") ?:""
                    val pageText = document.getString("text") ?: ""
                    val pageImageUri = document.getString("imageUri") ?: ""
                    val page = Page(pageID, pageTitle, pageText, pageImageUri)
                    pages.add(page)
                }
                pageListAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                // Log.e(TAG, "Error getting documents: ", exception)
            }
    }
}

