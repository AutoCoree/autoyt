package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ContentCampaign
import kotlinx.coroutines.flow.Flow

@Dao
interface CampaignDao {
    @Query("SELECT * FROM campaigns ORDER BY createdAtMillis DESC")
    fun getAllCampaigns(): Flow<List<ContentCampaign>>

    @Query("SELECT * FROM campaigns WHERE id = :id LIMIT 1")
    suspend fun getCampaignById(id: Long): ContentCampaign?

    @Query("SELECT * FROM campaigns WHERE id = :id LIMIT 1")
    fun observeCampaignById(id: Long): Flow<ContentCampaign?>

    @Query("SELECT * FROM campaigns WHERE status = 'SCHEDULED' ORDER BY scheduledTimeMillis ASC")
    fun getScheduledCampaigns(): Flow<List<ContentCampaign>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: ContentCampaign): Long

    @Update
    suspend fun updateCampaign(campaign: ContentCampaign)

    @Delete
    suspend fun deleteCampaign(campaign: ContentCampaign)

    @Query("DELETE FROM campaigns WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE campaigns SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE campaigns SET scheduledTimeMillis = :newTime, optimalReachScore = :newScore WHERE id = :id")
    suspend fun updateScheduleTime(id: Long, newTime: Long, newScore: Int)
}
