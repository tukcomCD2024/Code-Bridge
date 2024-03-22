import android.content.Intent
import android.os.Bundle
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
import com.example.sharenote.LoginActivity
import com.example.sharenote.Note
import com.example.sharenote.NoteActivity
import com.example.sharenote.OrganizationActivity
import com.example.sharenote.R
import com.example.sharenote.SharedPreferencesUtil
import com.example.sharenote.WorkSpace
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment(), NoteListAdapter.OnNoteClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var noteListAdapter: NoteListAdapter
    private lateinit var emailTextView: TextView
    private lateinit var menuBtn: ImageButton
    private lateinit var profileForm: RelativeLayout

    private lateinit var emailTextView1: TextView
    private lateinit var popupView: View // 팝업 뷰
    private lateinit var setting_circle: ImageView

    private var notes: MutableList<Note> = mutableListOf()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        auth = FirebaseAuth.getInstance()
        recyclerView = view.findViewById(R.id.recyclerViewNotes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        noteListAdapter = NoteListAdapter(notes, this)
        recyclerView.adapter = noteListAdapter
        menuBtn = view.findViewById(R.id.menuBtn)
        profileForm = view.findViewById(R.id.profileForm)

        // account_layout을 팝업으로 사용하기 위해 팝업 뷰를 초기화
        popupView = layoutInflater.inflate(R.layout.account_layout, null)
        emailTextView1 = popupView.findViewById(R.id.email)

        setting_circle = popupView.findViewById(R.id.setting_circle)

        // emailTextView를 찾습니다.
        emailTextView = view.findViewById(R.id.emailtextView)

        // 최근에 방문한 워크스페이스 ID를 불러옵니다.
        val recentWorkspaceId = getRecentWorkspaceId()

        // 사용자 이메일을 표시합니다.
        displayUserEmail()

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
            toggleNotesVisibility(themesBtn)
        }

        // Create Note 버튼 클릭 시 NoteActivity로 이동
        val buttonCreateNote = view.findViewById<Button>(R.id.buttonCreateNote)
        buttonCreateNote.setOnClickListener {
            createNote()
        }


        // 최근 워크스페이스 ID를 loadNotesFromFirestore() 함수로 전달하여 해당 워크스페이스에 속한 노트들을 가져옵니다.
        recentWorkspaceId?.let {
            loadNotesFromFirestore(it)
        }

        return view
    }

    override fun onNoteClick(note: Note) {
        val intent = Intent(requireContext(), NoteActivity::class.java)
        intent.putExtra("note_id", note.id)
        intent.putExtra("note_title", note.title)
        intent.putExtra("note_text", note.text)
        intent.putExtra("note_image_uri", note.imageUri)
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


    private fun toggleNotesVisibility(themesBtn: ImageButton) {
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
        val intent = Intent(requireContext(), NoteActivity::class.java)
        startActivity(intent)
    }

    private fun loadNotesFromFirestore(recentWorkspaceId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("notes")
            .whereEqualTo("workSpaceId", recentWorkspaceId) // 해당 워크스페이스 ID와 일치하는 노트만 가져오기
            .get()
            .addOnSuccessListener { result ->
                notes.clear()
                for (document in result) {
                    val noteID = document.getString("id") ?: ""
                    val noteTitle = document.getString("title") ?:""
                    val noteText = document.getString("text") ?: ""
                    val noteImageUri = document.getString("imageUri") ?: ""
                    val note = Note(noteID, noteTitle, noteText, noteImageUri)
                    notes.add(note)
                }
                noteListAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                // Log.e(TAG, "Error getting documents: ", exception)
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

