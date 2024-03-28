import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharenote.Note
import com.example.sharenote.R
import com.google.firebase.firestore.FirebaseFirestore

class NoteListAdapter(private val notes: MutableList<Note>,private val onNoteClickListener: OnNoteClickListener) :
    RecyclerView.Adapter<NoteListAdapter.NoteViewHolder>() {

    interface OnNoteClickListener {
        fun onNoteClick(note: Note)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = notes[position]
        holder.titleViewText.text = currentNote.title
        holder.textViewText.text = currentNote.text

        // 이미지 URI가 있을 경우 Glide를 사용하여 이미지를 로드하여 표시합니다.
        currentNote.imageUri?.let { uri ->
            holder.imageViewImage.visibility = View.VISIBLE
            Glide.with(holder.itemView)
                .load(uri) // 이미지 URI를 직접 전달합니다.
                .into(holder.imageViewImage)
        } ?: run {
            // 이미지 URI가 없는 경우 이미지뷰를 숨깁니다.
            holder.imageViewImage.visibility = View.GONE
        }

        holder.buttonDeleteNote.setOnClickListener {
            deleteNote(holder.adapterPosition)
        }
        // 노트를 클릭하면 해당 노트의 정보를 전달합니다.
        holder.itemView.setOnClickListener {
            onNoteClickListener.onNoteClick(currentNote)
        }

    }

    override fun getItemCount() = notes.size

    private fun deleteNote(position: Int) {
        val db = FirebaseFirestore.getInstance()
        val noteId = notes[position].id // Note 클래스에 ID 필드가 있다고 가정
        db.collection("notes").document(noteId)
            .delete()
            .addOnSuccessListener {
                // Firestore에서 문서 삭제 성공 후 RecyclerView에서 해당 아이템 제거
                notes.removeAt(position)
                notifyItemRemoved(position)
            }
            .addOnFailureListener { exception ->
                // 삭제 실패 처리
            }
    }


    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleViewText: TextView = itemView.findViewById(R.id.titleViewText)
        val textViewText: TextView = itemView.findViewById(R.id.textViewText)
        val imageViewImage: ImageView = itemView.findViewById(R.id.imageViewImage)
        val buttonDeleteNote: Button = itemView.findViewById(R.id.buttonDeleteNote)
    }
}
