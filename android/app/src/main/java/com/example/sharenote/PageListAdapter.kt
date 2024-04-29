import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharenote.CheckPage
import com.example.sharenote.Page
import com.example.sharenote.R
import com.google.firebase.firestore.FirebaseFirestore

class PageListAdapter(private val pages: MutableList<CheckPage>, private val onNoteClickListener: OnPageClickListener) :
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
        holder.idViewText.text = currentPage.id
        holder.userViewText.text = currentPage.createUser
        holder.atViewText.text = currentPage.createdAt



        holder.buttonDeletePage.setOnClickListener {
            deletePage(holder.adapterPosition)
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
        val idViewText: TextView = itemView.findViewById(R.id.idViewText)
        val userViewText: TextView = itemView.findViewById(R.id.userViewText)
        val atViewText: TextView = itemView.findViewById(R.id.atViewText)
        val buttonDeletePage: Button = itemView.findViewById(R.id.buttonDeletePage)
    }
}
