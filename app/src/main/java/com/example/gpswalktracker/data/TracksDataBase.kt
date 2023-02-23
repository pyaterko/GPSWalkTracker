package com.example.gpswalktracker.data

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [InfoTrackItem::class], version = 1, exportSchema = false)
abstract class TracksDataBase : RoomDatabase() {
    abstract fun trackDao(): TrackDao

    companion object {

        private const val DB_NAME = "info_track_item.db"
        private var INSTANCE: TracksDataBase? = null
        private val LOCK = Any()

        fun getInstance(application: Application): TracksDataBase {
            INSTANCE?.let { trackDataBase ->
                return trackDataBase
            }
            synchronized(LOCK) {
                INSTANCE?.let { trackDataBase ->
                    return trackDataBase
                }
            }
            val dataBase = Room.databaseBuilder(
                application,
                TracksDataBase::class.java,
                DB_NAME
            ).build()
            INSTANCE = dataBase
            return dataBase
        }
    }

}