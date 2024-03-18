import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharenote.R
import com.example.sharenote.WorkSpace

class WorkSpaceListAdapter(
    private val workSpaceList: MutableList<WorkSpace>,
    private val onWorkSpaceClickListener: OnWorkSpaceClickListener
) : RecyclerView.Adapter<WorkSpaceListAdapter.WorkSpaceViewHolder>() {

    // 클릭 리스너 인터페이스 정의
    interface OnWorkSpaceClickListener {
        fun onWorkSpaceClick(workSpaceName: WorkSpace)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkSpaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_workspace, parent, false)
        return WorkSpaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkSpaceViewHolder, position: Int) {
        val workSpaceName = workSpaceList[position]
        holder.workSpaceNameTextView.text = workSpaceName.name

        // 아이템 클릭 이벤트 처리
        holder.itemView.setOnClickListener {
            onWorkSpaceClickListener.onWorkSpaceClick(workSpaceName)
        }
    }

    override fun getItemCount(): Int {
        return workSpaceList.size
    }



    inner class WorkSpaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workSpaceNameTextView: TextView = itemView.findViewById(R.id.workspaceNameTextView)

    }
}
