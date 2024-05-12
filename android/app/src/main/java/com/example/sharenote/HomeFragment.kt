import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharenote.ApiService
import com.example.sharenote.CheckOrganization
import com.example.sharenote.CreateNoteActivity
import com.example.sharenote.LoginActivity
import com.example.sharenote.MainActivity
import com.example.sharenote.Note
import com.example.sharenote.NoteActivity
import com.example.sharenote.OrganizationActivity
import com.example.sharenote.PaintActivity
import com.example.sharenote.R
import com.example.sharenote.RetrofitClient
import com.example.sharenote.SharedPreferencesUtil
import com.example.sharenote.WorkSpace
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var noteListAdapter: NoteListAdapter
    private lateinit var emailTextView: TextView
    private lateinit var menuBtn: ImageButton
    private lateinit var profileForm: RelativeLayout

    private lateinit var MoveDraw: Button

    private lateinit var emailTextView1: TextView
    private lateinit var workSpaceText: TextView
    private lateinit var popupView: View // 팝업 뷰
    private lateinit var setting_circle: ImageView


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        auth = FirebaseAuth.getInstance()
        recyclerView = view.findViewById(R.id.recyclerViewNotes)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager

        // noteListAdapter를 초기화합니다.
        noteListAdapter = NoteListAdapter { noteId ->
            // 노트 아이템 클릭 시 NoteActivity로 이동
            saveRecentNoteId(noteId) // 클릭된 노트의 ID를 저장합니다.
            val intent = Intent(requireContext(), NoteActivity::class.java)
            startActivity(intent)
        }


        recyclerView.adapter = noteListAdapter
        menuBtn = view.findViewById(R.id.menuBtn)
        profileForm = view.findViewById(R.id.profileForm)

        MoveDraw = view.findViewById(R.id.MoveDraw)


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
        val userEmail = SharedPreferencesUtil.getUserEmail(requireContext())
        emailTextView.text = userEmail
        emailTextView1.text = userEmail

        recentWorkspaceId?.let {
            displayWorkspaceName(it)
        }

        MoveDraw.setOnClickListener {
            val intent2 = Intent(requireContext(), PaintActivity::class.java)
            startActivityForResult(intent2, 1)
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
            createNote()
        }

        // 최근 워크스페이스 ID를 loadNotesFromFirestore() 함수로 전달하여 해당 워크스페이스에 속한 노트들을 가져옵니다.
        recentWorkspaceId?.let {
            val userId = SharedPreferencesUtil.getUserId(requireContext()) ?: ""
            loadNotesFromMongoDB(it, userId)
        }

        return view
    }

    // 이게 없어서 지금까지 계속 튕김

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) {
            val imageUri = data?.getStringExtra("imageUrl")
            Log.e("imageUrl", imageUri ?: "")
        }
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

        // MongoDB에서 워크스페이스 데이터를 가져와서 어댑터에 설정
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

    /*
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
    }*/

    private fun loadWorkSpacesForPopup(adapter: WorkSpaceListAdapter) {
        val currentUserEmail = getUserId()

        currentUserEmail?.let { email ->
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    // Retrofit을 사용하여 HTTP 요청을 보냅니다.
                    val organizationList = RetrofitClient.apiService.getOrganization(email)

                    // 받아온 organization 데이터를 WorkSpace 객체로 변환하여 어댑터에 추가합니다.
                    val workSpaceList = organizationList.map { organization ->
                        WorkSpace(organization.name, organization.owner, organization.id)
                    }

                    // 어댑터에 워크스페이스 데이터 설정
                    withContext(Dispatchers.Main) {
                        adapter.setWorkSpaces(workSpaceList)
                    }
                } catch (e: Exception) {
                    // 실패한 경우 처리
                    Log.e(TAG, "Error getting organizations", e)
                }
            }
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


    private fun createNote() {
        val intent = Intent(requireContext(), CreateNoteActivity::class.java)
        startActivity(intent)
    }


    /*
    private fun loadNotesFromFirestore(recentWorkspaceId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("notes")
            .whereEqualTo("organizationId", recentWorkspaceId) // 해당 조직 ID와 일치하는 노트만 가져오기
            .get()
            .addOnSuccessListener { result ->
                val notes = mutableListOf<Note>()
                for (document in result) {
                    val noteId = document.getString("noteId") ?: ""
                    val title = document.getString("title") ?: ""
                    val organizationId = document.getString("organizationId") ?: ""
                    val userId = document.getString("userId") ?: ""
                    val noteImageUrl = document.getString("noteImageUrl") ?: ""
                    val note = Note(organizationId, title, userId, noteImageUrl, noteId)
                    notes.add(note) // 새로운 노트를 어댑터에 추가합니다.
                }
                // 어댑터에 데이터 설정
                noteListAdapter.setNotes(notes)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting notes:", exception)
            }
    }*/

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
                    noteListAdapter.setNotes(notes)
                }
            } catch (e: Exception) {
                // 오류 처리
                // e.printStackTrace()
                // 예상치 못한 오류가 발생했을 때
            }
        }
    }









    private fun displayWorkspaceName(workspaceId: String) {
        val currentUserEmail = getUserId()

        currentUserEmail?.let { email ->
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    // Retrofit을 사용하여 HTTP 요청을 보냅니다.
                    val organizationList = RetrofitClient.apiService.getOrganization(email)

                    // 받아온 organization 데이터 중에서 workspaceId와 일치하는 Organization을 찾습니다.
                    val organization = organizationList.find { it.id == workspaceId }

                    // 찾은 Organization의 이름을 가져옵니다.
                    val workspaceName = organization?.name

                    // 가져온 워크스페이스 이름을 TextView에 설정합니다.
                    withContext(Dispatchers.Main) {
                        workSpaceText.text = workspaceName
                    }
                } catch (e: Exception) {
                    // 실패한 경우 처리
                    Log.e(TAG, "Error getting workspace name", e)
                }
            }
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

    private fun getUserId(): String? {
        return SharedPreferencesUtil.getUserId(requireContext())
    }

    private fun saveRecentNoteId(noteId: String) {
        SharedPreferencesUtil.saveRecentNoteId(requireContext(), noteId)
    }
}

