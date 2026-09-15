package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ApiAccountConfig
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM api_accounts")
    fun getAllAccounts(): Flow<List<ApiAccountConfig>>

    @Query("SELECT * FROM api_accounts WHERE platform = :platform LIMIT 1")
    fun getAccountByPlatform(platform: String): Flow<ApiAccountConfig?>

    @Query("SELECT * FROM api_accounts WHERE platform = :platform LIMIT 1")
    suspend fun getAccountByPlatformSync(platform: String): ApiAccountConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAccount(account: ApiAccountConfig): Long

    @Update
    suspend fun updateAccount(account: ApiAccountConfig)

    @Query("UPDATE api_accounts SET isConnected = :connected WHERE platform = :platform")
    suspend fun updateConnectionStatus(platform: String, connected: Boolean)

    @Query("UPDATE api_accounts SET quotaUsed = quotaUsed + :cost WHERE platform = :platform")
    suspend fun incrementQuota(platform: String, cost: Int)
}
