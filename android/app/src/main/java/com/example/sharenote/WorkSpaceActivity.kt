package com.example.sharenote

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharenote.SharedPreferencesUtil.saveRecentWorkspaceId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkSpaceActivity : AppCompatActivity() {

    private lateinit var workSpaceNameEditText: EditText
    private lateinit var continueButton: Button
    private lateinit var backTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_work_space)

        workSpaceNameEditText = findViewById(R.id.workSpaceName)
        continueButton = findViewById(R.id.continueButton)
        backTextView = findViewById(R.id.backTextView)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // SharedPreferences 초기화
        sharedPreferences = getSharedPreferences("workSpacePrefs", Context.MODE_PRIVATE)


        continueButton.setOnClickListener {
            val workSpaceName = workSpaceNameEditText.text.toString().trim()
            val userEmail = SharedPreferencesUtil.getUserEmail(this).toString()

            if (workSpaceName.isNotEmpty()) {
                /*val currentUserEmail = auth.currentUser?.email
                currentUserEmail?.let { email ->
                    saveWorkSpaceToFirestore(workSpaceName, email)
                }*/
                val organization = Organization(workSpaceName, userEmail,"")
                saveWorkSpaceToMongoDB(organization)
            } else {
                // 워크스페이스 이름이 비어있는 경우
                Toast.makeText(this, "워크스페이스 이름을 정해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        backTextView.setOnClickListener {
            onBackPressed()
        }

    }


    private fun saveWorkSpaceToFirestore(workSpaceName: String, email: String) {
        val workSpaceData = hashMapOf(
            "workSpaceName" to workSpaceName,
            "owner" to email
        )

        val collectionPath = "workSpaces" // 워크스페이스 정보를 저장할 컬렉션 이름
        firestore.collection(collectionPath)
            .add(workSpaceData)
            .addOnSuccessListener { documentReference ->
                // 파이어스토어에 데이터가 성공적으로 추가된 경우
                val workSpaceId = documentReference.id // 새로 생성된 문서의 고유 ID 가져오기

                // 고유 ID를 SharedPreferences에 저장
                saveRecentWorkspaceId(workSpaceId)


                // 고유 ID를 해당 문서의 필드로 추가하여 다시 업데이트
                documentReference.update("workSpaceId", workSpaceId)
                    .addOnSuccessListener {
                        // InviteActivity로 이동
                        val intent = Intent(this, InviteActivity::class.java)
                        intent.putExtra("workSpaceId", workSpaceId) // 워크스페이스 ID를 인텐트에 추가
                        startActivity(intent)
                        finish() // 현재 Activity 종료
                    }
                    .addOnFailureListener { e ->
                        // 업데이트 실패 시 처리
                        Toast.makeText(this, "워크스페이스 ID를 저장하는 도중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                // 파이어스토어에 데이터 추가 중 오류 발생한 경우
                Toast.makeText(this, "워크스페이스 정보를 저장하는 도중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun saveWorkSpaceToMongoDB(organization: Organization) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // MongoDB에 워크스페이스 데이터를 전송
                val response = RetrofitClient.apiService.sendWorkSpaceData(organization)
                if (response.isSuccessful) {
                    // MongoDB에 데이터 저장 성공 시
                    val workSpaceResponse = response.body() // 응답 데이터 파싱
                    if (workSpaceResponse != null) {
                        // 반환된 데이터로부터 워크스페이스 ID 추출
                        val workSpaceName = organization.name
                        val workSpaceId = workSpaceResponse.organizationId

                        // 워크스페이스 이름을 SharedPreferences에 저장
                        saveRecentWorkspaceName(workSpaceName)

                        // 추출한 ID를 SharedPreferences에 저장
                        saveRecentWorkspaceId(workSpaceId)

                        // InviteActivity로 이동
                        val intent = Intent(this@WorkSpaceActivity, InviteActivity::class.java)
                        startActivity(intent)
                        finish() // 현재 Activity 종료
                    } else {
                        // 반환된 데이터가 없을 경우 에러 처리
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@WorkSpaceActivity,
                                "워크스페이스 정보를 받아오지 못했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    // MongoDB에 데이터 저장 실패 시
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@WorkSpaceActivity,
                            "워크스페이스 정보를 저장하는 데 실패했습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                // 네트워크 오류 등 예외 처리
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@WorkSpaceActivity,
                        "네트워크 오류가 발생했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // SharedPreferences에 워크스페이스 ID 저장
    private fun saveRecentWorkspaceId(workspaceId: String) {
        SharedPreferencesUtil.saveRecentWorkspaceId(this, workspaceId)
    }

    private fun saveRecentWorkspaceName(workspaceName: String) {
        SharedPreferencesUtil.saveRecentWorkspaceName(this, workspaceName)
    }

}
