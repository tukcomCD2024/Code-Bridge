package com.example.sharenote

import com.google.firebase.firestore.auth.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    // 회원가입을 처리하는 POST 요청을 정의
    @POST("user/signUp")
    fun signUpUser(@Body userData: UserData): Call<Void>

    @POST("user/login")
    suspend fun login(
        @Body userData: UserData,
        @Header("fcm") fcmToken: String
    ): Response<UserResponse>

    @POST("user/organization")
    suspend fun sendWorkSpaceData(
        @Body organization: Organization,
        @Header("access") access: String
    ): Response<OrganizationResponse>

    @POST("user/note")
    suspend fun sendNoteData(
        @Body note: UserNote,
        @Header("access") accessToken: String
    ): Response<NoteResponse>

    @POST("page")
    suspend fun sendPageData(
        @Body page: PageData,
        @Header("access") accessToken: String
    ): Response<PageResponse>

    @GET("user/organization/{organizationId}")
    suspend fun getOrganization(
        @Path("organizationId") organizationId: String,
        @Header("access") accessToken: String
    ): List<CheckOrganization>

    @POST("user/organization/invitation")
    fun sendInvitationEmail(
        @Body data: InvitationData,
        @Header("access") accessToken: String
    ): Call<Void>

    @Multipart
    @POST("image")
    suspend fun uploadImage(
        @Part multipartFile: MultipartBody.Part,
        @Header("access") accessToken: String
    ): Response<ImageResponse>

    @POST("quiz")
    fun createQuiz(
        @Body quizRequest: QuizRequest,
        @Header("access") accessToken: String
    ): Call<Void>

    @GET("quiz/{organization}/{noteId}/{userId}")
    fun getQuizzes(
        @Path("organization") organizationId: String,
        @Path("noteId") noteId: String,
        @Path("userId") userId: String
    ): Call<List<QuizList>>

    @POST("quiz/detail")
    fun quizDetail(
        @Body request: QuizDetailRequest,
        @Header("access") accessToken: String
    ): Call<QuizDetailResponse>

    @POST("quiz-solutions")
    fun solveQuiz(
        @Body request: SolveQuiz,
        @Header("access") accessTokten: String
    ): Call<ResponseBody>
}
