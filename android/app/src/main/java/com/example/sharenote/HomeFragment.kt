import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharenote.LoginActivity
import com.example.sharenote.MyPageActivity
import com.example.sharenote.Note
import com.example.sharenote.NoteActivity
import com.example.sharenote.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var noteListAdapter: NoteListAdapter
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
        noteListAdapter = NoteListAdapter(notes)
        recyclerView.adapter = noteListAdapter


        val buttonCreateNote = view.findViewById<Button>(R.id.buttonCreateNote)
        buttonCreateNote.setOnClickListener {
            createNote()
        }


        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener {
            // Firebase에서 로그아웃
            auth.signOut()

            // 로그인 화면으로 이동
            val loginIntent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(loginIntent)

            // 현재 액티비티 종료 (메인 페이지에서 뒤로가기 시 로그인 화면으로 가도록)
            requireActivity().finish()
        }

        loadNotesFromFirestore()

        return view
    }

    private fun createNote() {
        val intent = Intent(requireContext(), NoteActivity::class.java)
        startActivity(intent)
    }

    private fun initRecyclerView() {
        // RecyclerView 설정
        recyclerView.layoutManager = LinearLayoutManager(activity)
        notes = mutableListOf()
        noteListAdapter = NoteListAdapter(notes)
        recyclerView.adapter = noteListAdapter
    }

    private fun loadNotesFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("notes")
            .get()
            .addOnSuccessListener { result ->
                notes.clear()
                for (document in result) {
                    val noteText = document.getString("text") ?: ""
                    val noteImageUri = document.getString("imageUri") ?: ""
                    val note = Note(noteText, noteImageUri)
                    notes.add(note)
                }
                noteListAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle any errors
                // Log.e(TAG, "Error getting documents: ", exception)
            }
    }
}
