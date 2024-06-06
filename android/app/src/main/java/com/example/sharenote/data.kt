package com.example.sharenote

import okhttp3.MultipartBody


// 기존 노트 데이터
data class Note(
    val Id: String,
    val createUser: String,
    val title: String,
    val noteImageUrl: String,
)

// http 통신을 위한 노트 데이터
data class UserNote(
    val organizationId: String,
    val title: String,
    val userId: String,
    val noteImageUrl: String
)

data class NoteResponse(
    val noteId: String
)


data class Page(
    val id: String,
    val createUser: String,
    val createdAt: String
)

// http 통신을 위한 유저 데이터
data class UserData(
    val nickname: String,
    val userId: String,
    val email: String,
    val password: String
)

data class UserResponse(
    val name: String,
    val userId: String
)


data class WorkSpace(
    val name: String,
    val owner: String,
    val id : String
)



// http 통신을 위한 Organization 데이터
data class Organization(
    val name: String,
    val owner: String,
    val emoji: String
)
data class OrganizationResponse(
    val owner: String,
    val emoji: String,
    val organizationId: String
)

// http 통신을 위한 페이지 데이터
data class PageData(
    val organizationId: String,
    val noteId: String,
    val createUserId: String
)

data class PageResponse(
    val pageId: String
)


// http 통신을 통한 Org 조회
data class CheckOrganization(
    val id: String,
    val name: String,
    val description: String,
    val owner: String,
    val emoji: String,
    val members: List<String>,
    val notes: List<NoteCheck>
)

data class NoteCheck(
    val id: String,
    val title: String,
    val noteImageUrl: String,
    val pages: List<PageCheck>
)

data class PageCheck(
    val id: String,
    val createUser: String,
    val createdAt: String
)

data class InvitationData(
    val nickname: String,
    val organizationId: String,
    val email: String
)



/*
// http 통신을 통한 Note 조회
data class CheckNote(
    val id: String,
    val createUser: String,
    val title: String,
    val noteImageUrl: String,
    val pages: List<CheckPage>,
    val likesInfo: LikesInfo,
    val createdAt: String
)

// 페이지 데이터 모델 클래스
data class CheckPage(
    val id: String,
    val createUser: String,
    val createdAt: String
)

// 좋아요 정보 데이터 모델 클래스
data class LikesInfo(
    val userLikes: Map<String, Boolean>
)*/

data class ImageData(
    val multipartFile: MultipartBody.Part
)

data class ImageResponse(
    val image_url: String

)


data class AiImageRequest(
    val url: String
)
//data class AiImageUrls(
//    val imageUrls: List<String>
//)