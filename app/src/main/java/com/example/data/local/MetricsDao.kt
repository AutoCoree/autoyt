package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CampaignMetrics
import kotlinx.coroutines.flow.Flow

@Dao
interface MetricsDao {
    @Query("SELECT * FROM campaign_metrics")
    fun getAllMetrics(): Flow<List<CampaignMetrics>>

    @Query("SELECT * FROM campaign_metrics WHERE campaignId = :campaignId LIMIT 1")
    fun getMetricsForCampaign(campaignId: Long): Flow<CampaignMetrics?>

    @Query("SELECT * FROM campaign_metrics WHERE campaignId = :campaignId LIMIT 1")
    suspend fun getMetricsForCampaignSync(campaignId: Long): CampaignMetrics?

    @Query("SELECT * FROM campaign_metrics WHERE engagementSpikeDetected = 1")
    fun getViralSpikes(): Flow<List<CampaignMetrics>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMetrics(metrics: CampaignMetrics): Long

    @Update
    suspend fun updateMetrics(metrics: CampaignMetrics)

    @Query("DELETE FROM campaign_metrics WHERE campaignId = :campaignId")
    suspend fun deleteMetricsForCampaign(campaignId: Long)
}
