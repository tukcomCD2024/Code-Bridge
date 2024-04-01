import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharenote.LoginActivity
import com.example.sharenote.MainActivity
import com.example.sharenote.PageActivity
import com.example.sharenote.OrganizationActivity
import com.example.sharenote.Page
import com.example.sharenote.R
import com.example.sharenote.SharedPreferencesUtil
import com.example.sharenote.WorkSpace
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment(), PageListAdapter.OnPageClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var pageListAdapter: PageListAdapter
    private lateinit var emailTextView: TextView
    private lateinit var menuBtn: ImageButton
    private lateinit var profileForm: RelativeLayout

    private lateinit var emailTextView1: TextView
    private lateinit var workSpaceText: TextView
    private lateinit var popupView: View // 팝업 뷰
    private lateinit var setting_circle: ImageView

    private var pages: MutableList<Page> = mutableListOf()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        auth = FirebaseAuth.getInstance()
        recyclerView = view.findViewById(R.id.recyclerViewPages)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager
        pageListAdapter = PageListAdapter(pages, this)
        recyclerView.adapter = pageListAdapter
        menuBtn = view.findViewById(R.id.menuBtn)
        profileForm = view.findViewById(R.id.profileForm)

        // account_layout을 팝업으로 사용하기 위해 팝업 뷰를 초기화
        popupView = layoutInflater.inflate(R.layout.account_layout, null)
        emailTextView1 = popupView.findViewById(R.id.email)
        workSpaceText = view.findViewById(R.id.workSpaceText)

        setting_circle = popupView.findViewById(R.id.setting_circle)

        // emailTextView를 찾습니다.
        emailTextView = view.findViewById(R.id.emailtextView)

        // 최근에 방문한 워크스페이스 ID를 불러옵니다.
        val recentWorkspaceId = getRecentWorkspaceId()

        // 사용자 이메일을 표시합니다.
        displayUserEmail()

        recentWorkspaceId?.let {
            displayWorkspaceName(it)
        }

        profileForm.setOnClickListener {
            // account_layout을 화면 아래에 절반 크기로 보여줌
            showPopupAccount()
        }

        // menuBtn을 클릭했을 때 팝업 메뉴를 표시합니다.
        menuBtn.setOnClickListener {
            showPopupMenu()
        }



        setting_circle.setOnClickListener {
            showAccountMenuPopup()
        }


        // themesBtn 클릭 시 buttonCreateNote와 recyclerViewNotes의 가시성을 토글합니다.
        val themesBtn = view.findViewById<ImageButton>(R.id.themesBtn)
        themesBtn.setOnClickListener {
            togglePagesVisibility(themesBtn)
        }

        // Create Note 버튼 클릭 시 NoteActivity로 이동
        val buttonCreatePage = view.findViewById<Button>(R.id.buttonCreateNote)
        buttonCreatePage.setOnClickListener {
            showNoteCreationPopup()
        }


        // 최근 워크스페이스 ID를 loadNotesFromFirestore() 함수로 전달하여 해당 워크스페이스에 속한 노트들을 가져옵니다.
        recentWorkspaceId?.let {
            loadPagesFromFirestore(it)
        }

        return view
    }

    override fun onPageClick(page: Page) {
        val intent = Intent(requireContext(), PageActivity::class.java)
        intent.putExtra("page_id", page.id)
        intent.putExtra("page_title", page.title)
        intent.putExtra("page_text", page.text)
        intent.putExtra("page_image_uri", page.imageUri)
        startActivity(intent)
    }


    private fun showPopupAccount() {

        // PopupWindow 생성
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            1200,
            true
        )

        // account_layout 내의 RecyclerView를 찾습니다.
        val recyclerViewWorkSpace = popupView.findViewById<RecyclerView>(R.id.recyclerViewWorkSpace)
        val workSpaceListAdapter = WorkSpaceListAdapter(mutableListOf(), object :
            WorkSpaceListAdapter.OnWorkSpaceClickListener {
            override fun onWorkSpaceClick(workSpace: WorkSpace) {
                // 워크스페이스를 클릭했을 때 처리할 내용을 여기에 작성합니다.
                saveRecentWorkspaceId(workSpace.id)
                val MainIntent = Intent(requireContext(), MainActivity::class.java)
                startActivity(MainIntent)
                requireActivity().finish()
            }
        })
        recyclerViewWorkSpace.adapter = workSpaceListAdapter
        recyclerViewWorkSpace.layoutManager = LinearLayoutManager(requireContext())

        // 파이어스토어에서 워크스페이스 데이터를 가져와서 어댑터에 설정
        loadWorkSpacesForPopup(workSpaceListAdapter)

        // PopupWindow를 화면 아래쪽에 표시합니다.
        popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0)

        // PopupWindow가 바깥을 터치하면 닫히도록 설정합니다.
        popupWindow.isOutsideTouchable = true

        // 팝업 창에서 로그아웃 항목을 클릭했을 때의 동작 정의
        val settingLayoutView = popupView.findViewById<RelativeLayout>(R.id.logoutLayout)
        settingLayoutView.setOnClickListener {
            auth.signOut()
            val loginIntent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(loginIntent)
            requireActivity().finish()
            popupWindow.dismiss() // 팝업 창 닫기
        }
    }

    private fun showAccountMenuPopup() {
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.menu_account, null)

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // 팝업 창이 화면 바깥을 터치하면 닫히도록 설정
        popupWindow.isOutsideTouchable = true

        // 팝업 창을 클릭 가능하도록 설정
        popupWindow.isFocusable = true

        // 팝업 창을 표시할 위치 설정
        popupWindow.showAsDropDown(setting_circle) // settingCircleImageView가 클릭된 위치에 따라 팝업 창이 표시됩니다.

        // 워크스페이스 생성 또는 참여 항목 클릭 시 처리
        val workSpaceLayout = popupView.findViewById<RelativeLayout>(R.id.workSpaceLayout)
        workSpaceLayout.setOnClickListener {
            startActivity(Intent(requireContext(), OrganizationActivity::class.java))
            popupWindow.dismiss() // 팝업 창 닫기
        }

        // 로그아웃 항목 클릭 시 처리
        val logoutLayout = popupView.findViewById<RelativeLayout>(R.id.logout_Layout)
        logoutLayout.setOnClickListener {
            auth.signOut()
            val loginIntent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(loginIntent)
            requireActivity().finish()
            popupWindow.dismiss() // 팝업 창 닫기
        }
    }

    // 파이어스토어에서 워크스페이스 데이터를 가져와서 어댑터에 설정하는 함수
    private fun loadWorkSpacesForPopup(adapter: WorkSpaceListAdapter) {
        val db = FirebaseFirestore.getInstance()
        db.collection("workSpaces")
            .get()
            .addOnSuccessListener { result ->
                val workSpaceList = mutableListOf<WorkSpace>()
                for (document in result) {
                    val workSpaceName = document.getString("workSpaceName") ?: ""
                    val owner = document.getString("owner") ?: ""
                    val id = document.getString("workSpaceId") ?: ""
                    val workSpace = WorkSpace(workSpaceName, owner, id)
                    workSpaceList.add(workSpace)
                }

                // 어댑터에 워크스페이스 데이터 설정
                adapter.setWorkSpaces(workSpaceList)
            }
            .addOnFailureListener { exception ->
                // 쿼리 실패 시 에러 처리
                // 예를 들어, 로그 출력 등
            }
    }



    private fun showPopupMenu() {
        val popupView = layoutInflater.inflate(R.layout.menu_layout, null)
        val popupWindow = PopupWindow(
            popupView,
            700,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // 팝업 창이 화면 바깥을 터치하면 닫히도록 설정
        popupWindow.isOutsideTouchable = true

        // 팝업 창을 뷰의 아래에 표시
        popupWindow.showAsDropDown(menuBtn)

        // 팝업 창에서 각 항목을 클릭할 때의 동작 정의
        val settingLayoutView = popupView.findViewById<RelativeLayout>(R.id.settingLayout)
        settingLayoutView.setOnClickListener {
            // 설정 메뉴 클릭 시 실행할 작업 추가
            popupWindow.dismiss() // 팝업 창 닫기
        }

        val memberLayoutView = popupView.findViewById<RelativeLayout>(R.id.memberLayout)
        memberLayoutView.setOnClickListener {
            // 멤버 메뉴 클릭 시 실행할 작업 추가
            popupWindow.dismiss() // 팝업 창 닫기
        }

        val trashLayoutView = popupView.findViewById<RelativeLayout>(R.id.trashLayout)
        trashLayoutView.setOnClickListener {
            // 휴지통 메뉴 클릭 시 실행할 작업 추가
            popupWindow.dismiss() // 팝업 창 닫기
        }
    }


    private fun showNoteCreationPopup() {
        val inflater = LayoutInflater.from(requireContext())
        val popupView = inflater.inflate(R.layout.note_layout, null)
        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // 팝업 뷰에서 노트 제목을 입력하는 EditText 찾기
        val noteNameEditText = popupView.findViewById<EditText>(R.id.Note_name)

        // 팝업 창이 화면 바깥을 터치하면 닫히도록 설정
        popupWindow.isOutsideTouchable = true

        val cancleButton = popupView.findViewById<Button>(R.id.cancleButton)
        cancleButton.setOnClickListener {
            popupWindow.dismiss()
        }

        // 팝업 창에서 확인 버튼을 클릭했을 때의 동작 정의
        val confirmButton = popupView.findViewById<Button>(R.id.confirmButton)
        confirmButton.setOnClickListener {
            // 사용자가 입력한 노트 제목 가져오기
            val noteTitle = noteNameEditText.text.toString().trim()

            if (noteTitle.isNotEmpty()) {
                // 노트 생성 및 저장
                createNoteInFirestore(noteTitle)

                // 팝업 창 닫기
                popupWindow.dismiss()
            } else {
                // 사용자에게 제목을 입력하도록 메시지 표시 또는 처리
                Toast.makeText(requireContext(), "노트 제목을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 팝업 창을 뷰의 아래에 표시
        popupWindow.showAtLocation(requireView(), Gravity.CENTER, 0, 0)
    }



    private fun createNoteInFirestore(noteTitle: String) {
        // 현재 사용자의 ID 가져오기
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        // 현재 워크스페이스 ID 가져오기 (여기서는 가정하여 사용)
        val organizationId = getRecentWorkspaceId()

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
            "userId" to userId,
            // 기타 필요한 필드 추가
        )

        // notes 컬렉션에 새로운 노트 추가
        notesCollection.document(newNoteId)
            .set(newNote)
            .addOnSuccessListener {
                // 성공적으로 노트가 Firestore에 추가됨
                // 여기에 추가 작업 또는 UI 업데이트를 수행할 수 있음
            }
            .addOnFailureListener { e ->

            }
    }




    private fun togglePagesVisibility(themesBtn: ImageButton) {
        // recyclerViewNotes의 가시성을 토글합니다.
        recyclerView.visibility = if (recyclerView.visibility == View.VISIBLE) {
            View.GONE
        } else {
            View.VISIBLE
        }

        // themesBtn 이미지를 변경합니다.
        val newImageResource = if (recyclerView.visibility == View.VISIBLE) {
            R.drawable.baseline_keyboard_arrow_right_24 // 토글 후 recyclerView가 보이는 경우
        } else {
            R.drawable.baseline_keyboard_arrow_down_24 // 토글 후 recyclerView가 숨겨진 경우
        }

        // 새로운 이미지로 설정합니다.
        themesBtn.setImageResource(newImageResource)
    }


    private fun createPage() {
        val intent = Intent(requireContext(), PageActivity::class.java)
        startActivity(intent)
    }

    private fun loadPagesFromFirestore(recentWorkspaceId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("pages")
            .whereEqualTo("workSpaceId", recentWorkspaceId) // 해당 워크스페이스 ID와 일치하는 노트만 가져오기
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



    private fun displayWorkspaceName(workspaceId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("workSpaces")
            .document(workspaceId)
            .get()
            .addOnSuccessListener { document ->
                val workspaceName = document.getString("workSpaceName")
                // 가져온 워크스페이스 이름을 TextView에 설정합니다.
                workSpaceText.text = workspaceName
            }
            .addOnFailureListener { exception ->
                // 워크스페이스 이름을 가져오지 못한 경우 처리할 내용을 여기에 작성합니다.
            }
    }

    private fun displayUserEmail() {
        // FirebaseAuth 인스턴스를 사용하여 현재 사용자를 가져옵니다.
        val user: FirebaseUser? = auth.currentUser
        // 사용자가 로그인되어 있는지 확인합니다.
        user?.let {
            // 사용자가 로그인되어 있다면, 이메일을 가져와 TextView에 설정합니다.
            val userEmail = user.email
            emailTextView.text = userEmail
            emailTextView1.text = userEmail
        }
    }

    // 최근 워크스페이스 ID를 저장하고 불러오기
    private fun saveRecentWorkspaceId(workspaceId: String) {
        SharedPreferencesUtil.saveRecentWorkspaceId(requireContext(), workspaceId)
    }

    private fun getRecentWorkspaceId(): String? {
        return SharedPreferencesUtil.getRecentWorkspaceId(requireContext())
    }
}

