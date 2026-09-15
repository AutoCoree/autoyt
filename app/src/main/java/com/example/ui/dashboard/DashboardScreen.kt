package com.example.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CampaignMetrics
import com.example.data.model.CampaignStatus
import com.example.data.model.ContentCampaign
import com.example.data.model.PlatformType
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.AutoPublishProgressDialog
import com.example.ui.components.MetricStatCard
import com.example.ui.components.PlatformBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.ViralReachScoreBadge
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MetricGreen
import com.example.ui.theme.MetricOrange
import com.example.ui.theme.MetricPurple
import com.example.ui.theme.PrimaryGradientEnd
import com.example.ui.theme.PrimaryGradientStart
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.TikTokPink
import com.example.ui.theme.YouTubeRed
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val campaigns by viewModel.filteredCampaigns.collectAsStateWithLifecycle()
    val allCampaignsList by viewModel.allCampaigns.collectAsStateWithLifecycle()
    val metricsList by viewModel.allMetrics.collectAsStateWithLifecycle()
    val unreadAlerts by viewModel.unreadAlertsCount.collectAsStateWithLifecycle()
    val platformFilter by viewModel.platformFilter.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val isPublishing by viewModel.isPublishing.collectAsStateWithLifecycle()
    val publishProgress by viewModel.publishProgress.collectAsStateWithLifecycle()
    val publishStatusText by viewModel.publishStatusText.collectAsStateWithLifecycle()
    val publishResults by viewModel.publishResults.collectAsStateWithLifecycle()

    val metricsMap = remember(metricsList) {
        metricsList.associateBy { it.campaignId }
    }

    // Cumulative KPIs
    val totalViews = remember(metricsList) {
        metricsList.sumOf { it.totalViews }
    }
    val totalLikes = remember(metricsList) {
        metricsList.sumOf { it.totalLikes }
    }
    val totalShares = remember(metricsList) {
        metricsList.sumOf { it.totalShares }
    }
    val totalWatchHours = remember(metricsList) {
        metricsList.sumOf { it.totalWatchTimeHours }
    }
    val avgEngagementRate = remember(totalViews, totalLikes, totalShares) {
        if (totalViews > 0) ((totalLikes + totalShares).toDouble() / totalViews.toDouble()) * 100.0 else 0.0
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.setEditingCampaign(null)
                    viewModel.navigateTo(AppScreen.SCRIPT_EDITOR)
                },
                containerColor = PrimaryPurple,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_new_script")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Créer")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Nouveau Script IA", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Studio Header
            item {
                StudioHeader(
                    unreadAlerts = unreadAlerts,
                    onAlertsClick = { viewModel.navigateTo(AppScreen.ALERTS) }
                )
            }

            // High Level Metrics
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Vues Totales",
                        value = if (totalViews >= 1000) "${String.format(Locale.FRENCH, "%.1f", totalViews / 1000.0)}K" else "$totalViews",
                        subValue = "+34.2% ce mois",
                        icon = Icons.Default.Visibility,
                        accentColor = TikTokCyan,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Taux Engagement",
                        value = "${String.format(Locale.FRENCH, "%.1f", avgEngagementRate)}%",
                        subValue = "Partages & Likes",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        accentColor = TikTokPink,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricStatCard(
                        title = "Visionnage",
                        value = "${String.format(Locale.FRENCH, "%.0f", totalWatchHours)} h",
                        subValue = "YouTube & TikTok",
                        icon = Icons.Default.PlayArrow,
                        accentColor = MetricGreen,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Campagnes IA",
                        value = "${allCampaignsList.size}",
                        subValue = "${allCampaignsList.count { it.status == CampaignStatus.PUBLISHED.name }} publiées",
                        icon = Icons.Default.Bolt,
                        accentColor = MetricPurple,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Fast Feature Navigation Shortcuts
            item {
                FastFeatureShortcuts(
                    onScriptClick = {
                        viewModel.setEditingCampaign(null)
                        viewModel.navigateTo(AppScreen.SCRIPT_EDITOR)
                    },
                    onTrendsClick = { viewModel.navigateTo(AppScreen.TRENDS) },
                    onSchedulerClick = { viewModel.navigateTo(AppScreen.SCHEDULER) },
                    onAnalyticsClick = { viewModel.navigateTo(AppScreen.ANALYTICS) }
                )
            }

            // Search and Filter Header
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Gestion Unifiée des Contenus",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_campaigns_field"),
                        placeholder = { Text("Rechercher une vidéo, un script ou un tag...", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface,
                            focusedBorderColor = PrimaryPurple,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Platform Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val platformOptions = listOf(
                            "ALL" to "Toutes Plateformes",
                            PlatformType.YOUTUBE.name to "YouTube",
                            PlatformType.TIKTOK.name to "TikTok",
                            PlatformType.BOTH.name to "Multi-Plateformes"
                        )
                        items(platformOptions) { (key, label) ->
                            val isSelected = platformFilter == key
                            FilterChipCustom(
                                label = label,
                                selected = isSelected,
                                onClick = { viewModel.setPlatformFilter(key) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Status Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val statusOptions = listOf(
                            "ALL" to "Tous les Statuts",
                            CampaignStatus.SCHEDULED.name to "Programmés",
                            CampaignStatus.PUBLISHED.name to "Publiés",
                            CampaignStatus.VIRAL_ALERT.name to "🔥 Pics Viraux",
                            CampaignStatus.DRAFT.name to "Brouillons"
                        )
                        items(statusOptions) { (key, label) ->
                            val isSelected = statusFilter == key
                            FilterChipCustom(
                                label = label,
                                selected = isSelected,
                                onClick = { viewModel.setStatusFilter(key) }
                            )
                        }
                    }
                }
            }

            // Campaigns List
            if (campaigns.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Aucune campagne ne correspond aux filtres",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(campaigns, key = { it.id }) { campaign ->
                    val metrics = metricsMap[campaign.id]
                    CampaignCard(
                        campaign = campaign,
                        metrics = metrics,
                        onEdit = {
                            viewModel.setEditingCampaign(campaign)
                            viewModel.navigateTo(AppScreen.SCRIPT_EDITOR)
                        },
                        onPublishNow = { viewModel.startAutoPublish(campaign) },
                        onOptimizeSchedule = {
                            viewModel.calculateSmartScheduleForCampaign(campaign)
                            viewModel.navigateTo(AppScreen.SCHEDULER)
                        },
                        onSimulateSpike = { viewModel.simulateSpike(campaign.id) },
                        onDelete = { viewModel.deleteCampaign(campaign.id) }
                    )
                }
            }
        }
    }

    // Auto publish progress dialog
    AutoPublishProgressDialog(
        isPublishing = isPublishing,
        progress = publishProgress,
        statusText = publishStatusText,
        results = publishResults,
        onDismiss = { viewModel.clearPublishDialog() }
    )
}

