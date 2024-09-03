import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coop.sharenote.R
import com.coop.sharenote.WorkSpace
import com.google.firebase.firestore.FirebaseFirestore

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
            val buttonDeleteWorkSpace: Button = itemView.findViewById(R.id.buttonDeleteWorkSpace)
            buttonDeleteWorkSpace.setOnClickListener {
                val position = adapterPosition // 이 위치에서 adapterPosition 사용
                if (position != RecyclerView.NO_POSITION) {
                    val deletedWorkSpace = workSpaceList[position]
                    // 삭제할 작업 수행
                    deleteWorkSpace(deletedWorkSpace)
                }
            }
        }
    }
    private fun deleteWorkSpace(workSpace: WorkSpace) {
        val db = FirebaseFirestore.getInstance()
        db.collection("workSpaces")
            .document(workSpace.id)
            .delete()
            .addOnSuccessListener {
                // 문서 삭제 성공
                deleteNotesInWorkspace(workSpace.id) // 해당 워크스페이스에 속한 노트들 삭제
                workSpaceList.remove(workSpace) // 리스트에서 해당 워크스페이스 제거
                notifyDataSetChanged() // 어댑터에 변경 내용 반영
            }
            .addOnFailureListener { e ->
                // 문서 삭제 실패
                // 실패 처리 로직 작성
            }
    }

    private fun deleteNotesInWorkspace(workspaceId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("notes")
            .whereEqualTo("workSpaceId", workspaceId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    db.collection("notes")
                        .document(document.id)
                        .delete()
                        .addOnSuccessListener {
                            // 노트 삭제 성공
                        }
                        .addOnFailureListener { e ->
                            // 노트 삭제 실패
                            // 실패 처리 로직 작성
                        }
                }
            }
            .addOnFailureListener { exception ->
                // 쿼리 실패 시 에러 처리
                // 예를 들어, 로그 출력 등
            }
    }
}
