package com.example.gpswalktracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface TrackDao {

    @Query("SELECT * FROM info_track_table")
    fun getList(): Flow<List<InfoTrackItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItem(item: InfoTrackItem)

    @Query("DELETE FROM info_track_table WHERE id=:itemId")
    suspend fun deleteItem(itemId: Int)

    @Query("SELECT * FROM info_track_table WHERE id=:itemId LIMIT 1")
    suspend fun getItemById(itemId: Int): InfoTrackItem

    @Query("DELETE FROM info_track_table")
    suspend fun deleteAll()
}
