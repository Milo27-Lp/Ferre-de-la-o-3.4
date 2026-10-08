package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CashSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashSessionDao {
    @Query("SELECT * FROM cash_sessions WHERE status = 'ABIERTA' ORDER BY openTimeMillis DESC LIMIT 1")
    fun getActiveSession(): Flow<CashSessionEntity?>

    @Query("SELECT * FROM cash_sessions WHERE status = 'ABIERTA' ORDER BY openTimeMillis DESC LIMIT 1")
    suspend fun getActiveSessionSync(): CashSessionEntity?

    @Query("SELECT * FROM cash_sessions ORDER BY openTimeMillis DESC")
    fun getAllSessions(): Flow<List<CashSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: CashSessionEntity): Long

    @Update
    suspend fun updateSession(session: CashSessionEntity)
}
