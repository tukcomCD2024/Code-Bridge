package com.example.sharenote

data class Note(
    val id: String, // 문서의 고유 ID
    val text: String,
    val imageUri: String?
)

data class UserData(
    val name: String,
    val email: String,
    val password: String
)