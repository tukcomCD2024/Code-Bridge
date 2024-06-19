import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharenote.QuizList
import com.example.sharenote.R

class QuizAdapter(private val quizList: MutableList<QuizList>, private val listener: OnItemClickListener)
    : RecyclerView.Adapter<QuizAdapter.QuizViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(quizId: String)
    }

    class QuizViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val quizTitleTextView: TextView = itemView.findViewById(R.id.quizTitleTextView)
        val nicknameTextView: TextView = itemView.findViewById(R.id.nicknameTextView)
        val newImageView: ImageView = itemView.findViewById(R.id.newImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuizViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_quiz, parent, false)
        return QuizViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: QuizViewHolder, position: Int) {
        val quiz = quizList[position]
        holder.quizTitleTextView.text = quiz.quizTitle
        holder.nicknameTextView.text = quiz.nickname

        // correct 값을 기반으로 배경색 변경
        when (quiz.correct) {
            1 -> {
                holder.itemView.setBackgroundResource(R.color.lightblue)
                holder.newImageView.visibility = View.GONE
            }
            0 -> {
                holder.itemView.setBackgroundResource(R.color.lightcoral)
                holder.newImageView.visibility = View.GONE
            }
            else -> {
                holder.itemView.setBackgroundResource(android.R.color.transparent)
                holder.newImageView.visibility = View.VISIBLE
            }
        }

        // 아이템 클릭 이벤트 설정
        holder.itemView.setOnClickListener {
            listener.onItemClick(quiz.quizId) // 클릭한 퀴즈의 아이디를 전달
        }
    }

    override fun getItemCount(): Int {
        return quizList.size
    }

    fun setQuizzes(quizzes: List<QuizList>) {
        quizList.clear()
        quizList.addAll(quizzes)
        notifyDataSetChanged()
    }
}
