package com.azamovhudstc.taskapp.di

import android.content.Context
import com.azamovhudstc.taskapp.data.remote.api.LessonsApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @[Provides Singleton]
    fun getOkHTTPClient(@ApplicationContext context: Context): OkHttpClient = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(60, TimeUnit.SECONDS)
        .build()
    @[Provides Singleton ]
    fun getLessonsRetrofit2(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://iphonephotographyschool.com")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    @Provides
    fun getAuthApi( retrofit: Retrofit): LessonsApi =
        retrofit.create(LessonsApi::class.java)


}