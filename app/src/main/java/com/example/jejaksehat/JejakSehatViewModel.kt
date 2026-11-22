package com.example.jejaksehat

import android.app.Application
import com.google.gson.annotations.SerializedName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class FamilyMember(
    val id: Int,
    @SerializedName("nama_lengkap") val name: String,
    @SerializedName("hubungan") val relation: String,
    @SerializedName("golongan_darah") val blood: String = "TIDAK_TAHU"
)
data class Facility(
    val id: Int,
    val name: String,
    @SerializedName("jenis") val type: String,
    @SerializedName("kota") val city: String = ""
)
data class HealthRecord(
    val id: Int,
    @SerializedName("anggota_nama") val member: String = "Anggota",
    val kategori: String = "PENYAKIT",
    val name: String,
    val status: String = "AKTIF"
) { val category: String get() = kategori }
data class Visit(
    val id: Int,
    @SerializedName("anggota_nama") val member: String = "Anggota",
    @SerializedName("faskes_nama") val facility: String = "Faskes",
    val diagnosis: String = "",
    @SerializedName("tanggal_kunjungan") val date: String = ""
)
data class MedicalDocument(
    val id: Int,
    val title: String,
    val type: String = "LAINNYA",
    @SerializedName("nama_berkas") val fileName: String = ""
)

data class AppState(
    val loggedIn: Boolean = false,
    val profileName: String = "Pengguna JejakSehat",
    val profileEmail: String = "email pengguna",
    val avatar: String = "👩‍⚕️",
    val role: String = "PASIEN",
    val phone: String = "",
    val birthDate: String = "",
    val address: String = "",
    val members: List<FamilyMember> = listOf(FamilyMember(1, "Saya", "Diri sendiri", "O")),
    val facilities: List<Facility> = listOf(Facility(1, "RS Sehat Sentosa", "Rumah sakit", "Yogyakarta"), Facility(2, "Klinik Keluarga", "Klinik", "Sleman")),
    val records: List<HealthRecord> = listOf(HealthRecord(1, "Saya", "PENYAKIT", "Flu", "SEMBUH")),
    val visits: List<Visit> = listOf(Visit(1, "Saya", "RS Sehat Sentosa", "Pemeriksaan umum", "2026-11-01")),
    val documents: List<MedicalDocument> = listOf(MedicalDocument(1, "Hasil lab", "HASIL_LAB", "hasil-lab.jpg"))
)

class JejakSehatViewModel(application: Application) : AndroidViewModel(application) {
    private val session = SessionStore(application)
    private val api = ApiClient.create(session)
    private val demoUsers = mutableMapOf<String, String>()
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    init { restoreSession() }

    private fun restoreSession() {
        viewModelScope.launch {
            val sessionData = withContext(Dispatchers.IO) {
                Triple(session.accessToken(), session.profileExtras().first, session.profileExtras().second)
            }
            val accessToken = sessionData.first
            if (accessToken != null) {
                _state.value = _state.value.copy(
                    loggedIn = true,
                    avatar = withContext(Dispatchers.IO) { session.avatar().ifBlank { "👩‍⚕️" } },
                    phone = sessionData.second,
                    birthDate = sessionData.third,
                    address = withContext(Dispatchers.IO) { session.profileExtras().third }
                )
                loadProfile()
                loadRemoteData()
            }
        }
    }

    private fun loadProfile() { viewModelScope.launch { runCatching { api.profile() }.onSuccess { p -> _state.value = _state.value.copy(profileName = p.nama, profileEmail = p.email, role = p.role) } } }

    private fun loadRemoteData() {
        viewModelScope.launch {
            runCatching {
                val members = api.members()
                val facilities = api.facilities()
                val records = api.records()
                val visits = api.visits()
                val documents = api.documents()
                _state.value = _state.value.copy(members = members, facilities = facilities, records = records, visits = visits, documents = documents)
            }
        }
    }

    private fun apiErrorMessage(error: Exception): String = when (error) {
        is retrofit2.HttpException -> when (error.code()) { 401 -> "Email atau password salah"; 409 -> "Email sudah terdaftar"; 422 -> "Data yang dikirim tidak valid"; else -> "Server menolak permintaan (${error.code()})" }
        is java.net.ConnectException -> "Backend tidak bisa dijangkau dari emulator. Pastikan npm start tetap berjalan."
        is java.net.SocketTimeoutException -> "Koneksi ke backend timeout. Pastikan backend berjalan di port 3000."
        is java.net.UnknownHostException -> "Alamat backend tidak ditemukan. Periksa BASE_URL aplikasi."
        else -> "Gagal terhubung: ${error.javaClass.simpleName}"
    }

