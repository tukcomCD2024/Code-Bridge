package com.example.sharenote

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    // 회원가입을 처리하는 POST 요청을 정의합니다.
    @POST("signUp")
    fun signUpUser(@Body userData: UserData): Call<Void>
}
