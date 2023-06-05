package com.brave.playlist

import android.content.Context
import android.net.Uri
import android.util.Log
import com.brave.playlist.model.PlaylistItemModel
import com.brave.playlist.util.ConstantUtils
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.ui.DownloadNotificationHelper
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.MimeTypes
import java.io.File
import java.util.concurrent.Executors

object PlaylistDownloadUtils {
    private var dataSourceFactory: DataSource.Factory? = null
    private var httpDataSourceFactory: DataSource.Factory? = null
    private var databaseProvider: DatabaseProvider? = null
    private var downloadDirectory: File? = null
    private var downloadCache: Cache? = null
    private var downloadManager: DownloadManager? = null
    private const val DOWNLOAD_CONTENT_DIRECTORY = "downloads"

    @Synchronized
    fun getHttpDataSourceFactory(): DataSource.Factory? {
        if (httpDataSourceFactory == null) {
            httpDataSourceFactory = DefaultHttpDataSource.Factory()
        }
        return httpDataSourceFactory
    }

    /** Returns a [DataSource.Factory].  */
    @Synchronized
    fun getDataSourceFactory(context: Context): DataSource.Factory {
        if (dataSourceFactory == null) {
            val upstreamFactory = DefaultDataSource.Factory(
                context,
                getHttpDataSourceFactory()!!
            )
            dataSourceFactory =
                getDownloadCache(context)?.let { buildReadOnlyCacheDataSource(upstreamFactory, it) }
        }
        return dataSourceFactory!!
    }

    @Synchronized
    fun getDownloadNotificationHelper(
        context: Context
    ): DownloadNotificationHelper {
        return DownloadNotificationHelper(context, PlaylistVideoService.PLAYLIST_CHANNEL_ID)
    }

    @Synchronized
    fun getDownloadManager(context: Context): DownloadManager? {
        ensureDownloadManagerInitialized(context)
        return downloadManager
    }

    @Synchronized
    private fun getDownloadCache(context: Context): Cache? {
        if (downloadCache == null) {
            val downloadContentDirectory =
                File(getDownloadDirectory(context), DOWNLOAD_CONTENT_DIRECTORY)
            downloadCache = SimpleCache(
                downloadContentDirectory, NoOpCacheEvictor(), getDatabaseProvider(context)!!
            )
        }
        return downloadCache
    }

    @Synchronized
    private fun ensureDownloadManagerInitialized(context: Context) {
        if (downloadManager == null) {
            downloadManager = getDownloadCache(context)?.let {
                DownloadManager(
                    context,
                    getDatabaseProvider(context)!!,
                    it,
                    getHttpDataSourceFactory()!!,
                    Executors.newFixedThreadPool( /* nThreads = */6)
                )
            }
        }
    }

    @Synchronized
    private fun getDatabaseProvider(context: Context): DatabaseProvider? {
        if (databaseProvider == null) {
            databaseProvider = StandaloneDatabaseProvider(context)
        }
        return databaseProvider
    }

    @Synchronized
    private fun getDownloadDirectory(context: Context): File? {
        if (downloadDirectory == null) {
            downloadDirectory = context.getExternalFilesDir( /* type = */null)
            if (downloadDirectory == null) {
                downloadDirectory = context.filesDir
            }
        }
        return downloadDirectory
    }

    private fun buildReadOnlyCacheDataSource(
        upstreamFactory: DataSource.Factory, cache: Cache
    ): CacheDataSource.Factory {
        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(null)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
    }

    @JvmStatic
    fun startDownloadRequest(context : Context, playlistIteModel: PlaylistItemModel) {
        val extension: String = playlistIteModel.mediaPath
            .substring(playlistIteModel.mediaPath.lastIndexOf("."))
        Log.e(ConstantUtils.TAG, "extension : $extension")
        if (playlistIteModel.isCached && extension == ".m3u8") {
            val downloadRequest = DownloadRequest.Builder(playlistIteModel.id, Uri.parse(playlistIteModel.mediaSrc)).setMimeType(
                MimeTypes.APPLICATION_M3U8).build()
            if (getDownloadManager(context)?.downloadIndex?.getDownload(playlistIteModel.id)?.state != Download.STATE_COMPLETED) {
                DownloadService.sendAddDownload(context, PlaylistDownloadService::class.java, downloadRequest, true)
            }
            Log.e(ConstantUtils.TAG, playlistIteModel.name + playlistIteModel.mediaSrc)
        }
    }

    @JvmStatic
    fun removeDownloadRequest(context : Context, playlistIteModel: PlaylistItemModel) {
        val extension: String = playlistIteModel.mediaPath
            .substring(playlistIteModel.mediaPath.lastIndexOf("."))
        Log.e(ConstantUtils.TAG, "extension : $extension")
        if (playlistIteModel.isCached && extension == ".m3u8") {
            DownloadService.sendRemoveDownload(context, PlaylistDownloadService::class.java, playlistIteModel.id, true)
            Log.e(ConstantUtils.TAG, playlistIteModel.name + playlistIteModel.mediaSrc)
        }
    }

    fun getMediaItemFromDownloadRequest(context : Context, playlistIteModel: PlaylistItemModel) : MediaItem? {
        val extension: String = playlistIteModel.mediaPath
            .substring(playlistIteModel.mediaPath.lastIndexOf("."))
        return if (playlistIteModel.isCached && extension == ".m3u8") {
            Log.e(ConstantUtils.TAG, "extension : $extension")
            Log.e(ConstantUtils.TAG, getDownloadManager(context)?.downloadIndex?.getDownload(playlistIteModel.id)?.state.toString())
            val downloadRequest = DownloadRequest.Builder(playlistIteModel.id, Uri.parse(playlistIteModel.mediaSrc)).setMimeType(MimeTypes.APPLICATION_M3U8).build()
            Log.e(ConstantUtils.TAG, playlistIteModel.name + playlistIteModel.mediaSrc)
            downloadRequest.toMediaItem()
        } else {
            null
        }
    }
}