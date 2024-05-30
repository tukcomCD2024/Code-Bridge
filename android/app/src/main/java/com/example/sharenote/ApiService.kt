package com.example.sharenote

import com.google.firebase.firestore.auth.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {

    // 회원가입을 처리하는 POST 요청을 정의
    @POST("user/signUp")
    fun signUpUser(@Body userData: UserData): Call<Void>

    @POST("user/login")
    suspend fun login(@Body userData: UserData): Response<UserResponse>

    @POST("user/organization")
    suspend fun sendWorkSpaceData(@Body organization: Organization): Response<OrganizationResponse>

    @POST("user/note")
    suspend fun sendNoteData(@Body note: UserNote): Response<NoteResponse>

    @POST("page")
    suspend fun sendPageData(@Body page: PageData): Response<PageResponse>

    @GET("user/organization/{organizationId}")
    suspend fun getOrganization(@Path("organizationId") organizationId: String): List<CheckOrganization>

    @POST("user/organization/invitation")
    fun sendInvitationEmail(@Body data: InvitationData): Call<Void>

    @Multipart
    @POST("image")
    suspend fun uploadImage(@Part multipartFile: MultipartBody.Part): Response<ImageResponse>

    @POST("quiz")
    fun createQuiz(@Body quizRequest: QuizRequest): Call<Void>

    @GET("quiz/{organization}/{noteId}/{userId}")
    fun getQuizzes(
        @Path("organization") organizationId: String,
        @Path("noteId") noteId: String,
        @Path("userId") userId: String
    ): Call<List<QuizList>>

    @POST("quiz/detail")
    fun quizDetail(@Body request: QuizDetailRequest): Call<QuizDetailResponse>

    @POST("quiz-solutions")
    fun solveQuiz(@Body request: SolveQuiz): Call<ResponseBody>
}
