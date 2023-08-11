package com.azamovhudstc.taskapp.cache

import android.content.Context
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

object ExoPlayerCache {
    private var cache: SimpleCache? = null

    @Synchronized
    fun getInstance(context: Context): SimpleCache? {
        val cacheEvictor = LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024)
        val databaseProvider: DatabaseProvider = StandaloneDatabaseProvider(context)
        if (cache == null) {
            cache = SimpleCache(
                File(
                    context.cacheDir,
                    "exoplayer_cache"
                ),
                cacheEvictor,
                databaseProvider
            )
        }
        return cache
    }
}