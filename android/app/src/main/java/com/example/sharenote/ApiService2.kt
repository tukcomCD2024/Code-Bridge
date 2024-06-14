package com.example.sharenote


import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Multipart
import retrofit2.http.Part

interface ApiService2 {
    @Multipart
    @POST("image")
    suspend fun uploadImage(@Part multipartFile: MultipartBody.Part): Response<ImageResponse>

    @POST("ai/url")
    suspend fun aiPickImages(@Body url: AiImageRequest): Response<List<String>>
}