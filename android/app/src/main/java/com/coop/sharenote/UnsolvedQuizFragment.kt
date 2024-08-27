package com.coop.sharenote

import QuizAdapter
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coop.sharenote.RetrofitClient.apiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UnsolvedQuizFragment : Fragment(), QuizAdapter.OnItemClickListener {

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
        quizAdapter = QuizAdapter(filterUnsolvedQuizzes(), this)
        recyclerView.adapter = quizAdapter

        return view
    }

    override fun onItemClick(quizId: String) {
        SharedPreferencesUtil.saveRecentQuizId(requireContext(), quizId)
        val intent = Intent(requireContext(), QuizDetailActivity::class.java)
        startActivity(intent)
    }

    private fun filterUnsolvedQuizzes(): MutableList<QuizList> {
        // correct 값이 -1인 퀴즈만 필터링하여 반환하는 함수
        return quizList.filter { it.correct == -1 }.toMutableList()
    }

    private fun loadUnsolvedQuizzes() {
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
                        quizAdapter.setQuizzes(filterUnsolvedQuizzes())
                    }
                } else {
                    // 실패 처리
                }
            }

            override fun onFailure(call: Call<List<QuizList>>, t: Throwable) {
                // 실패 처리
            }
        })
    }

    override fun onResume() {
        super.onResume()
        loadUnsolvedQuizzes()
    }
}
