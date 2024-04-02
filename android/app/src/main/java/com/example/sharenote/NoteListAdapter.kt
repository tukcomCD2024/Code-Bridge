import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharenote.Note
import com.example.sharenote.R

class NoteListAdapter(private val noteList: List<Note>, private val onItemClick: (String) -> Unit) :
    RecyclerView.Adapter<NoteListAdapter.NoteViewHolder>() {
    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        // 뷰홀더 초기화 및 클릭 리스너 설정
        init {
            itemView.setOnClickListener(this)
        }

        // 아이템 클릭 시 호출되는 메서드
        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val clickedNoteId = noteList[position].noteId
                onItemClick(clickedNoteId)
            }
        }

        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentItem = noteList[position]
        holder.titleTextView.text = currentItem.title

    }

    override fun getItemCount() = noteList.size
}