@Composable
fun StudioHeader(
    unreadAlerts: Int,
    onAlertsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(PrimaryGradientStart, PrimaryGradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CreatorFlow",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PrimaryPurple.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "IA STUDIO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "YouTube & TikTok Automation Suite",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onAlertsClick,
                modifier = Modifier.testTag("button_alerts_header")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadAlerts > 0) {
                            Badge(
                                containerColor = TikTokPink,
                                contentColor = Color.White
                            ) {
                                Text("$unreadAlerts")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alertes",
                        tint = if (unreadAlerts > 0) TikTokPink else TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun FastFeatureShortcuts(
    onScriptClick: () -> Unit,
    onTrendsClick: () -> Unit,
    onSchedulerClick: () -> Unit,
    onAnalyticsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ShortcutItem(
            title = "Script & Montage",
            icon = Icons.Default.AutoAwesome,
            color = PrimaryPurple,
            onClick = onScriptClick,
            modifier = Modifier.weight(1f)
        )
        ShortcutItem(
            title = "Tendances",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            color = TikTokCyan,
            onClick = onTrendsClick,
            modifier = Modifier.weight(1f)
        )
        ShortcutItem(
            title = "Planning IA",
            icon = Icons.Default.Schedule,
            color = MetricOrange,
            onClick = onSchedulerClick,
            modifier = Modifier.weight(1f)
        )
        ShortcutItem(
            title = "Comparatif",
            icon = Icons.Default.BarChart,
            color = MetricGreen,
            onClick = onAnalyticsClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ShortcutItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("shortcut_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 4.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FilterChipCustom(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) PrimaryPurple else DarkSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) PrimaryPurple else DarkBorder
        )
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else TextSecondary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun CampaignCard(
    campaign: ContentCampaign,
    metrics: CampaignMetrics?,
    onEdit: () -> Unit,
    onPublishNow: () -> Unit,
    onOptimizeSchedule: () -> Unit,
    onSimulateSpike: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("campaign_card_${campaign.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (campaign.status == CampaignStatus.VIRAL_ALERT.name) TikTokPink.copy(alpha = 0.6f) else DarkBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row with badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PlatformBadge(platform = campaign.platform)
                    Spacer(modifier = Modifier.width(6.dp))
                    StatusBadge(status = campaign.status)
                }
                ViralReachScoreBadge(score = campaign.optimalReachScore)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = campaign.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (campaign.hookText.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hook : \"${campaign.hookText}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = TikTokCyan,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Live Metrics row (if published or viral alert)
            if (metrics != null && metrics.totalViews > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MetricMiniStat(
                            icon = Icons.Default.Visibility,
                            value = "${String.format(Locale.FRENCH, "%,d", metrics.totalViews)}",
                            label = "Vues",
                            tint = TikTokCyan
                        )
                        MetricMiniStat(
                            icon = Icons.Default.ThumbUp,
                            value = "${String.format(Locale.FRENCH, "%,d", metrics.totalLikes)}",
                            label = "Likes",
                            tint = MetricGreen
                        )
                        MetricMiniStat(
                            icon = Icons.Default.Share,
                            value = "${String.format(Locale.FRENCH, "%,d", metrics.totalShares)}",
                            label = "Partages",
                            tint = TikTokPink
                        )
                        MetricMiniStat(
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            value = "${String.format(Locale.FRENCH, "%.1f", metrics.overallEngagementRate)}%",
                            label = "Taux Eng.",
                            tint = MetricPurple
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Script & Cues", fontSize = 11.sp)
                }

                if (campaign.status != CampaignStatus.PUBLISHED.name) {
                    Button(
                        onClick = onPublishNow,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("button_publish_${campaign.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Publier API", fontSize = 11.sp, color = Color.White)
                    }
                } else {
                    OutlinedButton(
                        onClick = onSimulateSpike,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("button_spike_${campaign.id}"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TikTokPink),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TikTokPink.copy(alpha = 0.5f))
                    ) {
                        Text(text = "⚡ Simuler Pic", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MetricMiniStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    tint: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
        Text(text = label, fontSize = 10.sp, color = TextSecondary)
    }
}
