import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharenote.Note
import com.example.sharenote.R

class NoteListAdapter(private val onItemClick: (String) -> Unit) :
    RecyclerView.Adapter<NoteListAdapter.NoteViewHolder>() {

    private val noteList = mutableListOf<Note>()

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val clickedNoteId = noteList[position].Id
                onItemClick(clickedNoteId)
            }
        }

        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val noteNumberTextView: TextView = itemView.findViewById(R.id.noteNumberTextView) // 순번을 표시할 텍스트뷰
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentItem = noteList[position]

        // 아이템의 순번은 position을 기반으로 계산하여 1부터 시작하도록 설정
        val noteNumber = position + 1
        holder.noteNumberTextView.text = noteNumber.toString()

        // 아이템의 제목 설정
        holder.titleTextView.text = currentItem.title
    }

    override fun getItemCount() = noteList.size

    fun setNotes(notes: List<Note>) {
        noteList.clear()
        noteList.addAll(notes)
        notifyDataSetChanged()
    }
}
