package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.EngagementAlert
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {
    @Query("SELECT * FROM engagement_alerts ORDER BY timestampMillis DESC")
    fun getAllAlerts(): Flow<List<EngagementAlert>>

    @Query("SELECT COUNT(*) FROM engagement_alerts WHERE isRead = 0")
    fun getUnreadAlertCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: EngagementAlert): Long

    @Query("UPDATE engagement_alerts SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE engagement_alerts SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM engagement_alerts")
    suspend fun clearAll()
}
