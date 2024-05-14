import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharenote.Page
import com.example.sharenote.PageCheck
import com.example.sharenote.R
import com.google.firebase.firestore.FirebaseFirestore

class PageListAdapter(private val pages: MutableList<Page>, private val onPageClickListener: OnPageClickListener) :
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
        holder.pageNumberTextView.text = "Page ${position + 1}" // 순번을 설정합니다.
        holder.atViewText.text = currentPage.createdAt

        // 페이지를 클릭하면 해당 페이지의 정보를 전달합니다.
        holder.itemView.setOnClickListener {
            onPageClickListener.onPageClick(currentPage)
        }
    }

    override fun getItemCount() = pages.size


    fun setPages(pages: List<Page>) {
        this.pages.clear()
        this.pages.addAll(pages)
        notifyDataSetChanged()
    }

    private fun deletePage(position: Int) {
        val db = FirebaseFirestore.getInstance()
        val pageId = pages[position].id
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
        val pageNumberTextView: TextView = itemView.findViewById(R.id.pageNumberTextView) // 순번을 표시할 텍스트뷰
        val atViewText: TextView = itemView.findViewById(R.id.atViewText)
    }
}
