import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharenote.R
import com.example.sharenote.WorkSpace

class WorkSpaceListAdapter(
    private var workSpaceList: MutableList<WorkSpace>,
    private val onWorkSpaceClickListener: OnWorkSpaceClickListener
) : RecyclerView.Adapter<WorkSpaceListAdapter.WorkSpaceViewHolder>() {

    // 클릭 리스너 인터페이스 정의
    interface OnWorkSpaceClickListener {
        fun onWorkSpaceClick(workSpace: WorkSpace)
    }

    // setWorkSpaces 메서드 추가
    fun setWorkSpaces(workSpaces: List<WorkSpace>) {
        this.workSpaceList.clear()
        this.workSpaceList.addAll(workSpaces)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkSpaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workspace, parent, false)
        return WorkSpaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkSpaceViewHolder, position: Int) {
        val workSpace = workSpaceList[position]
        holder.bind(workSpace)
    }

    override fun getItemCount(): Int {
        return workSpaceList.size
    }

    inner class WorkSpaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val workSpaceNameTextView: TextView = itemView.findViewById(R.id.workspaceNameTextView)

        fun bind(workSpace: WorkSpace) {
            workSpaceNameTextView.text = workSpace.name

            // 아이템 클릭 이벤트 처리
            itemView.setOnClickListener {
                onWorkSpaceClickListener.onWorkSpaceClick(workSpace)
            }
        }
    }
}
