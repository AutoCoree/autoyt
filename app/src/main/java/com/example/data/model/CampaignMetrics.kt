package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campaign_metrics")
data class CampaignMetrics(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val campaignId: Long,
    val youtubeViews: Long = 0,
    val tiktokViews: Long = 0,
    val youtubeLikes: Long = 0,
    val tiktokLikes: Long = 0,
    val youtubeComments: Long = 0,
    val tiktokComments: Long = 0,
    val youtubeShares: Long = 0,
    val tiktokShares: Long = 0,
    val youtubeWatchTimeMinutes: Double = 0.0,
    val tiktokWatchTimeMinutes: Double = 0.0,
    val youtubeRetentionRate: Double = 0.0, // e.g. 74.2%
    val tiktokRetentionRate: Double = 0.0,  // e.g. 82.5%
    val youtubeCtr: Double = 0.0,           // Click-through rate e.g. 7.1%
    val tiktokCompletionRate: Double = 0.0, // e.g. 61.4%
    val engagementSpikeDetected: Boolean = false,
    val viralVelocityPerHour: Long = 0,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
) {
    val totalViews: Long
        get() = youtubeViews + tiktokViews

    val totalLikes: Long
        get() = youtubeLikes + tiktokLikes

    val totalComments: Long
        get() = youtubeComments + tiktokComments

    val totalShares: Long
        get() = youtubeShares + tiktokShares

    val totalWatchTimeHours: Double
        get() = (youtubeWatchTimeMinutes + tiktokWatchTimeMinutes) / 60.0

    val youtubeEngagementRate: Double
        get() = if (youtubeViews > 0) {
            ((youtubeLikes + youtubeComments + youtubeShares).toDouble() / youtubeViews.toDouble()) * 100.0
        } else 0.0

    val tiktokEngagementRate: Double
        get() = if (tiktokViews > 0) {
            ((tiktokLikes + tiktokComments + tiktokShares).toDouble() / tiktokViews.toDouble()) * 100.0
        } else 0.0

    val overallEngagementRate: Double
        get() = if (totalViews > 0) {
            ((totalLikes + totalComments + totalShares).toDouble() / totalViews.toDouble()) * 100.0
        } else 0.0
}
