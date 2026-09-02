package com.example.data.repository

import com.example.data.local.dao.MusicDao
import com.example.data.local.entity.PlaylistEntity
import com.example.data.local.entity.PlaylistSongCrossRef
import com.example.data.local.entity.RemixPresetEntity
import com.example.data.local.entity.SongEntity
import kotlinx.coroutines.flow.Flow

class MusicRepository(private val musicDao: MusicDao) {

    val allSongs: Flow<List<SongEntity>> = musicDao.getAllSongs()
    val favoriteSongs: Flow<List<SongEntity>> = musicDao.getFavoriteSongs()
    val recentlyPlayed: Flow<List<SongEntity>> = musicDao.getRecentlyPlayedSongs()
    val allPlaylists: Flow<List<PlaylistEntity>> = musicDao.getAllPlaylists()
    val allRemixPresets: Flow<List<RemixPresetEntity>> = musicDao.getAllRemixPresets()

    suspend fun insertSong(song: SongEntity): Long = musicDao.insertSong(song)
    suspend fun insertSongs(songs: List<SongEntity>) = musicDao.insertSongs(songs)
    suspend fun updateSong(song: SongEntity) = musicDao.updateSong(song)
    suspend fun deleteSong(song: SongEntity) = musicDao.deleteSong(song)
    suspend fun deleteDefaultSongs() = musicDao.deleteDefaultSongs()
    suspend fun toggleFavorite(id: Long, isFav: Boolean) = musicDao.setFavorite(id, isFav)
    suspend fun updateLastPlayed(id: Long, timestamp: Long) = musicDao.updateLastPlayed(id, timestamp)
    suspend fun getSongByUri(uri: String): SongEntity? = musicDao.getSongByUri(uri)
    suspend fun getSongByExternalId(externalId: String): SongEntity? = musicDao.getSongByExternalId(externalId)
    suspend fun getSongById(id: Long): SongEntity? = musicDao.getSongById(id)

    suspend fun createPlaylist(name: String, icon: String = "🎵", description: String = ""): Long {
        return musicDao.insertPlaylist(
            PlaylistEntity(
                name = name,
                icon = icon,
                description = description
            )
        )
    }

    suspend fun deletePlaylist(playlist: PlaylistEntity) {
        musicDao.clearPlaylistSongs(playlist.id)
        musicDao.deletePlaylist(playlist)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        musicDao.addSongToPlaylist(
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId
            )
        )
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        musicDao.removeSongFromPlaylist(playlistId, songId)
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<SongEntity>> {
        return musicDao.getSongsForPlaylist(playlistId)
    }

    suspend fun saveRemixPreset(preset: RemixPresetEntity): Long {
        return musicDao.insertRemixPreset(preset)
    }

    suspend fun deleteRemixPreset(preset: RemixPresetEntity) {
        musicDao.deleteRemixPreset(preset)
    }
}
