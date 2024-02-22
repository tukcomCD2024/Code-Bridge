import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharenote.Note
import com.example.sharenote.R

class NoteListAdapter(private val notes: List<Note>) :
    RecyclerView.Adapter<NoteListAdapter.NoteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = notes[position]
        holder.textViewText.text = currentNote.text

        // 이미지 URI가 있을 경우 Glide를 사용하여 이미지를 로드하여 표시합니다.
        currentNote.imageUri?.let { uri ->
            holder.imageViewImage.visibility = View.VISIBLE
            Glide.with(holder.itemView)
                .load(Uri.parse(uri))
                .into(holder.imageViewImage)
        } ?: run {
            // 이미지 URI가 없는 경우 이미지뷰를 숨깁니다.
            holder.imageViewImage.visibility = View.GONE
        }
    }

    override fun getItemCount() = notes.size

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewText: TextView = itemView.findViewById(R.id.textViewText)
        val imageViewImage: ImageView = itemView.findViewById(R.id.imageViewImage)
    }
}
