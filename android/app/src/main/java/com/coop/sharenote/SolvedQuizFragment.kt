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

class SolvedQuizFragment : Fragment(), QuizAdapter.OnItemClickListener {

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
        quizAdapter = QuizAdapter(filterSolvedQuizzes(), this)
        recyclerView.adapter = quizAdapter

        // 풀린 퀴즈 데이터 로드
        loadSolvedQuizzes()

        return view
    }

    override fun onItemClick(quizId: String) {
        SharedPreferencesUtil.saveRecentQuizId(requireContext(), quizId)
        val intent = Intent(requireContext(), QuizDetailActivity::class.java)
        startActivity(intent)
    }

    private fun filterSolvedQuizzes(): MutableList<QuizList> {
        // correct 값이 1 또는 0인 퀴즈만 필터링하여 반환하는 함수
        return quizList.filter { it.correct == 1 || it.correct == 0 }.toMutableList()
    }

    private fun loadSolvedQuizzes() {
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
                        // 필터링된 데이터를 다시 설정
                        quizAdapter.setQuizzes(filterSolvedQuizzes())
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
}
