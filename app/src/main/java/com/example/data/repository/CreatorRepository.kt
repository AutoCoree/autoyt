package com.example.data.repository

import android.content.Context
import com.example.data.local.CreatorFlowDatabase
import com.example.data.model.AlertType
import com.example.data.model.ApiAccountConfig
import com.example.data.model.CampaignMetrics
import com.example.data.model.CampaignStatus
import com.example.data.model.ContentCampaign
import com.example.data.model.EngagementAlert
import com.example.data.model.PlatformType
import com.example.data.model.TrendSuggestion
import com.example.data.model.VideoFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreatorRepository(
    private val database: CreatorFlowDatabase,
    private val scope: CoroutineScope
) {
    private val campaignDao = database.campaignDao()
    private val metricsDao = database.metricsDao()
    private val alertDao = database.alertDao()
    private val accountDao = database.accountDao()

    val allCampaigns: Flow<List<ContentCampaign>> = campaignDao.getAllCampaigns()
    val scheduledCampaigns: Flow<List<ContentCampaign>> = campaignDao.getScheduledCampaigns()
    val allMetrics: Flow<List<CampaignMetrics>> = metricsDao.getAllMetrics()
    val allAlerts: Flow<List<EngagementAlert>> = alertDao.getAllAlerts()
    val unreadAlertsCount: Flow<Int> = alertDao.getUnreadAlertCount()
    val allAccounts: Flow<List<ApiAccountConfig>> = accountDao.getAllAccounts()

    init {
        scope.launch(Dispatchers.IO) {
            checkAndSeedInitialData()
        }
    }

    suspend fun getCampaignById(id: Long): ContentCampaign? = campaignDao.getCampaignById(id)

    fun observeCampaignById(id: Long): Flow<ContentCampaign?> = campaignDao.observeCampaignById(id)

    fun getMetricsForCampaign(campaignId: Long): Flow<CampaignMetrics?> =
        metricsDao.getMetricsForCampaign(campaignId)

    suspend fun getMetricsForCampaignSync(campaignId: Long): CampaignMetrics? =
        metricsDao.getMetricsForCampaignSync(campaignId)

    suspend fun insertOrUpdateCampaign(campaign: ContentCampaign): Long {
        return if (campaign.id == 0L) {
            val id = campaignDao.insertCampaign(campaign)
            // Initialize default metrics for new campaign
            metricsDao.insertOrUpdateMetrics(
                CampaignMetrics(
                    campaignId = id,
                    youtubeViews = 0,
                    tiktokViews = 0,
                    youtubeLikes = 0,
                    tiktokLikes = 0,
                    youtubeComments = 0,
                    tiktokComments = 0,
                    youtubeShares = 0,
                    tiktokShares = 0
                )
            )
            id
        } else {
            campaignDao.updateCampaign(campaign)
            campaign.id
        }
    }

    suspend fun deleteCampaign(campaignId: Long) {
        campaignDao.deleteById(campaignId)
        metricsDao.deleteMetricsForCampaign(campaignId)
    }

    suspend fun updateSchedule(campaignId: Long, scheduledTimeMillis: Long, optimalScore: Int) {
        campaignDao.updateScheduleTime(campaignId, scheduledTimeMillis, optimalScore)
        campaignDao.updateStatus(campaignId, CampaignStatus.SCHEDULED.name)
    }

    suspend fun updateMetrics(metrics: CampaignMetrics) {
        metricsDao.insertOrUpdateMetrics(metrics)
    }

    suspend fun markAlertAsRead(alertId: Long) {
        alertDao.markAsRead(alertId)
    }

    suspend fun markAllAlertsAsRead() {
        alertDao.markAllAsRead()
    }

    suspend fun addAlert(alert: EngagementAlert): Long {
        return alertDao.insertAlert(alert)
    }

    suspend fun updateAccount(account: ApiAccountConfig) {
        accountDao.insertOrUpdateAccount(account)
    }

    suspend fun incrementQuota(platform: String, cost: Int) {
        accountDao.incrementQuota(platform, cost)
    }

    suspend fun simulateEngagementSpike(campaignId: Long): EngagementAlert? = withContext(Dispatchers.IO) {
        val campaign = campaignDao.getCampaignById(campaignId) ?: return@withContext null
        val currentMetrics = metricsDao.getMetricsForCampaignSync(campaignId) ?: CampaignMetrics(campaignId = campaignId)

        val additionalTiktokViews = (15_000L..45_000L).random()
        val additionalYtViews = (8_000L..25_000L).random()
        val additionalTiktokLikes = (additionalTiktokViews * 0.08).toLong()
        val additionalYtLikes = (additionalYtViews * 0.07).toLong()
        val additionalShares = (additionalTiktokViews * 0.03).toLong()

        val updated = currentMetrics.copy(
            tiktokViews = currentMetrics.tiktokViews + additionalTiktokViews,
            youtubeViews = currentMetrics.youtubeViews + additionalYtViews,
            tiktokLikes = currentMetrics.tiktokLikes + additionalTiktokLikes,
            youtubeLikes = currentMetrics.youtubeLikes + additionalYtLikes,
            tiktokShares = currentMetrics.tiktokShares + additionalShares,
            viralVelocityPerHour = (additionalTiktokViews + additionalYtViews),
            engagementSpikeDetected = true,
            lastUpdatedMillis = System.currentTimeMillis()
        )
        metricsDao.insertOrUpdateMetrics(updated)
        campaignDao.updateStatus(campaignId, CampaignStatus.VIRAL_ALERT.name)

        val alert = EngagementAlert(
            campaignId = campaignId,
            campaignTitle = campaign.title,
            platform = "TikTok & YouTube",
            alertType = AlertType.VIRAL_VELOCITY.name,
            message = "Pic viral exceptionnel détecté : +${(additionalTiktokViews + additionalYtViews) / 1000}K vues en moins d'une heure !",
            metricHighlight = "+${(additionalTiktokViews + additionalYtViews) / 1000}K vues/h",
            timestampMillis = System.currentTimeMillis()
        )
        alertDao.insertAlert(alert)
        alert
    }

    suspend fun publishCampaignNow(campaignId: Long): Boolean = withContext(Dispatchers.IO) {
        val campaign = campaignDao.getCampaignById(campaignId) ?: return@withContext false
        campaignDao.updateStatus(campaignId, CampaignStatus.PUBLISHING.name)

        // Simulate network API transmission
        kotlinx.coroutines.delay(1200)

        campaignDao.updateStatus(campaignId, CampaignStatus.PUBLISHED.name)
        val publishedCampaign = campaign.copy(
            status = CampaignStatus.PUBLISHED.name,
            publishedTimeMillis = System.currentTimeMillis(),
            youtubeVideoUrl = "https://youtube.com/shorts/cf_${campaign.id}",
            tiktokVideoUrl = "https://tiktok.com/@creatorflow/video/${campaign.id}"
        )
        campaignDao.updateCampaign(publishedCampaign)

        // Initialize realistic initial metrics for freshly published campaign
        val existingMetrics = metricsDao.getMetricsForCampaignSync(campaignId)
        val initialMetrics = existingMetrics?.copy(
            youtubeViews = (1200L..4500L).random(),
            tiktokViews = (3500L..12000L).random(),
            youtubeLikes = (90L..340L).random(),
            tiktokLikes = (280L..960L).random(),
            youtubeComments = (15L..50L).random(),
            tiktokComments = (40L..160L).random(),
            youtubeShares = (20L..80L).random(),
            tiktokShares = (110L..390L).random(),
            youtubeRetentionRate = 76.4,
            tiktokRetentionRate = 81.2,
            youtubeCtr = 6.8,
            tiktokCompletionRate = 58.5,
            lastUpdatedMillis = System.currentTimeMillis()
        ) ?: CampaignMetrics(
            campaignId = campaignId,
            youtubeViews = 2400L,
            tiktokViews = 6200L,
            youtubeLikes = 180L,
            tiktokLikes = 490L,
            youtubeComments = 32L,
            tiktokComments = 75L,
            youtubeShares = 42L,
            tiktokShares = 180L,
            youtubeRetentionRate = 76.4,
            tiktokRetentionRate = 81.2,
            youtubeCtr = 6.8,
            tiktokCompletionRate = 58.5,
            lastUpdatedMillis = System.currentTimeMillis()
        )
        metricsDao.insertOrUpdateMetrics(initialMetrics)

        // Increment API quotas
        accountDao.incrementQuota(PlatformType.YOUTUBE.name, 100)
        accountDao.incrementQuota(PlatformType.TIKTOK.name, 50)

        true
    }

    private suspend fun checkAndSeedInitialData() {
        val existing = campaignDao.getAllCampaigns().firstOrNull()
        if (existing.isNullOrEmpty()) {
            // Seed API Accounts
            accountDao.insertOrUpdateAccount(
                ApiAccountConfig(
                    platform = PlatformType.YOUTUBE.name,
                    accountName = "TechPulse Studio FR",
                    channelHandle = "@TechPulse_FR",
                    isConnected = true,
                    apiKey = "AIzaSyD-YT_DATA_V3_LIVE_SYNC",
                    accessToken = "ya29.a0AfH6SMA84x9q4K...",
                    quotaUsed = 1450,
                    quotaLimit = 10000,
                    autoPublishEnabled = true,
                    uploadPrivacyDefault = "PUBLIC"
                )
            )
            accountDao.insertOrUpdateAccount(
                ApiAccountConfig(
                    platform = PlatformType.TIKTOK.name,
                    accountName = "TechPulse TikTok",
                    channelHandle = "@techpulse.officiel",
                    isConnected = true,
                    apiKey = "tt_open_api_sec_89201a4bc",
                    accessToken = "act.tiktok.oauth.token.v2.89x",
                    quotaUsed = 420,
                    quotaLimit = 5000,
                    autoPublishEnabled = true,
                    uploadPrivacyDefault = "PUBLIC"
                )
            )

            // Seed Campaigns
            val c1Id = campaignDao.insertCampaign(
                ContentCampaign(
                    title = "5 Outils IA Secrets Qui Changent Tout en 2026",
                    description = "Sélection choc d'intelligences artificielles pour décupler sa productivité et créer du contenu en 60 secondes.",
                    platform = PlatformType.BOTH.name,
                    status = CampaignStatus.VIRAL_ALERT.name,
                    videoFormat = VideoFormat.SHORT.name,
                    videoDurationSeconds = 52,
                    tags = "#ia,#tech,#productivite,#outilsia,#futur",
                    hookText = "Arrête de perdre 4h par jour ! Voici les 5 outils IA que la Silicon Valley garde secrets.",
                    scriptContent = """
[00:00 - 00:03] HOOK : Gros plan caméra, zoom rapide. 'Arrête de perdre 4 heures par jour !'
[00:04 - 00:15] OUTIL 1 : B-Roll écran montrant l'analyse vidéo automatique en 1 clic.
[00:16 - 00:27] OUTIL 2 : Voix off punchy, texte jaune néon animé. Génération de scripts optimisés rétention.
[00:28 - 00:40] OUTIL 3 : Sous-titres dynamiques style Hormozi avec animations emoji sonores.
[00:41 - 00:52] CTA : 'Sauvegarde cette vidéo avant qu'elle ne soit bannie et commente IA pour la liste complète !'
                    """.trimIndent(),
                    editingCues = """
- Montage : Cut tous les 1.8 secondes (Rétention Maximale)
- Transition : Whoosh FX + Glitch zoom sur chaque outil
- Musique : Synthwave dynamique 128 BPM
- Textes : Sous-titres centré jaune/blanc gras avec bordure noire
- B-Roll : Screencasts dynamiques avec curseur agrandi
                    """.trimIndent(),
                    callToAction = "Commente 'IA' et abonne-toi pour le tutoriel complet !",
                    optimalReachScore = 98,
                    createdAtMillis = System.currentTimeMillis() - 86400_000L * 2,
                    publishedTimeMillis = System.currentTimeMillis() - 3600_000L * 6,
                    youtubeVideoUrl = "https://youtube.com/shorts/demo1",
                    tiktokVideoUrl = "https://tiktok.com/@techpulse.officiel/video/101"
                )
            )
            metricsDao.insertOrUpdateMetrics(
                CampaignMetrics(
                    campaignId = c1Id,
                    youtubeViews = 184500L,
                    tiktokViews = 342800L,
                    youtubeLikes = 14200L,
                    tiktokLikes = 38900L,
                    youtubeComments = 1180L,
                    tiktokComments = 3420L,
                    youtubeShares = 4850L,
                    tiktokShares = 18900L,
                    youtubeWatchTimeMinutes = 88560.0,
                    tiktokWatchTimeMinutes = 154260.0,
                    youtubeRetentionRate = 82.4,
                    tiktokRetentionRate = 89.1,
                    youtubeCtr = 8.6,
                    tiktokCompletionRate = 67.2,
                    engagementSpikeDetected = true,
                    viralVelocityPerHour = 48200L
                )
            )

            val c2Id = campaignDao.insertCampaign(
                ContentCampaign(
                    title = "Comment Automatiser 100% de ses Vidéos YouTube & TikTok",
                    description = "Guide pas à pas pour configurer un workflow complet de script, montage et planification automatique.",
                    platform = PlatformType.BOTH.name,
                    status = CampaignStatus.PUBLISHED.name,
                    videoFormat = VideoFormat.TIKTOK_FAST.name,
                    videoDurationSeconds = 64,
                    tags = "#automatisation,#youtube,#tiktok,#business,#ia",
                    hookText = "J'ai généré 500,000 vues sans toucher à un seul logiciel de montage.",
                    scriptContent = """
[00:00 - 00:04] HOOK : 'J'ai généré 500K vues sans toucher un logiciel de montage.'
[00:05 - 00:20] EXPLICATION : Capture du dashboard CreatorFlow et planification automatisée.
[00:21 - 00:45] STRATÉGIE : Comparaison avant/après entre publication manuelle et IA intelligente.
[00:46 - 01:04] CTA : 'Active les notifications et clique sur le lien pour tester.'
                    """.trimIndent(),
                    editingCues = """
- Pacing : Médium-rapide, son 'ding' à chaque étape clé
- Caméra : Alternance plan américain et zoom visage 110%
- Colorimétrie : Contraste élevé, tons bleus et néons
                    """.trimIndent(),
                    callToAction = "Enregistre la vidéo pour l'appliquer ce soir !",
                    optimalReachScore = 93,
                    createdAtMillis = System.currentTimeMillis() - 86400_000L * 4,
                    publishedTimeMillis = System.currentTimeMillis() - 86400_000L * 1,
                    youtubeVideoUrl = "https://youtube.com/shorts/demo2",
                    tiktokVideoUrl = "https://tiktok.com/@techpulse.officiel/video/102"
                )
            )
            metricsDao.insertOrUpdateMetrics(
                CampaignMetrics(
                    campaignId = c2Id,
                    youtubeViews = 72400L,
                    tiktokViews = 98100L,
                    youtubeLikes = 5120L,
                    tiktokLikes = 8450L,
                    youtubeComments = 430L,
                    tiktokComments = 720L,
                    youtubeShares = 1290L,
                    tiktokShares = 3840L,
                    youtubeWatchTimeMinutes = 39820.0,
                    tiktokWatchTimeMinutes = 53955.0,
                    youtubeRetentionRate = 74.6,
                    tiktokRetentionRate = 79.8,
                    youtubeCtr = 6.4,
                    tiktokCompletionRate = 54.3,
                    engagementSpikeDetected = false,
                    viralVelocityPerHour = 3200L
                )
            )

            val c3Id = campaignDao.insertCampaign(
                ContentCampaign(
                    title = "Le Piège Algorithmique TikTok que 99% Ignorent",
                    description = "Analyse de la mise à jour de l'algorithme : pourquoi vos vidéos stagnent à 200 vues et comment débloquer le million.",
                    platform = PlatformType.BOTH.name,
                    status = CampaignStatus.SCHEDULED.name,
                    videoFormat = VideoFormat.SHORT.name,
                    videoDurationSeconds = 48,
                    tags = "#algorithme,#croissance,#tiktoktips,#createurs",
                    hookText = "Si tes vidéos restent bloquées à 200 vues, ce n'est PAS ton contenu le problème.",
                    scriptContent = """
[00:00 - 00:03] HOOK : Graphique rouge descendant à l'écran. '200 vues ? C'est le piège de rétention.'
[00:04 - 00:25] DÉCRYPTAGE : Règle des 3 secondes et swipe-away rate expliqué simplement.
[00:26 - 00:48] SOLUTION : L'astuce du hook inversé avec question ouverte.
                    """.trimIndent(),
                    editingCues = """
- Effets sonores : 'Glass break' au hook, pulsation cardiaque sur le suspense
- Pacing : Très serré, zéro silence (suppression automatique des blancs)
                    """.trimIndent(),
                    callToAction = "Dis-moi en commentaire combien de vues tu as en moyenne !",
                    optimalReachScore = 96,
                    scheduledTimeMillis = System.currentTimeMillis() + 3600_000L * 2 + 1800_000L, // 2h30 from now (peak hour)
                    createdAtMillis = System.currentTimeMillis() - 3600_000L * 3
                )
            )
            metricsDao.insertOrUpdateMetrics(
                CampaignMetrics(
                    campaignId = c3Id,
                    youtubeViews = 0,
                    tiktokViews = 0
                )
            )

            val c4Id = campaignDao.insertCampaign(
                ContentCampaign(
                    title = "Top 3 Prompts pour Révolutionner votre Montage Vidéo",
                    description = "Templates de prompts exacts pour générer des découpages de scènes dynamiques et des sound designs percutants.",
                    platform = PlatformType.BOTH.name,
                    status = CampaignStatus.DRAFT.name,
                    videoFormat = VideoFormat.LONG_FORM.name,
                    videoDurationSeconds = 240,
                    tags = "#montagevideo,#prompts,#premierepro,#capcut",
                    hookText = "Copie-colle ces 3 prompts et regarde ton logiciel monter la vidéo à ta place.",
                    scriptContent = "Brouillon en cours d'édition avec suggestions d'effets visuels.",
                    editingCues = "Rythme chill lofi avec focus démonstratif et incrustation en écran partagé.",
                    callToAction = "Lien du document complet en description !",
                    optimalReachScore = 88,
                    createdAtMillis = System.currentTimeMillis() - 3600_000L
                )
            )
            metricsDao.insertOrUpdateMetrics(
                CampaignMetrics(campaignId = c4Id)
            )

            // Seed Alerts
            alertDao.insertAlert(
                EngagementAlert(
                    campaignId = c1Id,
                    campaignTitle = "5 Outils IA Secrets Qui Changent Tout en 2026",
                    platform = "TikTok & YouTube",
                    alertType = AlertType.VIRAL_VELOCITY.name,
                    message = "Alerte Rétention : La vidéo dépasse +48,200 vues/heure sur TikTok avec 89.1% de complétion !",
                    metricHighlight = "+48.2K vues/h",
                    timestampMillis = System.currentTimeMillis() - 3600_000L * 2,
                    isRead = false
                )
            )
            alertDao.insertAlert(
                EngagementAlert(
                    campaignId = c1Id,
                    campaignTitle = "5 Outils IA Secrets Qui Changent Tout en 2026",
                    platform = "YouTube Shorts",
                    alertType = AlertType.SHARE_SURGE.name,
                    message = "Le taux de partage a dépassé 2.6% sur YouTube Shorts (4,850 partages enregistrés).",
                    metricHighlight = "4,850 partages",
                    timestampMillis = System.currentTimeMillis() - 3600_000L * 5,
                    isRead = true
                )
            )
        }
    }
}
