package com.example.sharenote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("users/signup")
    fun signUpUser(@Body userData: UserData): Call<Void> // 회원가입을 위한 POST 요청
}
