package com.example.jejaksehat

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(val email: String, val password: String)
data class LoginUser(val id: Int, val nama: String, val email: String, val role: String)
data class ProfileResponse(val id: Int, val nama: String, val email: String, val role: String)
data class LoginResponse(val access_token: String, val refresh_token: String, val user: LoginUser)
data class RefreshRequest(val refresh_token: String)
data class RefreshResponse(val access_token: String)
data class EmptyResponse(val message: String? = null)
data class RegisterRequest(val nama: String, val email: String, val password: String)
data class RecordRequest(val anggota_id: Int, val kategori: String, val nama: String, val status: String = "AKTIF")
data class VisitRequest(val anggota_id: Int, val faskes_id: Int, val tanggal_kunjungan: String, val keluhan: String? = null, val diagnosis: String? = null)
data class DocumentRequest(val kunjungan_id: Int, val judul: String, val jenis: String, val nama_berkas: String, val mime_type: String, val ukuran_kb: Int)

interface JejakSehatApi {
    @POST("auth/login") suspend fun login(@Body body: LoginRequest): LoginResponse
    @POST("auth/register") suspend fun register(@Body body: RegisterRequest): LoginUser
    @POST("auth/refresh") suspend fun refresh(@Body body: RefreshRequest): RefreshResponse
    @POST("auth/logout") suspend fun logout(): EmptyResponse
    @retrofit2.http.GET("auth/me") suspend fun profile(): ProfileResponse
    @retrofit2.http.GET("anggota") suspend fun members(@retrofit2.http.Query("q") query: String = ""): List<FamilyMember>
    @POST("anggota") suspend fun addMember(@Body body: Map<String, @JvmSuppressWildcards Any?>): FamilyMember
    @retrofit2.http.GET("riwayat") suspend fun records(@retrofit2.http.Query("q") query: String = ""): List<HealthRecord>
    @POST("riwayat") suspend fun addRecord(@Body body: RecordRequest): HealthRecord
    @retrofit2.http.PUT("riwayat/{id}") suspend fun updateRecord(@retrofit2.http.Path("id") id: Int, @Body body: RecordRequest)
    @retrofit2.http.DELETE("riwayat/{id}") suspend fun deleteRecord(@retrofit2.http.Path("id") id: Int)
    @retrofit2.http.PUT("anggota/{id}") suspend fun updateMember(@retrofit2.http.Path("id") id: Int, @Body body: Map<String, @JvmSuppressWildcards Any?>)
    @retrofit2.http.DELETE("anggota/{id}") suspend fun deleteMemberApi(@retrofit2.http.Path("id") id: Int)
    @retrofit2.http.GET("kunjungan") suspend fun visits(@retrofit2.http.Query("q") query: String = ""): List<Visit>
    @POST("kunjungan") suspend fun addVisit(@Body body: VisitRequest): Visit
    @retrofit2.http.PUT("kunjungan/{id}") suspend fun updateVisit(@retrofit2.http.Path("id") id: Int, @Body body: VisitRequest)
    @retrofit2.http.DELETE("kunjungan/{id}") suspend fun deleteVisit(@retrofit2.http.Path("id") id: Int)
    @retrofit2.http.GET("dokumen") suspend fun documents(@retrofit2.http.Query("q") query: String = ""): List<MedicalDocument>
    @POST("dokumen") suspend fun addDocument(@Body body: DocumentRequest): MedicalDocument
    @retrofit2.http.PUT("dokumen/{id}") suspend fun updateDocument(@retrofit2.http.Path("id") id: Int, @Body body: Map<String, @JvmSuppressWildcards Any?>)
    @retrofit2.http.DELETE("dokumen/{id}") suspend fun deleteDocument(@retrofit2.http.Path("id") id: Int)
    @retrofit2.http.GET("faskes") suspend fun facilities(@retrofit2.http.Query("q") query: String = ""): List<Facility>
    @POST("faskes") suspend fun addFacility(@Body body: Map<String, @JvmSuppressWildcards Any?>): Facility
    @retrofit2.http.PUT("faskes/{id}") suspend fun updateFacility(@retrofit2.http.Path("id") id: Int, @Body body: Map<String, @JvmSuppressWildcards Any?>)
    @retrofit2.http.DELETE("faskes/{id}") suspend fun deleteFacility(@retrofit2.http.Path("id") id: Int)
}

private val Context.dataStore by preferencesDataStore("jejaksehat_session")
class SessionStore(private val context: Context) {
    private val accessKey = stringPreferencesKey("access_token")
    private val refreshKey = stringPreferencesKey("refresh_token")
    private val phoneKey = stringPreferencesKey("profile_phone")
    private val birthDateKey = stringPreferencesKey("profile_birth_date")
    private val addressKey = stringPreferencesKey("profile_address")
    private val avatarKey = stringPreferencesKey("profile_avatar")
    fun refreshToken(): String? = runBlocking { context.dataStore.data.first()[refreshKey] }
    fun profileExtras(): Triple<String, String, String> = runBlocking { val p = context.dataStore.data.first(); Triple(p[phoneKey].orEmpty(), p[birthDateKey].orEmpty(), p[addressKey].orEmpty()) }
    suspend fun saveProfileExtras(phone: String, birthDate: String, address: String) = context.dataStore.edit { it[phoneKey] = phone; it[birthDateKey] = birthDate; it[addressKey] = address }
    fun avatar(): String = runBlocking { context.dataStore.data.first()[avatarKey].orEmpty() }
    suspend fun saveAvatar(avatar: String) = context.dataStore.edit { it[avatarKey] = avatar }
    suspend fun save(access: String, refresh: String) = context.dataStore.edit { it[accessKey] = access; it[refreshKey] = refresh }
    suspend fun clear() = context.dataStore.edit { it.clear() }
    fun accessToken(): String? = runBlocking { context.dataStore.data.first()[accessKey] }
}

private fun responseCount(response: okhttp3.Response): Int { var count = 1; var prior = response.priorResponse; while (prior != null) { count++; prior = prior.priorResponse }; return count }

object ApiClient {
    // Port emulator diteruskan ke backend komputer dengan adb reverse.
    private const val BASE_URL = "http://127.0.0.1:3000/api/"
    fun create(store: SessionStore): JejakSehatApi {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val refreshApi = Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build().create(JejakSehatApi::class.java)
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request().newBuilder().apply { store.accessToken()?.let { header("Authorization", "Bearer $it") } }.build()
            chain.proceed(request)
        }.authenticator { _, response ->
            if (responseCount(response) >= 2) return@authenticator null
            val refresh = store.refreshToken() ?: return@authenticator null
            try {
                val token = runBlocking { refreshApi.refresh(RefreshRequest(refresh)).access_token }
                runBlocking { store.save(token, refresh) }
                response.request.newBuilder().header("Authorization", "Bearer $token").build()
            } catch (_: Exception) { runBlocking { store.clear() }; null }
        }.addInterceptor(logging).build()
        return Retrofit.Builder().baseUrl(BASE_URL).client(client).addConverterFactory(GsonConverterFactory.create()).build().create(JejakSehatApi::class.java)
    }
}
