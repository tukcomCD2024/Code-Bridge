import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.sharenote.R
import com.example.sharenote.WorkSpace

class MemberListAdapter(private val members: MutableList<String>) : RecyclerView.Adapter<MemberListAdapter.MemberViewHolder>() {

    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memberIdTextView: TextView = itemView.findViewById(R.id.memberIdTextView)
    }


    fun setMembers(member: List<String>) {
        this.members.clear()
        this.members.addAll(member)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_member, parent, false)
        return MemberViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val currentMemberId = members[position]
        holder.memberIdTextView.text = currentMemberId
    }

    override fun getItemCount(): Int {
        return members.size
    }
}