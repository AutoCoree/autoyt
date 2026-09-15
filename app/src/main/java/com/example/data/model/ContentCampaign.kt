package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PlatformType(val displayName: String) {
    YOUTUBE("YouTube"),
    TIKTOK("TikTok"),
    BOTH("Multi-Plateformes")
}

enum class CampaignStatus(val label: String) {
    DRAFT("Brouillon"),
    SCHEDULED("Programmé"),
    PUBLISHING("En cours d'envoi"),
    PUBLISHED("Publié"),
    VIRAL_ALERT("🔥 Pic d'engagement")
}

enum class VideoFormat(val label: String, val durationText: String) {
    SHORT("YouTube Short / Reel", "15-60s"),
    TIKTOK_FAST("TikTok Viral", "30-90s"),
    LONG_FORM("Format Long", "5-15 min")
}

@Entity(tableName = "campaigns")
data class ContentCampaign(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val platform: String = PlatformType.BOTH.name,
    val status: String = CampaignStatus.DRAFT.name,
    val videoFormat: String = VideoFormat.SHORT.name,
    val scheduledTimeMillis: Long = System.currentTimeMillis() + 3600_000L * 4,
    val publishedTimeMillis: Long = 0L,
    val videoDurationSeconds: Int = 45,
    val tags: String = "#viral,#ia,#creators",
    val hookText: String = "",
    val scriptContent: String = "",
    val editingCues: String = "",
    val callToAction: String = "",
    val optimalReachScore: Int = 92,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val youtubeVideoUrl: String? = null,
    val tiktokVideoUrl: String? = null
)
