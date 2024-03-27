package com.lorick.chatterbox.videoOrAudioControls

import android.annotation.SuppressLint
import android.content.Context
import android.os.StatFs
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File


class MediaCache private constructor(context: Context) {

    val cacheFactory: CacheDataSource.Factory


    private val maxCacheSize = getAvailableCacheSize(context)


    private val cacheDirectoryName: File = context.cacheDir

    init {
        cacheFactory = setupExoPlayerCache(context)
    }

    companion object {
        @Volatile
        private lateinit var instance: MediaCache

        fun getInstance(context: Context): MediaCache {
            synchronized(this) {
                if (!Companion::instance.isInitialized) {
                    instance = MediaCache(context)
                }
                return instance
            }
        }
    }

    private fun getAvailableCacheSize(context: Context): Long {
        val cacheDir = context.externalCacheDir ?: context.cacheDir
        val statFs = StatFs(cacheDir.path)
        val blockSize = statFs.blockSizeLong
        val availableBlocks = statFs.availableBlocksLong
        return availableBlocks * blockSize
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun setupExoPlayerCache(context: Context): CacheDataSource.Factory {
        val cacheEvict = LeastRecentlyUsedCacheEvictor(maxCacheSize)
        val databaseProvider = StandaloneDatabaseProvider(context)
        val cache = SimpleCache(
            File(context.cacheDir, cacheDirectoryName.name),
            cacheEvict, databaseProvider
        )
        val upstreamFactory = DefaultDataSource.Factory(context)
        return CacheDataSource.Factory().apply {
            setCache(cache)
            setUpstreamDataSourceFactory(upstreamFactory)
            setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        }
    }
}