package com.sangwon.example.bookapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.sangwon.example.bookapp.databinding.ActivityMypageBinding

class MyPageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMypageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var profileImageUrl: String

    companion object {
        private const val REQUEST_USER_INFO = 1001
        private const val TAG = "MypageActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMypageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        title = "                마이페이지"

        val currentUser = auth.currentUser
        currentUser?.let {
            // Firebase에서 사용자 정보를 읽어옴
            loadUserInfo()
        }

        binding.editProfileButton.setOnClickListener {
            // 개인정보 수정 화면으로 이동할 때 사용자 정보 전달
            val currentUser = auth.currentUser
            currentUser?.let {
                val intent = Intent(this, UserInfoActivity::class.java)
                intent.putExtra("userName", binding.usernameTextView.text.toString())
                intent.putExtra("phoneNumber", binding.PhoneNumberTextView.text.toString())
                intent.putExtra("profileImage", profileImageUrl)
                startActivityForResult(intent, REQUEST_USER_INFO)
            }
        }

        binding.logoutButton.setOnClickListener {
            logout()
        }

        binding.signOutButton.setOnClickListener {
            signOut()
        }

        binding.purchaseHistoryButton.setOnClickListener {
            val intent = Intent(this, BookListActivity::class.java)
            intent.putExtra("type", "purchase")
            startActivity(intent)
        }

        binding.salesHistoryButton.setOnClickListener {
            val intent = Intent(this, BookListActivity::class.java)
            intent.putExtra("type", "sales")
            startActivity(intent)
        }
        binding.chatHistoryButton.setOnClickListener {
            startActivity(Intent(this, ChatListActivity::class.java))
        }
    }

    private fun loadUserInfo() {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(Firebase.auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.toObject(User::class.java)
                    user?.let {
                        // 사용자 정보를 UI에 설정
                        binding.usernameTextView.text = it.name
                        binding.userEmailTextView.text = it.userId
                        binding.PhoneNumberTextView.text =
                            it.phoneNumber.replace("({3}-{3,4}-{4})", "$1-$2-$3")
                        profileImageUrl = "profile_images/${auth.currentUser!!.uid}"

                        val storageReference =
                            FirebaseStorage.getInstance().reference.child(profileImageUrl)

                        storageReference.downloadUrl.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                profileImageUrl = task.result.toString()
                                Glide.with(this)
                                    .load(task.result)
                                    .into(binding.profileImage)
                            } else {
                                binding.profileImage.setBackgroundResource(R.drawable.profile)
                                profileImageUrl = ""
                            }
                        }
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    private fun logout() {
        auth.signOut() // Firebase 로그아웃

        // LoginActivity로 이동
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun signOut() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("계정을 삭제합니다")
            .setMessage("정말로 계정을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(auth.currentUser!!.uid)
                    .get()
                    .addOnSuccessListener {
                        db.collection("users").document(auth.currentUser!!.uid)
                            .delete()
                            .addOnSuccessListener {
                                val storage = FirebaseStorage.getInstance()
                                storage.reference.child("profile_images").child(auth.currentUser!!.uid)
                                    .delete()
                                    .addOnSuccessListener {
                                        db.collection("post")
                                            .whereEqualTo("uid", auth.currentUser!!.uid)
                                            .get()
                                            .addOnSuccessListener {
                                                for (doc in it.documents) {
                                                    db.collection("Post").document(doc.id).delete()
                                                }
                                            }
                                    }
                            }
                        for (chat in it.get("ChatRoom") as ArrayList<String>){
                            db.collection("users").whereArrayContains("ChatRoom", chat)
                                .get()
                                .addOnSuccessListener {partners ->
                                    for (i in partners){
                                        db.collection("users").document(i.id)
                                            .update("ChatRoom", FieldValue.arrayRemove(chat))
                                    }
                                }
                            db.collection("Chats").document(chat).delete()
                        }
                    }

            }
            .setNegativeButton("취소") { _, _ -> }
        dialog.show()

//        auth.currentUser?.delete()
//        Toast.makeText(this, "계정이 삭제됐습니다.", Toast.LENGTH_SHORT).show()
//        val intent = Intent(this, LoginActivity::class.java)
//        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
//        startActivity(intent)
//        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_USER_INFO && resultCode == UserInfoActivity.RESULT_CODE_SUCCESS) {
            // UserInfoActivity로부터 전달받은 정보를 사용하여 화면 업데이트
            val newName = data?.getStringExtra("userName")
            val newPhoneNumber = data?.getStringExtra("phoneNumber")
            if (!newName.isNullOrEmpty()) {
                binding.usernameTextView.text = newName
            }
            if (!newPhoneNumber.isNullOrEmpty()) {
                binding.PhoneNumberTextView.text = newPhoneNumber
            }
        }
    }
}