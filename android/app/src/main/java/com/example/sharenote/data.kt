package com.example.sharenote

data class Note(
    val noteId: String,
    val title: String,
    val organizationId: String,
    val userId: String
    // 필요한 다른 필드 추가
)

data class Page(
    val id: String, // 문서의 고유 ID
    val title: String,
    val text: String,
    val imageUri: String?
)

data class UserData(
    val name: String,
    val email: String,
    val password: String
)

data class WorkSpace(
    val name: String,
    val owner: String,
    val id : String
)