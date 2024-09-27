package com.yourapp.network

import com.example.runner_dle.Geocoding.GeocodeResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// 네이버 Geocoding API를 위한 Retrofit 인터페이스
interface GeocodingService {
    @GET("/map-geocode/v2/geocode")
    suspend fun getGeocode(
        @Query("query") query: String,
        @Header("X-NCP-APIGW-API-KEY-ID") apiKeyId: String,
        @Header("X-NCP-APIGW-API-KEY") apiKey: String,
        @Header("Accept") accept: String = "application/json"
    ): GeocodeResponse
}

// Retrofit 인스턴스 생성 및 GeocodingService 구현
object GeocodingApi {
    private const val BASE_URL = "https://naveropenapi.apigw.ntruss.com/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: GeocodingService = retrofit.create(GeocodingService::class.java)
}
