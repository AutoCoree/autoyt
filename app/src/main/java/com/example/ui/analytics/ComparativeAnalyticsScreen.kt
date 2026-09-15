package com.example.ui.analytics

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MetricGreen
import com.example.ui.theme.MetricOrange
import com.example.ui.theme.MetricPurple
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.TikTokPink
import com.example.ui.theme.YouTubeRed
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparativeAnalyticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val campaigns by viewModel.allCampaigns.collectAsStateWithLifecycle()
    val metricsList by viewModel.allMetrics.collectAsStateWithLifecycle()

    var selectedPeriod by remember { mutableStateOf("30 derniers jours") }
    var includeYt by remember { mutableStateOf(true) }
    var includeTt by remember { mutableStateOf(true) }

    val totalYtViews = remember(metricsList) { metricsList.sumOf { it.youtubeViews } }
    val totalTtViews = remember(metricsList) { metricsList.sumOf { it.tiktokViews } }
    val totalYtLikes = remember(metricsList) { metricsList.sumOf { it.youtubeLikes } }
    val totalTtLikes = remember(metricsList) { metricsList.sumOf { it.tiktokLikes } }
    val totalYtShares = remember(metricsList) { metricsList.sumOf { it.youtubeShares } }
    val totalTtShares = remember(metricsList) { metricsList.sumOf { it.tiktokShares } }
    val totalYtWatchHours = remember(metricsList) { metricsList.sumOf { it.youtubeWatchTimeMinutes } / 60.0 }
    val totalTtWatchHours = remember(metricsList) { metricsList.sumOf { it.tiktokWatchTimeMinutes } / 60.0 }

    val ytEngagement = remember(totalYtViews, totalYtLikes, totalYtShares) {
        if (totalYtViews > 0) ((totalYtLikes + totalYtShares).toDouble() / totalYtViews.toDouble()) * 100.0 else 0.0
    }
    val ttEngagement = remember(totalTtViews, totalTtLikes, totalTtShares) {
        if (totalTtViews > 0) ((totalTtLikes + totalTtShares).toDouble() / totalTtViews.toDouble()) * 100.0 else 0.0
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Analyse Comparative en Temps Réel",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                        modifier = Modifier.testTag("button_back_analytics")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Platform Face-off Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // YouTube Side
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(YouTubeRed.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = YouTubeRed, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = "YouTube Shorts", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                    Text(text = "@TechPulse_FR", color = TextSecondary, fontSize = 11.sp)
                                }
                            }

                            Text(text = "VS", fontWeight = FontWeight.Black, color = TikTokPink, fontSize = 14.sp)

                            // TikTok Side
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "TikTok For You", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                                    Text(text = "@techpulse.officiel", color = TextSecondary, fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(TikTokCyan.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(TikTokPink))
                                }
                            }
                        }
                    }
                }
            }

            // Head-to-head metrics comparison
            item {
                Text(
                    text = "Indicateurs Clés de Performance (KPIs)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            item {
                ComparativeMetricRow(
                    metricName = "Vues Totales",
                    ytValue = String.format(Locale.FRENCH, "%,d", totalYtViews),
                    ttValue = String.format(Locale.FRENCH, "%,d", totalTtViews),
                    ytWeight = totalYtViews.toFloat(),
                    ttWeight = totalTtViews.toFloat(),
                    winnerIsTt = totalTtViews > totalYtViews
                )
            }

            item {
                ComparativeMetricRow(
                    metricName = "Likes & Réactions",
                    ytValue = String.format(Locale.FRENCH, "%,d", totalYtLikes),
                    ttValue = String.format(Locale.FRENCH, "%,d", totalTtLikes),
                    ytWeight = totalYtLikes.toFloat(),
                    ttWeight = totalTtLikes.toFloat(),
                    winnerIsTt = totalTtLikes > totalYtLikes
                )
            }

            item {
                ComparativeMetricRow(
                    metricName = "Partages & Viralisations",
                    ytValue = String.format(Locale.FRENCH, "%,d", totalYtShares),
                    ttValue = String.format(Locale.FRENCH, "%,d", totalTtShares),
                    ytWeight = totalYtShares.toFloat(),
                    ttWeight = totalTtShares.toFloat(),
                    winnerIsTt = totalTtShares > totalYtShares
                )
            }

            item {
                ComparativeMetricRow(
                    metricName = "Temps Visionnage",
                    ytValue = "${String.format(Locale.FRENCH, "%.0f", totalYtWatchHours)} h",
                    ttValue = "${String.format(Locale.FRENCH, "%.0f", totalTtWatchHours)} h",
                    ytWeight = totalYtWatchHours.toFloat(),
                    ttWeight = totalTtWatchHours.toFloat(),
                    winnerIsTt = totalTtWatchHours > totalYtWatchHours
                )
            }

            item {
                ComparativeMetricRow(
                    metricName = "Taux d'Engagement",
                    ytValue = "${String.format(Locale.FRENCH, "%.1f", ytEngagement)}%",
                    ttValue = "${String.format(Locale.FRENCH, "%.1f", ttEngagement)}%",
                    ytWeight = ytEngagement.toFloat(),
                    ttWeight = ttEngagement.toFloat(),
                    winnerIsTt = ttEngagement > ytEngagement
                )
            }

            // AI Comparative Insights Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryPurple.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Synthèse Comparative Générée par l'IA",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "• TikTok surpasse YouTube de +68% en vitesse de partage initiale sur les vidéos courtes (< 50 secondes).\n• YouTube Shorts maintient une durée de vie 4.2x plus longue grâce aux recherches pérennes et aux suggestions 'Up Next'.\n• Conseil Stratégique : Conserver le rythme de cuts serrés (1.6s) pour TikTok, et renforcer le premier tiers avec un titre explicite pour YouTube Shorts.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Customizable Report Export Center
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📊 Export de Rapports Personnalisables",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Générez un rapport détaillé pour vos bilans de performances ou vos sponsors.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Period Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("7 jours", "30 derniers jours", "Historique complet").forEach { period ->
                                val isSel = selectedPeriod == period
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedPeriod = period }
                                        .clip(RoundedCornerShape(10.dp)),
                                    color = if (isSel) PrimaryPurple.copy(alpha = 0.2f) else DarkSurface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) PrimaryPurple else DarkBorder)
                                ) {
                                    Text(
                                        text = period,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else TextSecondary,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Export Buttons (CSV & PDF / Executive Report)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.exportCsvReport() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("button_export_csv"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MetricGreen)
                            ) {
                                Icon(imageVector = Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Exporter CSV", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewModel.exportExecutivePdfReport() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("button_export_pdf"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                            ) {
                                Icon(imageVector = Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Rapport Exécutif", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComparativeMetricRow(
    metricName: String,
    ytValue: String,
    ttValue: String,
    ytWeight: Float,
    ttWeight: Float,
    winnerIsTt: Boolean
) {
    val total = (ytWeight + ttWeight).coerceAtLeast(1f)
    val ytPercent = ytWeight / total
    val ttPercent = ttWeight / total

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = metricName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextPrimary)
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (winnerIsTt) TikTokCyan.copy(alpha = 0.15f) else YouTubeRed.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (winnerIsTt) "TikTok en tête" else "YouTube en tête",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (winnerIsTt) TikTokCyan else YouTubeRed,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = ytValue, fontWeight = FontWeight.Bold, color = YouTubeRed, fontSize = 14.sp)
                Text(text = ttValue, fontWeight = FontWeight.Bold, color = TikTokCyan, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Comparative Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .weight(ytPercent.coerceAtLeast(0.05f))
                        .height(8.dp)
                        .background(YouTubeRed)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .weight(ttPercent.coerceAtLeast(0.05f))
                        .height(8.dp)
                        .background(TikTokCyan)
                )
            }
        }
    }
}
