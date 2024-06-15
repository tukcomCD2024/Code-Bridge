package com.example.sharenote

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/api/" // 엔드포인트 주소 외에는 baseUrl에 포함되어야 함
    private const val AI_BASE_URL = "http://54.85.65.14:8000/" // AI 서버 주소
    //private const val BASE_URL = "https://sharenote.shop/api/" // 배포용
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS) // 연결 타임아웃 설정
        .readTimeout(60, TimeUnit.SECONDS)    // 읽기 타임아웃 설정
        .writeTimeout(60, TimeUnit.SECONDS)   // 쓰기 타임아웃 설정
        .protocols(listOf(Protocol.HTTP_1_1))
        .build()

    // 이제 넌 AI용이다
    private val retrofit2: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(AI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    }

    val apiService2: ApiService2 by lazy {
        retrofit2.create(ApiService2::class.java)
    }
}