    fun register(nama: String, email: String, password: String, confirmation: String, onResult: (Boolean, String) -> Unit) {
        if (nama.trim().length < 3 || !Validation.email(email) || password.length < 8 || password != confirmation) { onResult(false, "Nama minimal 3 karakter, email valid, password minimal 8 karakter, dan konfirmasi harus sama"); return }
        viewModelScope.launch {
            try { api.register(RegisterRequest(nama.trim(), email.trim().lowercase(), password)); onResult(true, "Registrasi berhasil, silakan masuk") }
            catch (e: Exception) { onResult(false, apiErrorMessage(e)) }
        }
    }

    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        if (!Validation.email(email)) { onResult(false, "Format email tidak valid"); return }
        if (password.length < 8) { onResult(false, "Password minimal 8 karakter"); return }
        if (demoUsers[email.lowercase()] != null && demoUsers[email.lowercase()] != password) { onResult(false, "Password akun demo salah"); return }
        viewModelScope.launch {
            try {
                val result = api.login(LoginRequest(email, password))
                session.save(result.access_token, result.refresh_token)
                _state.value = _state.value.copy(loggedIn = true, profileName = result.user.nama, profileEmail = result.user.email, role = result.user.role)
                loadProfile()
                loadRemoteData()
                onResult(true, "")
            } catch (e: Exception) {
                // Tampilkan jenis error agar masalah koneksi/API mudah didiagnosis.
                onResult(false, apiErrorMessage(e))
            }
        }
    }

    fun updateProfile(phone: String, birthDate: String, address: String) { _state.value = _state.value.copy(phone = phone, birthDate = birthDate, address = address); viewModelScope.launch { session.saveProfileExtras(phone, birthDate, address) } }
    fun updateAvatar(avatar: String) { _state.value = _state.value.copy(avatar = avatar); viewModelScope.launch { session.saveAvatar(avatar) } }
    fun logout() { viewModelScope.launch { runCatching { api.logout() }; session.clear(); _state.value = AppState() } }
    fun addMember(name: String, relation: String, blood: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) { if (!Validation.name(name) || !Validation.relation(relation)) { onResult(false, "Nama minimal 2 karakter dan hubungan wajib diisi"); return }; viewModelScope.launch { runCatching { api.addMember(mapOf("nama_lengkap" to name, "hubungan" to relation, "golongan_darah" to blood)) }.onSuccess { loadRemoteData() }.onFailure { _state.value = _state.value.copy(members = _state.value.members + FamilyMember(nextId(_state.value.members.map { it.id }), name, relation, blood)) } } }
    fun deleteMember(id: Int) { viewModelScope.launch { runCatching { api.deleteMemberApi(id) }; _state.value = _state.value.copy(members = _state.value.members.filterNot { it.id == id }) } }
    fun addFacility(name: String, type: String, city: String) { viewModelScope.launch { runCatching { api.addFacility(mapOf("nama" to name, "jenis" to type, "kota" to city)) }.onSuccess { loadRemoteData() }.onFailure { _state.value = _state.value.copy(facilities = _state.value.facilities + Facility(nextId(_state.value.facilities.map { it.id }), name, type, city)) } } }
    fun addRecord(member: String, category: String, name: String) { viewModelScope.launch { val selected = _state.value.members.firstOrNull { it.name.equals(member, true) } ?: _state.value.members.firstOrNull { it.name.contains(member, true) }; runCatching { if (selected == null) error("Anggota tidak ditemukan") else api.addRecord(RecordRequest(selected.id, category.uppercase(), name)) }.onSuccess { loadRemoteData() }.onFailure { _state.value = _state.value.copy(records = _state.value.records + HealthRecord(nextId(_state.value.records.map { it.id }), member, category, name, "AKTIF")) } } }
    fun deleteRecord(id: Int) { viewModelScope.launch { runCatching { api.deleteRecord(id) }; _state.value = _state.value.copy(records = _state.value.records.filterNot { it.id == id }) } }
    fun addVisit(member: String, facility: String, diagnosis: String) { viewModelScope.launch { val selectedMember = _state.value.members.firstOrNull { it.name.equals(member, true) } ?: _state.value.members.firstOrNull { it.name.contains(member, true) }; val selectedFacility = _state.value.facilities.firstOrNull { it.name.equals(facility, true) } ?: _state.value.facilities.firstOrNull { it.name.contains(facility, true) }; runCatching { if (selectedMember == null || selectedFacility == null) error("Anggota atau faskes tidak ditemukan") else api.addVisit(VisitRequest(selectedMember.id, selectedFacility.id, "2026-11-09", diagnosis = diagnosis)) }.onSuccess { loadRemoteData() }.onFailure { _state.value = _state.value.copy(visits = _state.value.visits + Visit(nextId(_state.value.visits.map { it.id }), member, facility, diagnosis, "2026-11-09")) } } }
    fun addLocalDocument(fileName: String, mimeType: String, sizeKb: Int, title: String = "Dokumen medis") { val document = MedicalDocument(nextId(_state.value.documents.map { it.id }), title, if (mimeType == "image/png") "LAINNYA" else "HASIL_LAB", fileName); _state.value = _state.value.copy(documents = _state.value.documents + document); viewModelScope.launch { val visitId = _state.value.visits.firstOrNull()?.id ?: return@launch; runCatching { api.addDocument(DocumentRequest(visitId, title, document.type, fileName, mimeType, sizeKb)) } } }
    fun deleteVisit(id: Int) { viewModelScope.launch { runCatching { api.deleteVisit(id) }; _state.value = _state.value.copy(visits = _state.value.visits.filterNot { it.id == id }) } }
    private fun nextId(ids: List<Int>) = (ids.maxOrNull() ?: 0) + 1
}
