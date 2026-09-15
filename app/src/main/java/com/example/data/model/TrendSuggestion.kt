package com.example.data.model

data class TrendSuggestion(
    val id: String,
    val topic: String,
    val niche: String,
    val platform: PlatformType,
    val viralScore: Int, // e.g. 96
    val sampleHook: String,
    val description: String,
    val suggestedKeywords: List<String>,
    val soundEffectStyle: String,
    val optimalDuration: String,
    val audiencePeakWindow: String
)
