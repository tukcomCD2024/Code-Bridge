package com.example.sharenote

import PageListAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            val organizationId = getRecentWorkSpaceId() ?: ""
            val noteId = getRecentNoteId() ?: ""
            val userId = getUserId() ?: ""

            val pageData = PageData(organizationId, noteId, userId)
            sendPageDataToMongoDB(pageData)
        }

        // 최근에 사용한 노트의 ID 가져오기
        val recentWorkspaceId = SharedPreferencesUtil.getRecentWorkspaceId(this)

        // 페이지 데이터를 불러오는 함수 호출
        recentWorkspaceId?.let {
            loadPagesFromMongoDB(it)
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

    /*
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
    }*/


    private fun loadPagesFromMongoDB(recentWorkspaceId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // 현재 NoteId를 가져옵니다.
                val recentNoteId = SharedPreferencesUtil.getRecentNoteId(requireContext())

                // Retrofit을 사용하여 HTTP 요청을 보냅니다.
                val response = RetrofitClient.apiService.getOrganization(recentWorkspaceId)

                // 받아온 데이터에서 현재 NoteId와 일치하는 Note를 찾습니다.
                val notes = response.flatMap { it.notes }
                val matchingNote = notes.find { it.id == recentNoteId }

                // 찾은 Note가 없을 경우 처리합니다.
                if (matchingNote == null) {
                    // 처리할 내용을 추가하세요
                    return@launch
                }

                // 페이지 정보를 추출합니다.
                val pages = matchingNote.pages

                // 추출한 페이지 정보를 사용하여 원하는 작업을 수행합니다.
                withContext(Dispatchers.Main) {
                    // 페이지 정보를 어댑터에 설정합니다.
                    pageListAdapter.setPages(pages)
                }

            } catch (e: Exception) {
                // 오류 처리
                Log.e(TAG, "Error loading pages from MongoDB", e)
            }
        }
    }




    private fun sendPageDataToMongoDB(page: PageData) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.sendPageData(page)
                if (response.isSuccessful) {
                    // MongoDB에 데이터 저장 성공
                    val pageResponse = response.body()
                    if (pageResponse != null) {
                        val pageId = pageResponse.pageId


                        saveRecentPageId(pageId)



                        // 저장이 완료되면 메인 화면으로 이동
                        val intent = Intent(this@NoteActivity, PageActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // 반환된 데이터가 없을 경우 에러 처리
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@NoteActivity,
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
                            this@NoteActivity,
                            errorMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                // 네트워크 오류 등 예외 처리
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@NoteActivity,
                        "네트워크 오류가 발생했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    private fun saveRecentPageId(pageId: String) {
        SharedPreferencesUtil.saveRecentPageId(this, pageId)
    }

    private fun getUserId(): String? {
        return SharedPreferencesUtil.getUserId(this)
    }
    private fun getRecentWorkSpaceId(): String? {
        return SharedPreferencesUtil.getRecentWorkspaceId(this)
    }
    private fun getRecentNoteId(): String? {
        return SharedPreferencesUtil.getRecentNoteId(this)
    }
}

