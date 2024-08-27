package com.coop.sharenote

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteRecentListAdapter(private val onNoteClick: (String) -> Unit) :
    RecyclerView.Adapter<NoteRecentListAdapter.RecentNoteViewHolder>() {

    private var recentNotes: MutableList<Note> = mutableListOf()

    class RecentNoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.noteTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentNoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_note, parent, false)
        return RecentNoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentNoteViewHolder, position: Int) {
        val note = recentNotes[position]
        holder.titleTextView.text = note.title

        holder.itemView.setOnClickListener {
            onNoteClick(note.Id)
        }
    }

    override fun getItemCount(): Int {
        return recentNotes.size
    }

    fun setNotes(notes: List<Note>) {
        recentNotes.clear()
        recentNotes.addAll(notes)
        notifyDataSetChanged()
    }

}
