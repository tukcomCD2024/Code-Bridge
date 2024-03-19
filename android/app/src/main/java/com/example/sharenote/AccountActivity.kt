package com.example.sharenote

import WorkSpaceListAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AccountActivity : AppCompatActivity() {

    private lateinit var recyclerViewWorkSpace: RecyclerView
    private lateinit var workSpaceListAdapter: WorkSpaceListAdapter

    private var workspaces: MutableList<WorkSpace> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_layout)

        // RecyclerView 및 어댑터 초기화
        recyclerViewWorkSpace = findViewById(R.id.recyclerViewWorkSpace)
        workSpaceListAdapter = WorkSpaceListAdapter(workspaces, object :
            WorkSpaceListAdapter.OnWorkSpaceClickListener {
            override fun onWorkSpaceClick(workSpace: WorkSpace) {
                // 워크스페이스를 클릭했을 때 처리할 내용을 여기에 작성합니다.
            }
        })
        recyclerViewWorkSpace.adapter = workSpaceListAdapter
        recyclerViewWorkSpace.layoutManager = LinearLayoutManager(this)

        // 파이어스토어에서 워크스페이스 데이터를 가져와서 어댑터에 설정
        loadWorkSpaces()

    }

    // 파이어스토어에서 워크스페이스 데이터를 가져와서 어댑터에 설정하는 함수
    private fun loadWorkSpaces() {
        val db = FirebaseFirestore.getInstance()
        db.collection("workSpaces")
            .get()
            .addOnSuccessListener { result ->
                workspaces.clear()
                for (document in result) {
                    val workSpaceName = document.getString("workSpaceName") ?: ""
                    val owner = document.getString("owner") ?: ""
                    val workSpace = WorkSpace(workSpaceName, owner)
                    workspaces.add(workSpace)
                }
                workSpaceListAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // 쿼리 실패 시 에러 처리
                // 예를 들어, 로그 출력 등
            }
    }
}
