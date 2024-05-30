package com.example.sharenote

import MemberListAdapter
import PageListAdapter
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharenote.RetrofitClient.apiService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NoteActivity : AppCompatActivity(), PageListAdapter.OnPageClickListener, PageListAdapter.OnSettingClickListener {

    private lateinit var backTextView: TextView
    private lateinit var createPageButton: ImageButton

    private lateinit var recyclerView: RecyclerView
    private lateinit var pageListAdapter: PageListAdapter

    private lateinit var orgPopupWindow: PopupWindow

    private var pages: MutableList<Page> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)

        backTextView = findViewById(R.id.backTextView)
        createPageButton = findViewById(R.id.CreatePage)

        recyclerView = findViewById(R.id.recyclerViewPages)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        pageListAdapter = PageListAdapter(pages, this, this)
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

        findViewById<LinearLayout>(R.id.Organization).setOnClickListener {
            showOrgInfoPopup()
        }

        // 최근에 사용한 노트의 ID 가져오기
        val recentWorkspaceId = SharedPreferencesUtil.getRecentWorkspaceId(this)

        // 페이지 데이터를 불러오는 함수 호출
        recentWorkspaceId?.let {
            val userId = SharedPreferencesUtil.getUserId(this) ?: ""
            loadPagesFromMongoDB(it, userId)
        }
    }

    override fun onPageClick(page: Page) {
        saveRecentPageId(page.id)
        val intent = Intent(this, PageActivity::class.java)
        startActivity(intent)
    }

    override fun onSettingClick(page: Page, position: Int) {
        showSettingPopup(page, position)
    }


    private fun loadPagesFromMongoDB(recentWorkspaceId: String, userId: String) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // 현재 NoteId를 가져옵니다.
                val recentNoteId = SharedPreferencesUtil.getRecentNoteId(this@NoteActivity)

                // Retrofit을 사용하여 HTTP 요청을 보냅니다.
                val response = RetrofitClient.apiService.getOrganization(userId)

                // 받아온 데이터에서 현재 워크스페이스의 노트들만 필터링합니다.
                val matchingOrganization = response.find { it.id == recentWorkspaceId }

                // 현재 워크스페이스를 찾지 못한 경우 처리합니다.
                if (matchingOrganization == null) {
                    // 처리할 내용을 추가하세요
                    return@launch
                }

                // 현재 워크스페이스에 속한 노트들을 추출합니다.
                val notes = matchingOrganization.notes

                val matchingNote = notes.find { it.id == recentNoteId }

                // 찾은 Note가 없을 경우 처리합니다.
                if (matchingNote == null) {
                    // 처리할 내용을 추가하세요
                    return@launch
                }

                // 페이지 정보를 추출합니다.
                val pageChecks = matchingNote.pages

                // PageCheck를 Page로 변환하여 리스트에 추가합니다.
                val pages = mutableListOf<Page>()
                for (pageCheck in pageChecks) {
                    val page = Page(
                        id = pageCheck.id,
                        createUser = pageCheck.createUser,
                        createdAt = pageCheck.createdAt
                    )
                    pages.add(page)
                }

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


    fun showSettingPopup(page: Page, position: Int){
        // 팝업 창의 레이아웃을 inflate하여 가져옴
        val popupView = LayoutInflater.from(this).inflate(R.layout.setting_popup_layout, null)

        // 팝업 창을 생성
        val settingPopupWindow = PopupWindow(
            popupView,
            700,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )


        val titleEditText = popupView.findViewById<TextView>(R.id.titleEditText)
        titleEditText.text = "Page ${position + 1}"


        // 팝업 창 내의 각 레이아웃에 클릭 이벤트 설정
        val layout1 = popupView.findViewById<RelativeLayout>(R.id.layout1)
        val layout2 = popupView.findViewById<RelativeLayout>(R.id.layout2)
        val layout3 = popupView.findViewById<RelativeLayout>(R.id.layout3)

        layout1.setOnClickListener {
            // 클릭 이벤트 설정
        }

        layout2.setOnClickListener {
            // 클릭 이벤트 설정
        }

        layout3.setOnClickListener {
            // 클릭 이벤트 설정
        }


        // 팝업 창을 화면에 표시
        settingPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
    }



    private fun showOrgInfoPopup() {
        val recentWorkspaceId = getRecentWorkSpaceId() ?: ""
        val userId = getUserId() ?: ""
        val userName = getUserName() ?: ""
        // 팝업 창의 레이아웃을 inflate하여 가져옴
        val popupView = LayoutInflater.from(this).inflate(R.layout.org_info_layout, null)


        // 워크스페이스 이름을 표시할 텍스트뷰 선언
        val organizationTextView = popupView.findViewById<TextView>(R.id.Organization)

        val workspaceName = SharedPreferencesUtil.getRecentWorkspaceName(this)
        organizationTextView.text = workspaceName


        // 팝업 창을 생성
        orgPopupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )


        // 팝업 창 내의 RecyclerView 설정
        val memberRecyclerView = popupView.findViewById<RecyclerView>(R.id.recyclerViewMembers)
        val layoutManager = LinearLayoutManager(this)
        memberRecyclerView.layoutManager = layoutManager
        val memberAdapter = MemberListAdapter(mutableListOf()) // 초기에는 빈 리스트를 넣어 초기화
        memberRecyclerView.adapter = memberAdapter

        fetchOrganizationMembers(recentWorkspaceId, userId, memberAdapter)

        // 팝업 창 내의 멤버 수 텍스트뷰 설정
        val membersTextView = popupView.findViewById<TextView>(R.id.members)
        memberAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                membersTextView.text = "멤버 목록 (${memberAdapter.itemCount}명)"
            }
        })


        // Send Invitation 버튼 클릭 시 이메일 전송
        val sendInvitationButton = popupView.findViewById<Button>(R.id.send)
        sendInvitationButton.setOnClickListener {
            val emailEditText = popupView.findViewById<EditText>(R.id.inviteEditText)
            val email = emailEditText.text.toString()

            // 이메일을 보낼 때 사용할 데이터
            val inviteData = InvitationData(
                nickname = userName,
                organizationId = recentWorkspaceId,
                email = email
            )

            // 이메일 전송 함수 호출
            sendInvitationEmail(inviteData)
        }

        // 팝업 창을 화면에 표시
        orgPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)
    }


    fun sendInvitationEmail(data: InvitationData) {
        apiService.sendInvitationEmail(data).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 요청이 성공적으로 처리되었을 때의 작업 수행
                    showToast("이메일이 성공적으로 전송되었습니다.")
                } else {
                    // 요청이 실패했을 때의 작업 수행
                    showToast("이메일 전송에 실패했습니다: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // 네트워크 오류 또는 요청 실패시의 작업 수행
                showToast("오류가 발생했습니다: ${t.message}")
            }
        })
    }

    fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }




    private fun fetchOrganizationMembers(recentWorkspaceId: String, userId: String, memberAdapter: MemberListAdapter) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Retrofit을 사용하여 HTTP 요청을 보냄
                val response = RetrofitClient.apiService.getOrganization(userId)

                // 받아온 데이터에서 현재 워크스페이스의 데이터를 찾음
                val matchingOrganization = response.find { it.id == recentWorkspaceId }

                // 현재 워크스페이스를 찾지 못한 경우 처리
                if (matchingOrganization == null) {
                    // 처리할 내용을 추가하세요
                    return@launch
                }

                // 현재 워크스페이스에 속한 멤버 데이터를 가져옴
                val members = matchingOrganization.members

                // 어댑터에 멤버 데이터 설정
                withContext(Dispatchers.Main) {
                    memberAdapter.setMembers(members)
                }
            } catch (e: Exception) {
                // 오류 처리
                e.printStackTrace()
            }
        }
    }





    private fun saveRecentPageId(pageId: String) {
        SharedPreferencesUtil.saveRecentPageId(this, pageId)
    }

    private fun getUserName(): String? {
        return SharedPreferencesUtil.getUserName(this)
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

