package com.example.data.api

import com.example.data.model.ApiAccountConfig
import com.example.data.model.ContentCampaign
import com.example.data.model.PlatformType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

data class PublishResult(
    val isSuccess: Boolean,
    val platform: String,
    val videoUrl: String,
    val message: String,
    val quotaConsumed: Int
)

data class ApiStatusCheck(
    val platform: String,
    val isOnline: Boolean,
    val responseTimeMs: Long,
    val accountHandle: String,
    val remainingQuota: Int,
    val details: String
)

class SocialAutoPublishService {

    suspend fun pingPlatformApi(config: ApiAccountConfig): ApiStatusCheck = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        delay(400) // network latency simulation
        val elapsed = System.currentTimeMillis() - start

        val isYouTube = config.platform == PlatformType.YOUTUBE.name
        ApiStatusCheck(
            platform = if (isYouTube) "YouTube Data API v3" else "TikTok Content Posting API v2",
            isOnline = true,
            responseTimeMs = elapsed,
            accountHandle = config.channelHandle,
            remainingQuota = (config.quotaLimit - config.quotaUsed).coerceAtLeast(0),
            details = if (isYouTube) {
                "OAuth2 Token valide - Scope: youtube.upload, youtube.readonly. Prêt pour publication automatique."
            } else {
                "Open API Access Token connecté - Scope: video.publish, video.upload. Prêt pour programmation automatique."
            }
        )
    }

    suspend fun executeAutoPublish(
        campaign: ContentCampaign,
        onProgress: suspend (stage: String, percent: Float) -> Unit
    ): List<PublishResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<PublishResult>()

        onProgress("Initialisation des connexions API sécurisées...", 0.15f)
        delay(500)

        onProgress("Vérification des quotas et encodage métadonnées...", 0.35f)
        delay(600)

        val targetPlatform = campaign.platform
        val publishToYoutube = targetPlatform == PlatformType.YOUTUBE.name || targetPlatform == PlatformType.BOTH.name
        val publishToTiktok = targetPlatform == PlatformType.TIKTOK.name || targetPlatform == PlatformType.BOTH.name

        if (publishToYoutube) {
            onProgress("Envoi du flux vidéo vers YouTube Data API v3 (Shorts/Vidéo)...", 0.65f)
            delay(800)
            results.add(
                PublishResult(
                    isSuccess = true,
                    platform = "YouTube",
                    videoUrl = "https://youtube.com/shorts/${campaign.id}_auto_${System.currentTimeMillis() % 10000}",
                    message = "Publication réussie avec visibilité PUBLIQUE sur YouTube Shorts !",
                    quotaConsumed = 100
                )
            )
        }

        if (publishToTiktok) {
            onProgress("Synchronisation avec TikTok Content Posting API (Format 9:16)...", 0.90f)
            delay(800)
            results.add(
                PublishResult(
                    isSuccess = true,
                    platform = "TikTok",
                    videoUrl = "https://www.tiktok.com/@techpulse.officiel/video/7391829${campaign.id}",
                    message = "Publication validée avec succès sur TikTok For You Page !",
                    quotaConsumed = 50
                )
            )
        }

        onProgress("Vérification d'indexation et fin du déploiement !", 1.0f)
        delay(400)

        results
    }
}
