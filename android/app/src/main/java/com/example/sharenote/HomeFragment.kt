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

        // account_layout을 팝업으로 사용하기 위해 팝업 뷰를 초기화합니다.
        popupView = layoutInflater.inflate(R.layout.account_layout, null)
        emailTextView1 = popupView.findViewById(R.id.email)

        setting_circle = popupView.findViewById(R.id.setting_circle)



        // emailTextView를 찾습니다.
        emailTextView = view.findViewById(R.id.emailtextView)



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

        // Logout 버튼 클릭 시 로그아웃 처리
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            auth.signOut()
            val loginIntent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(loginIntent)
            requireActivity().finish()
        }

        loadNotesFromFirestore()
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

        popupWindow.isOutsideTouchable = true

        // PopupWindow를 표시할 위치 설정 (아래쪽에 표시)
        popupWindow?.showAtLocation(view, Gravity.BOTTOM, 0, 0)

        // 팝업 창에서 각 항목을 클릭할 때의 동작 정의
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

    private fun loadNotesFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("notes")
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
}
