import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharenote.ApiService
import com.example.sharenote.CheckOrganization
import com.example.sharenote.Contribution
import com.example.sharenote.CreateNoteActivity
import com.example.sharenote.CreateQuiz
import com.example.sharenote.LoginActivity
import com.example.sharenote.MainActivity
import com.example.sharenote.Note
import com.example.sharenote.NoteActivity
import com.example.sharenote.NoteRecentListAdapter
import com.example.sharenote.OrganizationActivity
import com.example.sharenote.PaintActivity
import com.example.sharenote.QuizActivity
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

    private lateinit var recyclerViewRecentNotes: RecyclerView
    private lateinit var recentNotesAdapter: NoteRecentListAdapter

    private lateinit var emailTextView: TextView
    private lateinit var menuBtn: ImageButton
    private lateinit var profileForm: RelativeLayout

    private lateinit var listLayout: RelativeLayout
    private lateinit var listLayout_1: ImageView

    private lateinit var MoveDraw: Button
    private lateinit var Quiz : Button
    private lateinit var Cont : Button

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
            SharedPreferencesUtil.saveRecentNoteIds(requireContext(), noteId)
            val intent = Intent(requireContext(), NoteActivity::class.java)
            startActivity(intent)
        }

        recyclerViewRecentNotes = view.findViewById(R.id.recyclerViewRecentNotes)
        recyclerViewRecentNotes.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        recentNotesAdapter = NoteRecentListAdapter { noteId ->
            saveRecentNoteId(noteId)
            SharedPreferencesUtil.saveRecentNoteIds(requireContext(), noteId)
            val intent = Intent(requireContext(), NoteActivity::class.java)
            startActivity(intent)
        }

        recyclerView.adapter = noteListAdapter
        recyclerViewRecentNotes.adapter = recentNotesAdapter

        menuBtn = view.findViewById(R.id.menuBtn)
        profileForm = view.findViewById(R.id.profileForm)

        listLayout = view.findViewById(R.id.listLayout)
        listLayout_1 = view.findViewById(R.id.listLayout_1)

        MoveDraw = view.findViewById(R.id.MoveDraw)
        Quiz = view.findViewById(R.id.Quiz)
        Cont = view.findViewById(R.id.Contribution)


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

        Quiz.setOnClickListener {
            val intent = Intent(requireContext(), QuizActivity::class.java)
            startActivity(intent)
        }

        Cont.setOnClickListener {
            val intent = Intent(requireContext(), Contribution::class.java)
            startActivity(intent)
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

        }


        listLayout_1.setOnClickListener {
            // recyclerViewNotes의 가시성을 토글
            if (recyclerView.visibility == View.VISIBLE) {
                animateView(false)
            } else {
                animateView(true)
            }
        }

        // Create Note 버튼 클릭 시 NoteActivity로 이동
        val buttonCreateNote = view.findViewById<ImageView>(R.id.listLayout_4)
        buttonCreateNote.setOnClickListener {
            createNote()
        }

        // 최근 워크스페이스 ID를 loadNotesFromFirestore() 함수로 전달하여 해당 워크스페이스에 속한 노트들을 가져옵니다.
        recentWorkspaceId?.let {
            val userId = SharedPreferencesUtil.getUserId(requireContext()) ?: ""
            loadNotesFromMongoDB(it, userId)
        }
        loadRecentNotes()

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
        val dialog = Dialog(requireContext())
        val popupView = LayoutInflater.from(requireContext()).inflate(R.layout.account_layout, null)
        dialog.setContentView(popupView)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = 1200
        layoutParams.gravity = Gravity.BOTTOM
        dialog.window?.attributes = layoutParams

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val recyclerViewWorkSpace = popupView.findViewById<RecyclerView>(R.id.recyclerViewWorkSpace)
        val workSpaceListAdapter = WorkSpaceListAdapter(mutableListOf(), object : WorkSpaceListAdapter.OnWorkSpaceClickListener {
            override fun onWorkSpaceClick(workSpace: WorkSpace) {
                saveRecentWorkspaceId(workSpace.id)
                saveRecentWorkspaceName(workSpace.name)
                val mainIntent = Intent(requireContext(), MainActivity::class.java)
                startActivity(mainIntent)
                requireActivity().finish()
            }
        })
        recyclerViewWorkSpace.adapter = workSpaceListAdapter
        recyclerViewWorkSpace.layoutManager = LinearLayoutManager(requireContext())

        loadWorkSpacesForPopup(workSpaceListAdapter)

        val settingLayoutView = popupView.findViewById<RelativeLayout>(R.id.logoutLayout)
        settingLayoutView.setOnClickListener {
            auth.signOut()
            val loginIntent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(loginIntent)
            requireActivity().finish()
            dialog.dismiss()
        }

        val emailTextView1 = popupView.findViewById<TextView>(R.id.email)
        val userEmail = SharedPreferencesUtil.getUserEmail(requireContext())
        emailTextView1.text = userEmail

        val settingCircle = popupView.findViewById<ImageView>(R.id.setting_circle)
        settingCircle.setOnClickListener {
            showAccountMenuPopup(settingCircle) // setting_circle을 전달하여 팝업 창이 해당 뷰의 아래쪽에 표시
        }

        dialog.show()
    }



    private fun showAccountMenuPopup(anchorView: View) {
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
        popupWindow.showAsDropDown(anchorView, 0, 0) // setting_circle 아래에 표시

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







    private fun loadWorkSpacesForPopup(adapter: WorkSpaceListAdapter) {
        val currentUserEmail = getUserId()

        currentUserEmail?.let { email ->
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    // Retrofit을 사용하여 HTTP 요청을 보냅니다.
                    val accessToken = SharedPreferencesUtil.getAccessToken(requireContext()) ?: ""

                    val organizationList = RetrofitClient.apiService.getOrganization(email, accessToken)

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



    private fun animateView(visible: Boolean) {
        // 애니메이션 생성 및 설정
        val rotationFrom = if (visible) -90f else 0f
        val rotationTo = if (visible) 0f else -90f
        val rotationAnimation = ObjectAnimator.ofFloat(listLayout_1, "rotation", rotationFrom, rotationTo)
        rotationAnimation.duration = 200 // 애니메이션의 지속 시간을 설정합니다 (밀리초 단위)

        // 애니메이션 시작
        rotationAnimation.start()

        // recyclerViewNotes의 가시성 변경
        recyclerView.visibility = if (visible) View.VISIBLE else View.GONE
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
                val accessToken = SharedPreferencesUtil.getAccessToken(requireContext()) ?: ""

                // Retrofit을 사용하여 HTTP 요청을 보냅니다.
                val response = RetrofitClient.apiService.getOrganization(userId, accessToken)

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


    private fun loadRecentNotes() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val recentNoteIds = SharedPreferencesUtil.getRecentNoteIds(requireContext())
                Log.d("notes", "Recent Note IDs: $recentNoteIds")
                if (recentNoteIds.isEmpty()) return@launch

                val accessToken = SharedPreferencesUtil.getAccessToken(requireContext()) ?: ""
                val userId = SharedPreferencesUtil.getUserId(requireContext()) ?: ""

                val response = RetrofitClient.apiService.getOrganization(userId, accessToken)
                val allNotes = mutableListOf<Note>()

                for (organization in response) {
                    for (noteData in organization.notes) {
                        val note = Note(
                            Id = noteData.id,
                            createUser = organization.owner,
                            title = noteData.title,
                            noteImageUrl = noteData.noteImageUrl
                        )
                        allNotes.add(note)
                    }
                }

                val recentNotes = allNotes.filter { recentNoteIds.contains(it.Id) }.take(3)
                Log.d(TAG, "Recent Notes: $recentNotes")

                withContext(Dispatchers.Main) {
                    recentNotesAdapter.setNotes(recentNotes)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading recent notes", e)
            }
        }
    }









    private fun displayWorkspaceName(workspaceId: String) {
        val currentUserEmail = getUserId()

        currentUserEmail?.let { email ->
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    // Retrofit을 사용하여 HTTP 요청을 보냅니다.
                    val accessToken = SharedPreferencesUtil.getAccessToken(requireContext()) ?: ""
                    val organizationList = RetrofitClient.apiService.getOrganization(email, accessToken)

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

    private fun saveRecentWorkspaceName(workspaceName: String) {
        SharedPreferencesUtil.saveRecentWorkspaceName(requireContext(), workspaceName)
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

