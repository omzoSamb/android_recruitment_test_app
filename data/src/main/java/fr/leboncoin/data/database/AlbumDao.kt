package fr.leboncoin.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(albums: List<AlbumEntity>)

    @Query("DELETE FROM albums")
    suspend fun deleteAll()

    @Query("SELECT * FROM albums WHERE id = :id")
    suspend fun getAlbumById(id: Int): AlbumEntity?

    @Query("UPDATE albums SET isFavorite = :isFavorite WHERE id = :albumId")
    suspend fun updateFavoriteStatus(albumId: Int, isFavorite: Boolean)

    @Query("SELECT isFavorite FROM albums WHERE id = :albumId")
    suspend fun isFavorite(albumId: Int): Boolean

    @Query("SELECT * FROM albums WHERE isFavorite = 1")
    fun getFavoriteAlbums(): Flow<List<AlbumEntity>>
}
