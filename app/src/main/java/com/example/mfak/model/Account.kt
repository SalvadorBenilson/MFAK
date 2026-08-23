package com.example.mfak.model

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val id: String,
    val serviceName: String,
    val email: String,
    val secretKey: String,
    val isGeneric: Boolean = true
)
