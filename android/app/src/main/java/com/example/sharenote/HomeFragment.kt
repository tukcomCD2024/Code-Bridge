import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.sharenote.LoginActivity
import com.example.sharenote.MyPageActivity
import com.example.sharenote.NoteActivity
import com.example.sharenote.R
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        auth = FirebaseAuth.getInstance()

        val buttonCreateNote = view.findViewById<Button>(R.id.buttonCreateNote)
        buttonCreateNote.setOnClickListener {
            createNote()
        }

        val myPageButton = view.findViewById<Button>(R.id.myPageButton)
        myPageButton.setOnClickListener {
            val myPageIntent = Intent(requireContext(), MyPageActivity::class.java)
            startActivity(myPageIntent)
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

        return view
    }

    private fun createNote() {
        val intent = Intent(requireContext(), NoteActivity::class.java)
        startActivity(intent)
    }
}
