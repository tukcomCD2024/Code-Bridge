import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coop.sharenote.Page
import com.coop.sharenote.R
import com.google.firebase.firestore.FirebaseFirestore

class PageListAdapter(
    private val pages: MutableList<Page>,
    private val onPageClickListener: OnPageClickListener,
    private val onSettingClickListener: OnSettingClickListener,
    private val onCreatePageClickListener: OnCreatePageClickListener // CreatePage 버튼 클릭 리스너 추가
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_CREATE_PAGE = 0
        private const val VIEW_TYPE_PAGE_ITEM = 1
    }

    interface OnPageClickListener {
        fun onPageClick(page: Page)
    }

    interface OnSettingClickListener {
        fun onSettingClick(page: Page, position: Int)
    }

    interface OnCreatePageClickListener {
        fun onCreatePageClick()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_CREATE_PAGE else VIEW_TYPE_PAGE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_CREATE_PAGE) {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_create_page, parent, false)
            CreatePageViewHolder(itemView)
        } else {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_page, parent, false)
            PageViewHolder(itemView)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CreatePageViewHolder) {
            holder.bind(onCreatePageClickListener)
        } else if (holder is PageViewHolder) {
            val currentPage = pages[position - 1] // 첫 번째 항목이 CreatePage이므로 페이지 인덱스는 -1
            holder.pageNumberTextView.text = "Page $position" // 순번 설정
            holder.atViewText.text = currentPage.createdAt

            holder.pageLayout.setOnClickListener {
                onPageClickListener.onPageClick(currentPage)
            }

            holder.settingLayout.setOnClickListener {
                onSettingClickListener.onSettingClick(currentPage, position - 1)
            }
        }
    }

    override fun getItemCount() = pages.size + 1 // CreatePage 버튼을 포함하기 위해 +1

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
                pages.removeAt(position)
                notifyItemRemoved(position)
            }
            .addOnFailureListener { exception ->
                // 삭제 실패 처리
            }
    }

    inner class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pageNumberTextView: TextView = itemView.findViewById(R.id.pageNumberTextView)
        val atViewText: TextView = itemView.findViewById(R.id.atViewText)
        val pageLayout: ViewGroup = itemView.findViewById(R.id.PageLayout)
        val settingLayout: ViewGroup = itemView.findViewById(R.id.settingLayout)
    }

    inner class CreatePageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val createPageButton: View = itemView.findViewById(R.id.CreatePage)

        fun bind(onCreatePageClickListener: OnCreatePageClickListener) {
            createPageButton.setOnClickListener {
                onCreatePageClickListener.onCreatePageClick()
            }
        }
    }
}
