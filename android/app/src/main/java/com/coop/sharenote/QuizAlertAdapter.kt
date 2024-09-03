import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coop.sharenote.QuizList
import com.coop.sharenote.R

class QuizAlertAdapter(private val quizList: List<QuizList>, private val listener: OnItemClickListener)
    : RecyclerView.Adapter<QuizAlertAdapter.QuizViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(quizId: String)
    }

    class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val quizTitleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val nicknameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_quiz_alert, parent, false)
        return QuizViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val quiz = quizList[position]
        holder.quizTitleTextView.text = quiz.quizTitle
        holder.nicknameTextView.text = quiz.nickname+"님이 새로 문제를 출제하였습니다!"


        // 아이템 클릭 이벤트 설정
        holder.itemView.setOnClickListener {
            listener.onItemClick(quiz.quizId) // 클릭한 퀴즈의 아이디를 전달
        }
    }

    override fun getItemCount(): Int {
        return quizList.size
    }
}
