package com.example.emy.data.remote

import android.content.Context
import com.example.emy.BuildConfig
import com.example.emy.data.local.TokenManager
import com.google.gson.JsonElement
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

object ApiClient {
    @Volatile
    private var tokenManager: TokenManager? = null

    fun initialize(context: Context) {
        tokenManager = TokenManager(context.applicationContext)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = tokenManager?.let { manager ->
                    runBlocking { manager.getToken() }
                }
                val request = chain.request().newBuilder().apply {
                    if (!token.isNullOrBlank()) {
                        header("Authorization", "Bearer $token")
                    }
                }.build()
                chain.proceed(request)
            }
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BASIC
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                }
            )
            .build()
    }

    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

interface ApiService {
    @GET("categorias")
    suspend fun getPublicCategories(): Response<JsonElement>

    @GET("productos-publicos")
    suspend fun getPublicProducts(
        @Query("categoria") categoryId: Int? = null
    ): Response<JsonElement>

    @GET("{path}")
    suspend fun get(
        @Path(value = "path", encoded = true) path: String
    ): Response<JsonElement>

    @POST("{path}")
    suspend fun post(
        @Path(value = "path", encoded = true) path: String,
        @Body body: JsonElement
    ): Response<JsonElement>

    @PUT("{path}")
    suspend fun put(
        @Path(value = "path", encoded = true) path: String,
        @Body body: JsonElement
    ): Response<JsonElement>

    @PATCH("{path}")
    suspend fun patch(
        @Path(value = "path", encoded = true) path: String,
        @Body body: JsonElement
    ): Response<JsonElement>

    @DELETE("{path}")
    suspend fun delete(
        @Path(value = "path", encoded = true) path: String
    ): Response<JsonElement>
}
