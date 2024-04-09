package com.example.sharenote

import com.google.firebase.firestore.auth.User
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.POST

interface ApiService {

    // 회원가입을 처리하는 POST 요청을 정의
    @POST("signUp")
    fun signUpUser(@Body userData: UserData): Call<Void>

    @POST("login")
    suspend fun login(@Body userData: UserData): Response<UserResponse>

    @POST("organization")
    suspend fun sendWorkSpaceData(@Body organization: Organization): Response<OrganizationResponse>

    @POST("note")
    suspend fun sendNoteData(@Body note: Note): Response<ResponseBody>

}
