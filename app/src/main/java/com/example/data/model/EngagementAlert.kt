package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AlertType(val label: String, val iconEmoji: String) {
    VIRAL_VELOCITY("Accélération virale", "🚀"),
    COMMENT_EXPLOSION("Flot de commentaires", "💬"),
    SHARE_SURGE("Pic de partages", "🔁"),
    RETENTION_SPIKE("Rétention record", "📈")
}

@Entity(tableName = "engagement_alerts")
data class EngagementAlert(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val campaignId: Long = 0,
    val campaignTitle: String,
    val platform: String,
    val alertType: String = AlertType.VIRAL_VELOCITY.name,
    val message: String,
    val metricHighlight: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
