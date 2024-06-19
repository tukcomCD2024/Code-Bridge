package com.example.sharenote

import QuizAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharenote.RetrofitClient.apiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AllQuizFragment : Fragment(), QuizAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var quizAdapter: QuizAdapter
    private var quizList: MutableList<QuizList> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_quiz_list, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        quizAdapter = QuizAdapter(quizList, this)
        recyclerView.adapter = quizAdapter

        // 퀴즈 데이터를 불러오는 메서드 호출
        loadQuizzes()

        return view
    }

    override fun onItemClick(quizId: String) {
        SharedPreferencesUtil.saveRecentQuizId(requireContext(), quizId)
        val intent = Intent(requireContext(), QuizDetailActivity::class.java)
        startActivity(intent)
    }

    private fun loadQuizzes() {
        val recentWorkspaceId = SharedPreferencesUtil.getRecentWorkspaceId(requireContext()) ?: ""
        val noteId = SharedPreferencesUtil.getRecentNoteId(requireContext()) ?: ""
        val userId = SharedPreferencesUtil.getUserId(requireContext()) ?: ""
        val accessToken = SharedPreferencesUtil.getAccessToken(requireContext()) ?: ""

        apiService.getQuizzes(recentWorkspaceId, noteId, userId, accessToken).enqueue(object : Callback<List<QuizList>> {
            override fun onResponse(call: Call<List<QuizList>>, response: Response<List<QuizList>>) {
                if (response.isSuccessful) {
                    response.body()?.let { quizzes ->
                        quizList.clear()
                        quizList.addAll(quizzes)
                        quizAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load quizzes", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<QuizList>>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to load quizzes: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
