import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharenote.LoginActivity
import com.example.sharenote.Note
import com.example.sharenote.NoteActivity
import com.example.sharenote.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment(), NoteListAdapter.OnNoteClickListener {

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
        noteListAdapter = NoteListAdapter(notes, this)
        recyclerView.adapter = noteListAdapter

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
        intent.putExtra("note_text", note.text)
        intent.putExtra("note_image_uri", note.imageUri)
        startActivity(intent)
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
                    val noteText = document.getString("text") ?: ""
                    val noteImageUri = document.getString("imageUri") ?: ""
                    val note = Note(noteID, noteText, noteImageUri)
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
