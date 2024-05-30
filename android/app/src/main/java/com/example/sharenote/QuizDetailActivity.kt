package com.example.sharenote

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.CheckBox
import android.widget.Toast
import com.example.sharenote.QuizDetailRequest
import com.example.sharenote.QuizDetailResponse
import com.example.sharenote.R
import com.example.sharenote.RetrofitClient
import okhttp3.ResponseBody
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QuizDetailActivity : AppCompatActivity() {

    private lateinit var noteEditText: TextView
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

        // HTTP POST 요청
        val solveQuizCall = RetrofitClient.apiService.solveQuiz(solveQuizData)

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
        val call = RetrofitClient.apiService.quizDetail(request)

        call.enqueue(object : Callback<QuizDetailResponse> {
            override fun onResponse(call: Call<QuizDetailResponse>, response: Response<QuizDetailResponse>) {
                if (response.isSuccessful) {
                    val quizDetailResponse = response.body()
                    quizDetailResponse?.let {
                        // 받아온 데이터를 화면에 표시
                        noteEditText.setText(it.noteName)
                        titleEditText.setText(it.quizTitle)

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

        // 체크박스 찾기
        val choiceCheckBox = view.findViewById<CheckBox>(R.id.choiceCheckBox)

        // 받아온 problem 값을 answerEditText에 설정
        answerEditText.setText(problem)

        // 체크박스 클릭 리스너 설정
        choiceCheckBox.setOnClickListener {
            val clickedCheckBox = it as CheckBox
            if (clickedCheckBox.isChecked) {
                // 선택된 체크박스의 인덱스 추적
                selectedChoiceIndex = clickedCheckBox.tag as Int
                // 다른 모든 체크박스 선택 해제
                uncheckOtherCheckBoxes(clickedCheckBox)
            } else {
                // 체크 해제된 경우 인덱스 초기화
                selectedChoiceIndex = -1
            }
        }

        // 체크박스에 인덱스 부여
        choiceCheckBox.tag = choiceIndex

        // 생성한 뷰를 container에 추가
        container.addView(view)

        // 인덱스 증가
        choiceIndex++
    }

    // 다른 모든 체크박스 선택 해제
    private fun uncheckOtherCheckBoxes(clickedCheckBox: CheckBox) {
        for (i in 0 until container.childCount) {
            val childView = container.getChildAt(i)
            val checkBox = childView.findViewById<CheckBox>(R.id.choiceCheckBox)
            if (checkBox != clickedCheckBox) {
                checkBox.isChecked = false
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
