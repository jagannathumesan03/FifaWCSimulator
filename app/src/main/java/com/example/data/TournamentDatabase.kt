package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchPredictionDao {
    @Query("SELECT * FROM match_predictions")
    fun getAllPredictionsFlow(): Flow<List<MatchPredictionEntity>>

    @Query("SELECT * FROM match_predictions")
    suspend fun getAllPredictions(): List<MatchPredictionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: MatchPredictionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(predictions: List<MatchPredictionEntity>)

    @Query("DELETE FROM match_predictions")
    suspend fun clearAllPredictions()
}

@Dao
interface SavedBracketDao {
    @Query("SELECT * FROM saved_brackets ORDER BY timestamp DESC")
    fun getAllBracketsFlow(): Flow<List<SavedBracketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBracket(bracket: SavedBracketEntity)

    @Query("DELETE FROM saved_brackets WHERE id = :id")
    suspend fun deleteBracketById(id: String)

    @Query("SELECT * FROM saved_brackets WHERE id = :id LIMIT 1")
    suspend fun getBracketById(id: String): SavedBracketEntity?
}

@Database(
    entities = [MatchPredictionEntity::class, SavedBracketEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TournamentDatabase : RoomDatabase() {
    abstract fun matchPredictionDao(): MatchPredictionDao
    abstract fun savedBracketDao(): SavedBracketDao

    companion object {
        @Volatile
        private var INSTANCE: TournamentDatabase? = null

        fun getDatabase(context: Context): TournamentDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TournamentDatabase::class.java,
                    "tournament_predictor_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
