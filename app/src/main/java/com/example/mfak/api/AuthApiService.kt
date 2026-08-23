package com.example.mfak.api

import com.example.mfak.model.Account
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

@Serializable
data class UserRegistration(val email: String, val passwordHash: String)

@Serializable
data class BackupRequest(val accounts: List<Account>)

interface AuthApiService {
    @POST("register")
    suspend fun register(@Body registration: UserRegistration)

    @POST("backup")
    suspend fun backupAccounts(@Body backupRequest: BackupRequest)

    @GET("restore")
    suspend fun restoreAccounts(): List<Account>

    @GET("codes")
    suspend fun getAuthCodes(): List<AuthCode>
}
