package com.example.mfak.repository

import android.content.Context
import com.example.mfak.model.Account
import com.example.mfak.security.SecurityUtils
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AccountRepository(context: Context) {
    private val prefs = SecurityUtils.getEncryptedPrefs(context)
    private val json = Json { ignoreUnknownKeys = true }

    fun saveAccounts(accounts: List<Account>) {
        val serialized = json.encodeToString(accounts)
        prefs.edit().putString("accounts", serialized).apply()
    }

    fun getAccounts(): List<Account> {
        val serialized = prefs.getString("accounts", null) ?: return emptyList()
        return try {
            json.decodeFromString(serialized)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addAccount(account: Account) {
        val current = getAccounts().toMutableList()
        current.add(account)
        saveAccounts(current)
    }
}
