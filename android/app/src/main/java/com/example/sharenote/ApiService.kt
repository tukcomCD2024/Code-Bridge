package com.example.sharenote

import com.google.firebase.firestore.auth.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.POST
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

    /*
    @GET("user/note/{organizationId}")
    suspend fun getNotesForOrganization(@Path("organizationId") organizationId: String): List<CheckNote>*/
}
