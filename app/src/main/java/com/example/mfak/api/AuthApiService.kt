package com.example.mfak.api

import retrofit2.http.GET

interface AuthApiService {
    @GET("codes") // Assuming an endpoint "codes"
    suspend fun getAuthCodes(): List<AuthCode>
}
