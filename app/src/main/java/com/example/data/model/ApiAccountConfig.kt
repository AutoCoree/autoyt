package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_accounts")
data class ApiAccountConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val platform: String, // YOUTUBE or TIKTOK
    val accountName: String,
    val channelHandle: String,
    val isConnected: Boolean = true,
    val apiKey: String = "",
    val accessToken: String = "",
    val quotaUsed: Int = 120,
    val quotaLimit: Int = 10000,
    val autoPublishEnabled: Boolean = true,
    val uploadPrivacyDefault: String = "PUBLIC", // PUBLIC, UNLISTED, PRIVATE
    val lastSyncMillis: Long = System.currentTimeMillis()
)
