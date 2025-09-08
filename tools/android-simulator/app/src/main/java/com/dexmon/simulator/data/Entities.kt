package com.dexmon.simulator.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "cached_readings")
data class CachedReading(
    @PrimaryKey val id: String,
    val sensorId: String,
    val userId: String,
    val timestampIso: String,
    val glucoseValue: Int,
    val source: String,
    val version: Int
)

@Dao
interface CachedReadingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<CachedReading>)

    @Query("SELECT * FROM cached_readings WHERE userId = :userId ORDER BY timestampIso DESC LIMIT :limit")
    suspend fun latest(userId: String, limit: Int): List<CachedReading>
}

@Database(entities = [CachedReading::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun readings(): CachedReadingDao
}
