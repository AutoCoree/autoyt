package com.example.ui.script_editor

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ContentCampaign
import com.example.data.model.PlatformType
import com.example.data.model.VideoFormat
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScriptEditorScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val editingCampaign by viewModel.editingCampaign.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGeneratingScript.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    var topic by remember { mutableStateOf(editingCampaign?.title ?: "") }
    var selectedPlatform by remember { mutableStateOf(editingCampaign?.platform ?: PlatformType.BOTH.name) }
    var selectedTone by remember { mutableStateOf("Viral & Choc") }
    var selectedDuration by remember { mutableIntStateOf(editingCampaign?.videoDurationSeconds ?: 45) }

    var hookText by remember { mutableStateOf(editingCampaign?.hookText ?: "") }
    var scriptContent by remember { mutableStateOf(editingCampaign?.scriptContent ?: "") }
    var editingCues by remember { mutableStateOf(editingCampaign?.editingCues ?: "") }
    var callToAction by remember { mutableStateOf(editingCampaign?.callToAction ?: "") }
    var tags by remember { mutableStateOf(editingCampaign?.tags ?: "#viral,#ia,#creators") }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showCopiedToast by remember { mutableStateOf(false) }

    // Sync when editingCampaign updates from AI generation
    LaunchedEffect(editingCampaign) {
        editingCampaign?.let {
            topic = it.title
            selectedPlatform = it.platform
            selectedDuration = it.videoDurationSeconds
            hookText = it.hookText
            scriptContent = it.scriptContent
            editingCues = it.editingCues
            callToAction = it.callToAction
            tags = it.tags
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editingCampaign == null) "Nouvelle Vidéo IA" else "Éditeur Script & Montage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                        modifier = Modifier.testTag("button_back_script_editor")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    if (scriptContent.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val fullText = "TITRE: $topic\nHOOK: $hookText\n\nSCRIPT:\n$scriptContent\n\nMONTAGE & CUES:\n$editingCues\n\nCTA: $callToAction\nTAGS: $tags"
                                clipboardManager.setText(AnnotatedString(fullText))
                                showCopiedToast = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copier",
                                tint = TikTokCyan
                            )
                        }
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
            // AI Prompt Configuration Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryPurple.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = PrimaryPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Générateur de Script & Montage IA",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Topic Input
                        OutlinedTextField(
                            value = topic,
                            onValueChange = { topic = it },
                            label = { Text("Sujet ou Idée de la vidéo") },
                            placeholder = { Text("ex: 5 outils IA méconnus pour automatiser son business") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_video_topic"),
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // Platform Selector
                        Text(text = "Plateforme Cible", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val platforms = listOf(
                                PlatformType.BOTH.name to "Multi (YT + TT)",
                                PlatformType.YOUTUBE.name to "YouTube Shorts",
                                PlatformType.TIKTOK.name to "TikTok Viral"
                            )
                            platforms.forEach { (pKey, pLabel) ->
                                val isSelected = selectedPlatform == pKey
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedPlatform = pKey }
                                        .clip(RoundedCornerShape(10.dp)),
                                    color = if (isSelected) PrimaryPurple.copy(alpha = 0.25f) else DarkSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) PrimaryPurple else DarkBorder
                                    )
                                ) {
                                    Text(
                                        text = pLabel,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tone Selector
                        Text(text = "Ton & Énergie", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val tones = listOf("Viral & Choc", "Éducatif & Rapide", "Storytelling", "Humour & Punchline", "Mystérieux")
                            items(tones) { t ->
                                val isSelected = selectedTone == t
                                Surface(
                                    modifier = Modifier
                                        .clickable { selectedTone = t }
                                        .clip(RoundedCornerShape(16.dp)),
                                    color = if (isSelected) TikTokCyan.copy(alpha = 0.2f) else DarkSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) TikTokCyan else DarkBorder
                                    )
                                ) {
                                    Text(
                                        text = t,
                                        fontSize = 11.sp,
                                        color = if (isSelected) TikTokCyan else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Duration Selector
                        Text(text = "Durée Estimée", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(30 to "30 sec", 45 to "45 sec", 60 to "60 sec", 180 to "Format Long").forEach { (dur, label) ->
                                val isSelected = selectedDuration == dur
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedDuration = dur }
                                        .clip(RoundedCornerShape(10.dp)),
                                    color = if (isSelected) MetricGreen.copy(alpha = 0.2f) else DarkSurface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) MetricGreen else DarkBorder
                                    )
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        color = if (isSelected) MetricGreen else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // AI Generate Button
                        Button(
                            onClick = {
                                viewModel.generateScriptWithAi(
                                    topic = topic.ifBlank { "5 Astuces Incroyables pour Créateurs" },
                                    platform = selectedPlatform,
                                    tone = selectedTone,
                                    durationSeconds = selectedDuration
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("button_generate_ai_script"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                            enabled = !isGenerating
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Génération IA en cours...")
                            } else {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (scriptContent.isBlank()) "Générer le Script & Montage IA" else "Régénérer avec l'IA",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Tabs for Structured Script Breakdown
            if (scriptContent.isNotBlank() || hookText.isNotBlank()) {
                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = DarkSurface,
                        contentColor = PrimaryPurple,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = PrimaryPurple
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("Hook (0-3s)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Script Scènes", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = { Text("Montage IA", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        Tab(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            text = { Text("CTA & SEO", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }

                // Tab Content View
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            when (selectedTab) {
                                0 -> {
                                    // Hook Editor
                                    Text(
                                        text = "⚡ Accroche Virale (Les 3 Premières Secondes)",
                                        fontWeight = FontWeight.Bold,
                                        color = TikTokCyan,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Le hook détermine 80% du taux de swipe. Il doit combiner voix forte et déclencheur visuel.",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = hookText,
                                        onValueChange = { hookText = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(110.dp)
                                            .testTag("input_hook_text"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = DarkSurface,
                                            unfocusedContainerColor = DarkSurface,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = TikTokCyan,
                                            unfocusedBorderColor = DarkBorder
                                        )
                                    )
                                }
                                1 -> {
                                    // Full Script Editor
                                    Text(
                                        text = "🎬 Script Vidéo & Dialogues par Scène",
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryPurple,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Dialogues précis calibrés pour un débit dynamique sans temps mort.",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = scriptContent,
                                        onValueChange = { scriptContent = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(240.dp)
                                            .testTag("input_script_content"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = DarkSurface,
                                            unfocusedContainerColor = DarkSurface,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = PrimaryPurple,
                                            unfocusedBorderColor = DarkBorder
                                        )
                                    )
                                }
                                2 -> {
                                    // Editing Cues (Montage IA)
                                    Text(
                                        text = "✂️ Blueprint de Montage IA (Effets, Cuts & SFX)",
                                        fontWeight = FontWeight.Bold,
                                        color = TikTokPink,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Directives automatisées pour l'éditeur vidéo : timing des cuts, B-rolls et style de sous-titres.",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = editingCues,
                                        onValueChange = { editingCues = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(220.dp)
                                            .testTag("input_editing_cues"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = DarkSurface,
                                            unfocusedContainerColor = DarkSurface,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = TikTokPink,
                                            unfocusedBorderColor = DarkBorder
                                        )
                                    )
                                }
                                3 -> {
                                    // CTA & Tags
                                    Text(
                                        text = "🎯 Call To Action (Appel à l'action)",
                                        fontWeight = FontWeight.Bold,
                                        color = MetricGreen,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = callToAction,
                                        onValueChange = { callToAction = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = DarkSurface,
                                            unfocusedContainerColor = DarkSurface,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = MetricGreen,
                                            unfocusedBorderColor = DarkBorder
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "🏷️ Hashtags & Mots-clés SEO",
                                        fontWeight = FontWeight.Bold,
                                        color = MetricOrange,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = tags,
                                        onValueChange = { tags = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = DarkSurface,
                                            unfocusedContainerColor = DarkSurface,
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary,
                                            focusedBorderColor = MetricOrange,
                                            unfocusedBorderColor = DarkBorder
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Final Action Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val current = editingCampaign
                                val toSave = (current ?: ContentCampaign(
                                    title = topic.ifBlank { "Sans titre" },
                                    description = "Créé via Éditeur CreatorFlow",
                                    platform = selectedPlatform,
                                    videoDurationSeconds = selectedDuration
                                )).copy(
                                    title = topic.ifBlank { "Sans titre" },
                                    platform = selectedPlatform,
                                    videoDurationSeconds = selectedDuration,
                                    hookText = hookText,
                                    scriptContent = scriptContent,
                                    editingCues = editingCues,
                                    callToAction = callToAction,
                                    tags = tags
                                )
                                viewModel.saveEditingCampaign(toSave)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("button_save_campaign"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MetricGreen)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Enregistrer", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                val current = editingCampaign
                                val toSave = (current ?: ContentCampaign(
                                    title = topic.ifBlank { "Sans titre" },
                                    description = "Créé via Éditeur CreatorFlow",
                                    platform = selectedPlatform,
                                    videoDurationSeconds = selectedDuration
                                )).copy(
                                    title = topic.ifBlank { "Sans titre" },
                                    platform = selectedPlatform,
                                    videoDurationSeconds = selectedDuration,
                                    hookText = hookText,
                                    scriptContent = scriptContent,
                                    editingCues = editingCues,
                                    callToAction = callToAction,
                                    tags = tags
                                )
                                viewModel.calculateSmartScheduleForCampaign(toSave)
                                viewModel.navigateTo(AppScreen.SCHEDULER)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("button_schedule_from_script"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MetricOrange)
                        ) {
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Programmer IA", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
