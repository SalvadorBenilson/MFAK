package com.example.mfak.api

import kotlinx.serialization.Serializable

@Serializable
data class AuthCode(
    val id: Int,
    val serviceName: String,
    val code: String,
    val email: String
)
