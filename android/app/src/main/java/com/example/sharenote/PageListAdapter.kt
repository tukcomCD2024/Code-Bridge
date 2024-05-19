import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharenote.Page
import com.example.sharenote.R
import com.google.firebase.firestore.FirebaseFirestore

class PageListAdapter(private val pages: MutableList<Page>, private val onNoteClickListener: OnPageClickListener) :
    RecyclerView.Adapter<PageListAdapter.PageViewHolder>() {

    interface OnPageClickListener {
        fun onPageClick(page: Page)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_page, parent, false)
        return PageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val currentPage = pages[position]
        holder.titleViewText.text = currentPage.title
        holder.textViewText.text = currentPage.text

        // 이미지 URI가 있을 경우 Glide를 사용하여 이미지를 로드하여 표시합니다.
        currentPage.imageUri?.let { uri ->
            holder.imageViewImage.visibility = View.VISIBLE
            Glide.with(holder.itemView)
                .load(uri) // 이미지 URI를 직접 전달합니다.
                .into(holder.imageViewImage)
        } ?: run {
            // 이미지 URI가 없는 경우 이미지뷰를 숨깁니다.
            holder.imageViewImage.visibility = View.GONE
        }

        holder.buttonDeletePage.setOnClickListener {
            deletePage(holder.adapterPosition)
        }
        // 노트를 클릭하면 해당 노트의 정보를 전달합니다.
        holder.itemView.setOnClickListener {
            onNoteClickListener.onPageClick(currentPage)
        }

    }

    override fun getItemCount() = pages.size

    private fun deletePage(position: Int) {
        val db = FirebaseFirestore.getInstance()
        val pageId = pages[position].id // Note 클래스에 ID 필드가 있다고 가정
        db.collection("pages").document(pageId)
            .delete()
            .addOnSuccessListener {
                // Firestore에서 문서 삭제 성공 후 RecyclerView에서 해당 아이템 제거
                pages.removeAt(position)
                notifyItemRemoved(position)
            }
            .addOnFailureListener { exception ->
                // 삭제 실패 처리
            }
    }


    inner class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleViewText: TextView = itemView.findViewById(R.id.titleViewText)
        val textViewText: TextView = itemView.findViewById(R.id.textViewText)
        val imageViewImage: ImageView = itemView.findViewById(R.id.imageViewImage)
        val buttonDeletePage: Button = itemView.findViewById(R.id.buttonDeletePage)
    }
}
