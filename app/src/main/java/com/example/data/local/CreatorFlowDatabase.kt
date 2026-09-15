package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ApiAccountConfig
import com.example.data.model.CampaignMetrics
import com.example.data.model.ContentCampaign
import com.example.data.model.EngagementAlert

@Database(
    entities = [
        ContentCampaign::class,
        CampaignMetrics::class,
        EngagementAlert::class,
        ApiAccountConfig::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CreatorFlowDatabase : RoomDatabase() {
    abstract fun campaignDao(): CampaignDao
    abstract fun metricsDao(): MetricsDao
    abstract fun alertDao(): AlertDao
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: CreatorFlowDatabase? = null

        fun getInstance(context: Context): CreatorFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CreatorFlowDatabase::class.java,
                    "creatorflow_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
