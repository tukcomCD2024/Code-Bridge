package com.coop.sharenote

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.coop.sharenote.R
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QuizDetailActivity : AppCompatActivity() {

    private lateinit var noteEditText: TextView
    private lateinit var noteType : TextView
    private lateinit var titleEditText: TextView
    private lateinit var container: LinearLayout
    private lateinit var backTextView: TextView

    // 체크박스 인덱스 추적을 위한 변수
    private var selectedChoiceIndex: Int = -1

    // 체크박스 인덱스 변수
    private var choiceIndex = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_detail)

        // XML에서 뷰 찾기
        noteEditText = findViewById(R.id.noteEditText)
        noteType = findViewById(R.id.noteType)
        titleEditText = findViewById(R.id.titleEditText)
        container = findViewById(R.id.container)

        backTextView = findViewById(R.id.backTextView)

        backTextView.setOnClickListener {
            onBackPressed()
        }

        val OrgId = SharedPreferencesUtil.getRecentWorkspaceId(this) ?: ""
        val noteId = SharedPreferencesUtil.getRecentNoteId(this) ?: ""
        val userId = SharedPreferencesUtil.getUserId(this) ?: ""
        val quizId = SharedPreferencesUtil.getRecentQuizId(this) ?: ""

        // 네트워크 요청 보내기
        val request = QuizDetailRequest(
            organizationId = OrgId,
            noteId = noteId,
            userId = userId,
            quizId = quizId
        )
        fetchQuizDetail(request)

        val submitButton: Button = findViewById(R.id.submitButton)
        submitButton.setOnClickListener {
            submitQuizAnswer(OrgId, noteId, userId, quizId)
        }
    }

    private fun submitQuizAnswer(OrgId: String, noteId: String, userId: String, quizId: String) {
        // 선택된 답변이 없는 경우
        if (selectedChoiceIndex == -1) {
            Toast.makeText(this, "답변을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // SolveQuiz 데이터 생성
        val solveQuizData = SolveQuiz(
            organizationId = OrgId,
            noteId = noteId,
            userId = userId,
            quizId = quizId,
            answer = selectedChoiceIndex
        )

        val accessToken = SharedPreferencesUtil.getAccessToken(this@QuizDetailActivity) ?: ""

        // HTTP POST 요청
        val solveQuizCall = RetrofitClient.apiService.solveQuiz(solveQuizData, accessToken)

        solveQuizCall.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // 서버에서 올바른 응답을 받은 경우
                    val responseBody = response.body()?.string() ?: "퀴즈 제출 성공!"
                    Toast.makeText(this@QuizDetailActivity, responseBody, Toast.LENGTH_SHORT).show()
                    // 퀴즈 제출 후 QuizActivity로 이동
                    val intent = Intent(this@QuizDetailActivity, QuizActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // 서버에서 오류 응답을 받은 경우
                    val errorMessage = response.errorBody()?.string() ?: "퀴즈 제출 실패"
                    Toast.makeText(this@QuizDetailActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    Log.e("QuizDetailActivity", "Error: $errorMessage")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 네트워크 요청 실패 시 처리
                Toast.makeText(this@QuizDetailActivity, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("QuizDetailActivity", "Network Error: ${t.message}")
            }
        })
    }

    private fun fetchQuizDetail(request: QuizDetailRequest) {
        val accessToken = SharedPreferencesUtil.getAccessToken(this@QuizDetailActivity) ?: ""

        val call = RetrofitClient.apiService.quizDetail(request, accessToken)

        call.enqueue(object : Callback<QuizDetailResponse> {
            override fun onResponse(call: Call<QuizDetailResponse>, response: Response<QuizDetailResponse>) {
                if (response.isSuccessful) {
                    val quizDetailResponse = response.body()
                    quizDetailResponse?.let {
                        // 받아온 데이터를 화면에 표시
                        noteEditText.setText(it.noteName + " 퀴즈 맞히기")
                        noteType.setText("Type : " + it.quizType)
                        titleEditText.setText("     " + it.quizTitle)

                        // problems 리스트의 항목 수만큼 아이템을 생성하여 container에 추가
                        for (problem in it.problems) {
                            addChoiceItem(problem)
                        }
                    }
                } else {
                    // 서버에서 오류 응답을 받은 경우 처리

                }
            }

            override fun onFailure(call: Call<QuizDetailResponse>, t: Throwable) {
                // 네트워크 요청 실패 시 처리
            }
        })
    }

    private fun addChoiceItem(problem: String) {
        // add_choice.xml 레이아웃을 인플레이트하여 뷰 생성
        val view = layoutInflater.inflate(R.layout.add_choice_detail, container, false)

        // 뷰에서 answerEditText 찾기
        val answerEditText = view.findViewById<TextView>(R.id.answerEditText)


        val answerText = view.findViewById<TextView>(R.id.answerText)

        // 받아온 problem 값을 answerEditText에 설정
        answerEditText.setText(problem)


        answerText.setOnClickListener {
            val clickedTextView = it as TextView
            if (selectedChoiceIndex != clickedTextView.tag as Int) {
                // 선택된 TextView의 인덱스 추적
                selectedChoiceIndex = clickedTextView.tag as Int
                // 다른 모든 TextView의 선택 해제
                uncheckOtherTextViews(clickedTextView)

                clickedTextView.setBackgroundResource(R.drawable.rectangle_bright_blue)
            } else {
                // 클릭 해제된 경우 인덱스 초기화
                selectedChoiceIndex = -1
            }
        }

        answerText.tag = choiceIndex

        // 생성한 뷰를 container에 추가
        container.addView(view)

        // 인덱스 증가
        choiceIndex++
    }

    private fun uncheckOtherTextViews(selectedTextView: TextView) {
        // container의 모든 자식 뷰를 순회하며 다른 TextView의 선택 해제
        for (i in 0 until container.childCount) {
            val childView = container.getChildAt(i)
            val textView = childView.findViewById<TextView>(R.id.answerText)
            if (textView != selectedTextView) {
                textView.setBackgroundResource(R.drawable.rectangle)
            }
        }
    }

    private fun startQuizActivity() {
        val intent = Intent(this, QuizActivity::class.java)
        startActivity(intent)
        // 현재 액티비티를 종료하여 이전 액티비티로 돌아가지 않도록 설정
        finish()
    }

}
