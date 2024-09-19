import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coop.sharenote.Note
import com.coop.sharenote.R

class SearchNoteListAdapter_f(private val onItemClick: (String) -> Unit) :
    RecyclerView.Adapter<SearchNoteListAdapter_f.NoteViewHolder>() {

    private val noteList = mutableListOf<Note>()
    private var filteredNoteList = mutableListOf<Note>()

    init {
        filteredNoteList.addAll(noteList)
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val clickedNoteId = filteredNoteList[position].Id
                onItemClick(clickedNoteId)
            }
        }

        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_search_note_f, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentItem = filteredNoteList[position]
        // 아이템의 제목 설정
        holder.titleTextView.text = currentItem.title
    }

    override fun getItemCount() = filteredNoteList.size

    fun setNotes(notes: List<Note>) {
        noteList.clear()
        noteList.addAll(notes)
        filter("")
    }

    fun filter(query: String) {
        filteredNoteList.clear()
        if (query.isEmpty()) {
            filteredNoteList.addAll(noteList)
        } else {
            filteredNoteList.addAll(noteList.filter { it.title.contains(query, ignoreCase = true) })
        }
        notifyDataSetChanged()
    }
}
