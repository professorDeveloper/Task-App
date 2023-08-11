package com.azamovhudstc.taskapp.cache

import android.annotation.SuppressLint
import android.content.Context
import com.azamovhudstc.taskapp.utils.okHttpClient
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import java.io.InputStream


@GlideModule
class CacheGlideApp : AppGlideModule(){
    @SuppressLint("CheckResult")
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        val diskCacheSizeBytes = 1024 * 1024 * 100 // 100 MiB
        builder.apply {
            setDiskCache(InternalCacheDiskCacheFactory(context, "img", diskCacheSizeBytes.toLong()))
        }
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(okHttpClient)
        )
        super.registerComponents(context, glide, registry)
    }
}