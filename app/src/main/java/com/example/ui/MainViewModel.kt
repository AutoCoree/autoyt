package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AiGeneratedScript
import com.example.data.api.ApiStatusCheck
import com.example.data.api.GeminiContentService
import com.example.data.api.PublishResult
import com.example.data.api.SmartScheduleRecommendation
import com.example.data.api.SocialAutoPublishService
import com.example.data.local.CreatorFlowDatabase
import com.example.data.model.ApiAccountConfig
import com.example.data.model.CampaignMetrics
import com.example.data.model.CampaignStatus
import com.example.data.model.ContentCampaign
import com.example.data.model.EngagementAlert
import com.example.data.model.PlatformType
import com.example.data.model.TrendSuggestion
import com.example.data.model.VideoFormat
import com.example.data.repository.CreatorRepository
import com.example.data.util.NotificationHelper
import com.example.data.util.ReportExportHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen(val title: String) {
    DASHBOARD("Tableau de bord"),
    SCRIPT_EDITOR("Éditeur de Script IA"),
    TRENDS("Tendances Virales"),
    SCHEDULER("Programmation IA"),
    ANALYTICS("Analyse Comparative"),
    API_INTEGRATIONS("Intégrations API"),
    ALERTS("Alertes d'Engagement")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = CreatorFlowDatabase.getInstance(application)
    private val repository = CreatorRepository(database, viewModelScope)
    private val geminiService = GeminiContentService()
    private val publishService = SocialAutoPublishService()

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Dashboard Filters
    private val _platformFilter = MutableStateFlow("ALL") // ALL, YOUTUBE, TIKTOK, BOTH
    val platformFilter: StateFlow<String> = _platformFilter.asStateFlow()

    private val _statusFilter = MutableStateFlow("ALL") // ALL, DRAFT, SCHEDULED, PUBLISHED, VIRAL_ALERT
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Repository Flows
    val allCampaigns: StateFlow<List<ContentCampaign>> = repository.allCampaigns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduledCampaigns: StateFlow<List<ContentCampaign>> = repository.scheduledCampaigns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMetrics: StateFlow<List<CampaignMetrics>> = repository.allMetrics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlerts: StateFlow<List<EngagementAlert>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadAlertsCount: StateFlow<Int> = repository.unreadAlertsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allAccounts: StateFlow<List<ApiAccountConfig>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Campaigns
    val filteredCampaigns: StateFlow<List<ContentCampaign>> = combine(
        allCampaigns,
        platformFilter,
        statusFilter,
        searchQuery
    ) { campaigns, platform, status, query ->
        campaigns.filter { c ->
            val matchPlatform = platform == "ALL" || c.platform == platform
            val matchStatus = status == "ALL" || c.status == status
            val matchQuery = query.isBlank() || c.title.contains(query, ignoreCase = true) || c.tags.contains(query, ignoreCase = true)
            matchPlatform && matchStatus && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Script Editor State
    private val _editingCampaign = MutableStateFlow<ContentCampaign?>(null)
    val editingCampaign: StateFlow<ContentCampaign?> = _editingCampaign.asStateFlow()

    private val _isGeneratingScript = MutableStateFlow(false)
    val isGeneratingScript: StateFlow<Boolean> = _isGeneratingScript.asStateFlow()

    // Trends State
    private val _trendSuggestions = MutableStateFlow<List<TrendSuggestion>>(emptyList())
    val trendSuggestions: StateFlow<List<TrendSuggestion>> = _trendSuggestions.asStateFlow()

    private val _selectedNiche = MutableStateFlow("Tech & Intelligence Artificielle")
    val selectedNiche: StateFlow<String> = _selectedNiche.asStateFlow()

    private val _isLoadingTrends = MutableStateFlow(false)
    val isLoadingTrends: StateFlow<Boolean> = _isLoadingTrends.asStateFlow()

    // Smart Scheduling State
    private val _smartScheduleRecommendation = MutableStateFlow<SmartScheduleRecommendation?>(null)
    val smartScheduleRecommendation: StateFlow<SmartScheduleRecommendation?> = _smartScheduleRecommendation.asStateFlow()

    private val _isCalculatingSchedule = MutableStateFlow(false)
    val isCalculatingSchedule: StateFlow<Boolean> = _isCalculatingSchedule.asStateFlow()

    // Publishing State
    private val _isPublishing = MutableStateFlow(false)
    val isPublishing: StateFlow<Boolean> = _isPublishing.asStateFlow()

    private val _publishProgress = MutableStateFlow(0f)
    val publishProgress: StateFlow<Float> = _publishProgress.asStateFlow()

    private val _publishStatusText = MutableStateFlow("")
    val publishStatusText: StateFlow<String> = _publishStatusText.asStateFlow()

    private val _publishResults = MutableStateFlow<List<PublishResult>>(emptyList())
    val publishResults: StateFlow<List<PublishResult>> = _publishResults.asStateFlow()

    // API Ping Status
    private val _apiPingStatuses = MutableStateFlow<Map<String, ApiStatusCheck>>(emptyMap())
    val apiPingStatuses: StateFlow<Map<String, ApiStatusCheck>> = _apiPingStatuses.asStateFlow()

    init {
        // Load initial trends
        loadTrendSuggestions(_selectedNiche.value)
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun setPlatformFilter(filter: String) {
        _platformFilter.value = filter
    }

    fun setStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setEditingCampaign(campaign: ContentCampaign?) {
        _editingCampaign.value = campaign
    }

    fun setSelectedNiche(niche: String) {
        _selectedNiche.value = niche
        loadTrendSuggestions(niche)
    }

    fun loadTrendSuggestions(niche: String) {
        viewModelScope.launch {
            _isLoadingTrends.value = true
            try {
                val list = geminiService.generateTrendSuggestions(niche)
                _trendSuggestions.value = list
            } catch (e: Exception) {
                // Keep existing or fallback
            } finally {
                _isLoadingTrends.value = false
            }
        }
    }

    fun generateScriptWithAi(
        topic: String,
        platform: String,
        tone: String,
        durationSeconds: Int
    ) {
        viewModelScope.launch {
            _isGeneratingScript.value = true
            try {
                val aiScript = geminiService.generateScriptAndBlueprint(topic, platform, tone, durationSeconds)
                val current = _editingCampaign.value
                val updated = (current ?: ContentCampaign(
                    title = aiScript.title,
                    description = "Généré par CreatorFlow IA",
                    platform = platform,
                    videoDurationSeconds = durationSeconds
                )).copy(
                    title = aiScript.title,
                    hookText = aiScript.hookText,
                    scriptContent = aiScript.scriptContent,
                    editingCues = aiScript.editingCues,
                    callToAction = aiScript.callToAction,
                    tags = aiScript.tags,
                    optimalReachScore = aiScript.optimalReachScore
                )
                _editingCampaign.value = updated
            } catch (e: Exception) {
                // error handled
            } finally {
                _isGeneratingScript.value = false
            }
        }
    }

    fun saveEditingCampaign(campaign: ContentCampaign) {
        viewModelScope.launch {
            repository.insertOrUpdateCampaign(campaign)
            _editingCampaign.value = null
            _currentScreen.value = AppScreen.DASHBOARD
        }
    }

    fun deleteCampaign(campaignId: Long) {
        viewModelScope.launch {
            repository.deleteCampaign(campaignId)
        }
    }

    fun calculateSmartScheduleForCampaign(campaign: ContentCampaign) {
        viewModelScope.launch {
            _isCalculatingSchedule.value = true
            try {
                val recommendation = geminiService.calculateSmartSchedule(campaign.platform, campaign.title)
                _smartScheduleRecommendation.value = recommendation
            } finally {
                _isCalculatingSchedule.value = false
            }
        }
    }

    fun applySmartSchedule(campaignId: Long, timeMillis: Long, score: Int) {
        viewModelScope.launch {
            repository.updateSchedule(campaignId, timeMillis, score)
            _smartScheduleRecommendation.value = null
        }
    }

    fun startAutoPublish(campaign: ContentCampaign) {
        viewModelScope.launch {
            _isPublishing.value = true
            _publishProgress.value = 0f
            _publishStatusText.value = "Connexion aux passerelles YouTube & TikTok..."

            val results = publishService.executeAutoPublish(campaign) { stage, progress ->
                _publishStatusText.value = stage
                _publishProgress.value = progress
            }

            _publishResults.value = results
            repository.publishCampaignNow(campaign.id)
            _isPublishing.value = false
        }
    }

    fun clearPublishDialog() {
        _publishResults.value = emptyList()
        _publishStatusText.value = ""
        _publishProgress.value = 0f
    }

    fun testApiConnection(account: ApiAccountConfig) {
        viewModelScope.launch {
            val status = publishService.pingPlatformApi(account)
            val current = _apiPingStatuses.value.toMutableMap()
            current[account.platform] = status
            _apiPingStatuses.value = current
        }
    }

    fun updateAccountConfig(account: ApiAccountConfig) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun simulateSpike(campaignId: Long) {
        viewModelScope.launch {
            val alert = repository.simulateEngagementSpike(campaignId)
            if (alert != null) {
                NotificationHelper.showViralSpikeNotification(getApplication(), alert)
            }
        }
    }

    fun markAlertAsRead(id: Long) {
        viewModelScope.launch {
            repository.markAlertAsRead(id)
        }
    }

    fun markAllAlertsAsRead() {
        viewModelScope.launch {
            repository.markAllAlertsAsRead()
        }
    }

    fun exportCsvReport() {
        val campaigns = allCampaigns.value
        val metricsList = allMetrics.value
        val metricsMap = metricsList.associateBy { it.campaignId }
        val csv = ReportExportHelper.generateCsvReport(campaigns, metricsMap)
        ReportExportHelper.shareTextReport(getApplication(), csv, "Rapport_CreatorFlow.csv", isCsv = true)
    }

    fun exportExecutivePdfReport() {
        val campaigns = allCampaigns.value
        val metricsList = allMetrics.value
        val metricsMap = metricsList.associateBy { it.campaignId }
        val report = ReportExportHelper.generateExecutiveSummaryReport(campaigns, metricsMap)
        ReportExportHelper.shareTextReport(getApplication(), report, "Rapport_Executif_CreatorFlow.txt", isCsv = false)
    }

    fun createCampaignFromTrend(trend: TrendSuggestion) {
        val newCampaign = ContentCampaign(
            title = trend.topic,
            description = trend.description,
            platform = trend.platform.name,
            status = CampaignStatus.DRAFT.name,
            videoFormat = VideoFormat.SHORT.name,
            hookText = trend.sampleHook,
            tags = trend.suggestedKeywords.joinToString(",") { "#${it.replace(" ", "")}" },
            optimalReachScore = trend.viralScore
        )
        _editingCampaign.value = newCampaign
        _currentScreen.value = AppScreen.SCRIPT_EDITOR
        // Trigger AI auto-script generation right away
        generateScriptWithAi(
            topic = trend.topic,
            platform = trend.platform.name,
            tone = "Viral & Percutant",
            durationSeconds = 45
        )
    }
}
