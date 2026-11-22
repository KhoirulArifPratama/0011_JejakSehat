package com.example.jejaksehat

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.jejaksehat.ui.theme.JejakSehatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { JejakSehatTheme(dynamicColor = false) { JejakSehatApp() } }
    }

    fun copyMedicalFile(uri: Uri): String? {
        val type = contentResolver.getType(uri) ?: return null
        if (type !in listOf("image/jpeg", "image/png")) return null
        val size = contentResolver.openAssetFileDescriptor(uri, "r")?.length ?: return null
        if (size > 5 * 1024 * 1024) return null
        val file = File(filesDir, "dokumen_${System.currentTimeMillis()}.${if (type == "image/png") "png" else "jpg"}")
        contentResolver.openInputStream(uri)?.use { input -> FileOutputStream(file).use { output -> input.copyTo(output) } }
        return file.name
    }
}

@Composable
private fun JejakSehatApp(viewModel: JejakSehatViewModel = viewModel()) {
    val nav = rememberNavController()
    val state by viewModel.state.collectAsState()
    val loggedIn = state.loggedIn
    val role = state.role
    var showMemberForm by remember { mutableStateOf(false) }
    var showFacilityForm by remember { mutableStateOf(false) }
    var showRecordForm by remember { mutableStateOf(false) }
    var showVisitForm by remember { mutableStateOf(false) }
    var selectedDocument by remember { mutableStateOf<String?>(null) }
    var showProfileForm by remember { mutableStateOf(false) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    if (!loggedIn) { AuthScreen(onSubmit = { name, email, password, confirmation, isRegister, result -> if (isRegister) viewModel.register(name, email, password, confirmation, result) else viewModel.login(email, password, result) }); return }
    val currentRoute = nav.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(topBar = { AppBar(role) { showLogoutConfirmation = true } }, bottomBar = {
        NavigationBar {
            NavigationBarItem(currentRoute == "home", { nav.navigate("home") }, { Icon(if (role == "ADMIN") Icons.Default.AdminPanelSettings else Icons.Default.Home, null) }, label = { Text(if (role == "ADMIN") "Dashboard" else "Beranda") })
            if (role != "ADMIN") {
                NavigationBarItem(currentRoute == "members", { nav.navigate("members") }, { Icon(Icons.Default.Person, null) }, label = { Text("Keluarga") })
                NavigationBarItem(currentRoute == "visits", { nav.navigate("visits") }, { Icon(Icons.Default.LocalHospital, null) }, label = { Text("Kunjungan") })
                NavigationBarItem(currentRoute == "records", { nav.navigate("records") }, { Icon(Icons.Default.Description, null) }, label = { Text("Riwayat") })
            }
            NavigationBarItem(currentRoute == "facilities", { nav.navigate("facilities") }, { Icon(Icons.Default.LocalHospital, null) }, label = { Text(if (role == "ADMIN") "Kelola Faskes" else "Faskes") })
            NavigationBarItem(currentRoute == "profile", { nav.navigate("profile") }, { Icon(if (role == "ADMIN") Icons.Default.AdminPanelSettings else Icons.Default.Person, null) }, label = { Text(if (role == "ADMIN") "Admin" else "Profil") })
        }
    }) { padding ->
        NavHost(nav, "home", Modifier.padding(padding)) {
            composable("home") { if (role == "ADMIN") AdminDashboard(state.profileName, state.facilities.size) else HomeScreen(
                state.profileName,
                state.members.size,
                state.records.size,
                state.visits.size,
                state.documents.size,
                state.facilities.size,
                openMembers = { nav.navigate("members") },
                openRecords = { nav.navigate("records") },
                openVisits = { nav.navigate("visits") },
                openFacilities = { nav.navigate("facilities") },
                openProfile = { nav.navigate("profile") },
                openDocuments = { nav.navigate("visits") }
            ) }
            composable("members") { MemberScreen(state.members, { showMemberForm = true }, viewModel::deleteMember) }
            composable("facilities") { FacilityScreen(state.facilities, role == "ADMIN", { showFacilityForm = true }) }
            composable("records") { RecordScreen(state.records, { showRecordForm = true }, viewModel::deleteRecord) }
            composable("visits") { VisitScreen(state.visits, state.documents, viewModel, { showVisitForm = true }, viewModel::deleteVisit) }
            composable("profile") { ProfileScreen(state.profileName, state.profileEmail, role, state.avatar, state.phone, state.birthDate, state.address) { showAvatarPicker = true } }
        }
    }
    if (showMemberForm) MemberForm({ name, relation, blood -> viewModel.addMember(name, relation, blood); showMemberForm = false }, { showMemberForm = false })
    if (showFacilityForm) FacilityForm({ name, type, city -> viewModel.addFacility(name, type, city); showFacilityForm = false }, { showFacilityForm = false })
    if (showRecordForm) FormDialog("Tambah Riwayat", listOf("Anggota", "Kategori", "Nama penyakit/alergen"), { member, category, name -> viewModel.addRecord(member, category, name); showRecordForm = false }, { showRecordForm = false })
    if (showVisitForm) FormDialog("Tambah Kunjungan", listOf("Anggota", "Faskes", "Diagnosis/keluhan"), { member, facility, diagnosis -> viewModel.addVisit(member, facility, diagnosis); showVisitForm = false }, { showVisitForm = false })
    if (showProfileForm) ProfileForm(state.phone, state.birthDate, state.address, { phone, birthDate, address -> viewModel.updateProfile(phone, birthDate, address); showProfileForm = false }, { showProfileForm = false })
    if (showAvatarPicker) AvatarPicker(state.avatar, { avatar -> viewModel.updateAvatar(avatar); showAvatarPicker = false }, { showAvatarPicker = false })
    if (showLogoutConfirmation) AlertDialog(onDismissRequest = { showLogoutConfirmation = false }, title = { Text("Keluar dari aplikasi?") }, text = { Text("Sesi login akan diakhiri. Data yang sudah tersimpan tetap aman." ) }, confirmButton = { TextButton({ showLogoutConfirmation = false; viewModel.logout() }) { Text("Keluar") } }, dismissButton = { TextButton({ showLogoutConfirmation = false }) { Text("Batal") } })
}

@Composable private fun AdminDashboard(name: String, facilityCount: Int) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Dashboard Admin", style = MaterialTheme.typography.headlineMedium)
        Text("Halo, $name", style = MaterialTheme.typography.titleMedium)
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) { Column(Modifier.padding(20.dp)) { Text("Panel Pengelolaan", style = MaterialTheme.typography.titleLarge); Text("Kelola fasilitas kesehatan dan pantau data sistem.", modifier = Modifier.padding(top = 8.dp)); Text("$facilityCount fasilitas terdaftar", modifier = Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.primary) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun AppBar(role: String, logout: () -> Unit) { TopAppBar(title = { Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Image(painterResource(com.example.jejaksehat.R.drawable.jejaksehat_app_logo), "Logo JejakSehat", Modifier.height(34.dp).width(48.dp), contentScale = ContentScale.Fit); Text("JejakSehat") } }, actions = { Text(role, style = MaterialTheme.typography.labelSmall); IconButton(logout) { Icon(Icons.Default.Logout, "Keluar") } }) }

@Composable private fun AuthScreen(onSubmit: (String, String, String, String, Boolean, (Boolean, String) -> Unit) -> Unit) {
    var register by remember { mutableStateOf(false) }
    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val green = androidx.compose.ui.graphics.Color(0xFF4D9A68)
    val softGreen = androidx.compose.ui.graphics.Color(0xFFEAF6EC)

    Box(Modifier.fillMaxSize().background(softGreen)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(18.dp))
            Image(
                painterResource(com.example.jejaksehat.R.drawable.jejaksehat_app_logo),
                "Logo JejakSehat",
                Modifier.height(150.dp).width(190.dp),
                contentScale = ContentScale.Fit
            )
            Text("JejakSehat", style = MaterialTheme.typography.headlineSmall, color = green)
            Spacer(Modifier.height(20.dp))
            Card(
                Modifier.widthIn(max = 460.dp).fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 5.dp)
            ) {
                Column(Modifier.padding(horizontal = 24.dp, vertical = 26.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(if (register) "Buat akun" else "Masuk", style = MaterialTheme.typography.headlineSmall, color = green)
                    if (register) OutlinedTextField(nama, { nama = it }, Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp), label = { Text("Nama lengkap") })
                    OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp), label = { Text("Email") }, leadingIcon = { Icon(Icons.Default.Email, null) })
                    OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp), label = { Text("Password") }, visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(), leadingIcon = { Icon(Icons.Default.Lock, null) }, trailingIcon = { IconButton({ passwordVisible = !passwordVisible }) { Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, "Tampilkan password") } })
                    if (register) OutlinedTextField(confirmation, { confirmation = it }, Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(14.dp), label = { Text("Konfirmasi password") }, visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(), leadingIcon = { Icon(Icons.Default.Lock, null) })
                    if (error.isNotEmpty()) Text(error, color = if (error.contains("berhasil")) green else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    Button({ onSubmit(nama, email, password, confirmation, register) { success, message -> error = message; if (success && register) { register = false; password = ""; confirmation = "" } } }, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), enabled = email.isNotBlank() && password.isNotBlank(), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = green)) { Icon(Icons.Default.Login, null); Spacer(Modifier.width(8.dp)); Text(if (register) "Daftar Sekarang" else "Masuk") }
                }
            }
            TextButton({ register = !register; error = "" }) { Text(if (register) "Sudah punya akun? Masuk" else "Belum punya akun? Daftar sekarang", color = green) }
            Text("Rekam medis keluarga", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable private fun ProfileScreen(name: String, email: String, role: String, avatar: String, phone: String, birthDate: String, address: String, edit: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Text("Profil", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary); TextButton(edit) { Text("Pilih avatar") } }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)) { Row(Modifier.padding(20.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Text(if (role == "ADMIN") "🛡️" else avatar, style = MaterialTheme.typography.displaySmall, modifier = Modifier.size(72.dp), color = androidx.compose.ui.graphics.Color.White); Spacer(Modifier.width(16.dp)); Column { Text(if (role == "ADMIN") "Administrator JejakSehat" else name, style = MaterialTheme.typography.titleLarge, color = androidx.compose.ui.graphics.Color.White); Text(email, color = androidx.compose.ui.graphics.Color.White.copy(alpha = .85f)); Text(if (role == "ADMIN") "Administrator" else "Pasien", color = androidx.compose.ui.graphics.Color.White.copy(alpha = .85f), style = MaterialTheme.typography.labelMedium) } } }
        Text("Informasi akun", style = MaterialTheme.typography.titleMedium)
        ProfileInfoCard("Nama", name, Icons.Default.Person)
        ProfileInfoCard("Email", email, Icons.Default.Email)
        ProfileInfoCard("Nomor telepon", phone.ifBlank { "Belum diisi" }, Icons.Default.Phone)
        ProfileInfoCard("Tanggal lahir", birthDate.ifBlank { "Belum diisi" }, Icons.Default.DateRange)
        ProfileInfoCard("Alamat", address.ifBlank { "Belum diisi" }, Icons.Default.Home)
    }
}

@Composable private fun ProfileForm(phone: String, birthDate: String, address: String, save: (String, String, String) -> Unit, cancel: () -> Unit) {
    var p by remember { mutableStateOf(phone) }; var b by remember { mutableStateOf(birthDate) }; var a by remember { mutableStateOf(address) }
    val validDate = b.isBlank() || Regex("\\d{4}-\\d{2}-\\d{2}").matches(b)
    AlertDialog(onDismissRequest = cancel, title = { Text("Lengkapi profil") }, text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) { OutlinedTextField(p, { p = it }, label = { Text("Nomor telepon") }, singleLine = true); OutlinedTextField(b, { b = it }, label = { Text("Tanggal lahir") }, singleLine = true, placeholder = { Text("YYYY-MM-DD") }, isError = !validDate); if (!validDate) Text("Gunakan format YYYY-MM-DD", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall); OutlinedTextField(a, { a = it }, label = { Text("Alamat") }, minLines = 2) } }, confirmButton = { Button({ save(p, b, a) }, enabled = validDate) { Text("Simpan") } }, dismissButton = { TextButton(cancel) { Text("Batal") } })
}
@Composable private fun ProfileInfoCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) { Card(Modifier.fillMaxWidth()) { Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary); Column { Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(value, style = MaterialTheme.typography.bodyLarge) } } } }

@Composable private fun HomeScreen(
    name: String,
    members: Int,
    records: Int,
    visits: Int,
    documents: Int,
    facilities: Int,
    openMembers: () -> Unit,
    openRecords: () -> Unit,
    openVisits: () -> Unit,
    openFacilities: () -> Unit,
    openProfile: () -> Unit,
    openDocuments: () -> Unit
) {
    val green = androidx.compose.ui.graphics.Color(0xFF4D9A68)
    val pale = androidx.compose.ui.graphics.Color(0xFFEAF6EC)
    Column(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color(0xFFF5FBF6)).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(4.dp))
        Text("Halo, $name 👋", style = MaterialTheme.typography.headlineMedium)
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = green)) { Row(Modifier.fillMaxWidth().padding(22.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text("Keluarga Sehat", color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.headlineSmall); Text("Semua rekam medis keluarga, tersusun rapi dalam satu tempat.", color = androidx.compose.ui.graphics.Color.White.copy(alpha = .9f), style = MaterialTheme.typography.bodyMedium); Text("Cari cepat • Simpan aman • Mudah dibagikan", color = androidx.compose.ui.graphics.Color.White.copy(alpha = .8f), style = MaterialTheme.typography.labelMedium) }; Icon(Icons.Default.Favorite, null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(56.dp)) } }
        Text("Akses cepat", style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { QuickCard("Anggota", Icons.Default.Person, Modifier.weight(1f), openMembers); QuickCard("Riwayat", Icons.Default.Description, Modifier.weight(1f), openRecords); QuickCard("Kunjungan", Icons.Default.LocalHospital, Modifier.weight(1f), openVisits) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { QuickCard("Dokumen", Icons.Default.Description, Modifier.weight(1f), openDocuments); QuickCard("Faskes", Icons.Default.LocalHospital, Modifier.weight(1f), openFacilities); QuickCard("Profil", Icons.Default.Person, Modifier.weight(1f), openProfile) }
        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = pale)) { Row(Modifier.padding(18.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Icon(Icons.Default.Favorite, null, tint = green, modifier = Modifier.size(30.dp)); Column(Modifier.padding(start = 12.dp)) { Text("Tips hari ini", style = MaterialTheme.typography.titleMedium, color = green); Text("Simpan hasil pemeriksaan agar mudah ditemukan.", modifier = Modifier.padding(top = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
        Text("Ringkasan", style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { SummaryCard("Anggota", members.toString(), Icons.Default.Person, Modifier.weight(1f)); SummaryCard("Riwayat", records.toString(), Icons.Default.Description, Modifier.weight(1f)) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { SummaryCard("Kunjungan", visits.toString(), Icons.Default.LocalHospital, Modifier.weight(1f)); SummaryCard("Dokumen", documents.toString(), Icons.Default.Description, Modifier.weight(1f)) }
    }
}
@Composable private fun SummaryCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) { Card(modifier, shape = RoundedCornerShape(20.dp), elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)) { Row(Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { androidx.compose.material3.Surface(shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(42.dp)) { Box(contentAlignment = androidx.compose.ui.Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) } }; Column { Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary); Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
}
@Composable private fun QuickCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit = {}) { Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(20.dp), elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(vertical = 16.dp, horizontal = 8.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) { androidx.compose.material3.Surface(shape = androidx.compose.foundation.shape.CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(42.dp)) { Box(contentAlignment = androidx.compose.ui.Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) } }; Text(title, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 9.dp), maxLines = 1) } } }
@Composable private fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) { SummaryCard(title, value, icon, Modifier.fillMaxWidth()) }

@Composable private fun RecordScreen(items: List<HealthRecord>, add: () -> Unit, delete: (Int) -> Unit) { var q by remember { mutableStateOf("") }; Column(Modifier.padding(16.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Riwayat Kesehatan", style = MaterialTheme.typography.headlineSmall); IconButton(add) { Icon(Icons.Default.Add, "Tambah") } }; OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), label = { Text("Cari penyakit atau alergen") }); LazyColumn { items(items.filter { it.name.contains(q, true) }) { r -> Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) { Row(Modifier.padding(16.dp)) { Column(Modifier.weight(1f)) { Text(r.name, style = MaterialTheme.typography.titleMedium); Text("${r.member} â€¢ ${r.category} â€¢ ${r.status}") }; ConfirmDeleteButton("Hapus riwayat ini?", "Catatan riwayat akan dihapus.", { delete(r.id) }) } } } } } }

@Composable private fun VisitScreen(items: List<Visit>, documents: List<MedicalDocument>, viewModel: JejakSehatViewModel, add: () -> Unit, delete: (Int) -> Unit) { var q by remember { mutableStateOf("") }; val context = LocalContext.current; var fileMessage by remember { mutableStateOf("") }; val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) { val name = (context as? MainActivity)?.copyMedicalFile(uri); if (name == null) fileMessage = "Berkas tidak ditemukan di perangkat ini atau format/ukuran tidak sesuai" else { val bytes = context.contentResolver.openAssetFileDescriptor(uri, "r")?.length ?: 0L; viewModel.addLocalDocument(name, context.contentResolver.getType(uri) ?: "image/jpeg", (bytes / 1024L).toInt()); fileMessage = "Berkas tersimpan: $name (${bytes / 1024L} KB)" } } }; Column(Modifier.padding(16.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Kunjungan", style = MaterialTheme.typography.headlineSmall); IconButton(add) { Icon(Icons.Default.Add, "Tambah") } }; OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), label = { Text("Cari diagnosis, anggota, atau faskes") }); Button({ picker.launch(arrayOf("image/jpeg", "image/png")) }, Modifier.fillMaxWidth()) { Text("Pilih dokumen medis") }; if (fileMessage.isNotEmpty()) Text(fileMessage, color = MaterialTheme.colorScheme.primary); LazyColumn { items(items.filter { it.diagnosis.contains(q, true) || it.member.contains(q, true) || it.facility.contains(q, true) }) { v -> Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) { Row(Modifier.padding(16.dp)) { Column(Modifier.weight(1f)) { Text(v.diagnosis, style = MaterialTheme.typography.titleMedium); Text("${v.member} â€¢ ${v.facility} â€¢ ${v.date}"); Text(if (documents.isEmpty()) "Belum ada dokumen medis" else "Dokumen medis: ${documents.joinToString { it.fileName }}") }; ConfirmDeleteButton("Hapus kunjungan ini?", "Dokumen medis terkait ikut terhapus.", { delete(v.id) }) } } } } } }

@Composable private fun MemberScreen(items: List<FamilyMember>, add: () -> Unit, delete: (Int) -> Unit) { var q by remember { mutableStateOf("") }; var showRecords by remember { mutableStateOf(false) }; Column(Modifier.fillMaxSize().padding(16.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text("Anggota Keluarga", style = MaterialTheme.typography.headlineSmall); Text("Kelola profil dan riwayat keluarga", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; IconButton(add) { Icon(Icons.Default.Add, "Tambah") } }; OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth().padding(top = 12.dp), label = { Text("Cari nama anggota") }, leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true); TextButton({ showRecords = !showRecords }, Modifier.padding(top = 4.dp)) { Text(if (showRecords) "Sembunyikan riwayat kesehatan" else "Lihat riwayat kesehatan") }; if (showRecords) RecordScreen(emptyList(), {}, {}) else LazyColumn { items(items.filter { it.name.contains(q, true) }) { member -> Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = RoundedCornerShape(18.dp)) { Row(Modifier.padding(16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 14.dp)); Column(Modifier.weight(1f)) { Text(member.name, style = MaterialTheme.typography.titleMedium); Text("${member.relation} â€¢ Golongan darah ${member.blood}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; ConfirmDeleteButton("Hapus anggota ini?", "Seluruh riwayat, kunjungan, dan dokumen anggota ikut terhapus.", { delete(member.id) }) } } } } }
}

@Composable private fun FacilityScreen(items: List<Facility>, admin: Boolean, add: () -> Unit) {
    var q by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Fasilitas Kesehatan", style = MaterialTheme.typography.headlineSmall); if (admin) IconButton(add) { Icon(Icons.Default.Add, "Tambah") } }
        OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth().padding(top = 12.dp), label = { Text("Cari nama atau kota") }, singleLine = true)
        LazyColumn { items(items.filter { it.name.contains(q, true) || it.city.contains(q, true) }) { f -> Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(16.dp)) { Text(f.name, style = MaterialTheme.typography.titleMedium); Text("${f.type} ? ${f.city}", color = MaterialTheme.colorScheme.onSurfaceVariant) } } } }
    }
}

@Composable private fun ConfirmDeleteButton(title: String, message: String, onConfirm: () -> Unit) {
    var open by remember { mutableStateOf(false) }
    IconButton({ open = true }) { Icon(Icons.Default.Delete, "Hapus") }
    if (open) AlertDialog(onDismissRequest = { open = false }, title = { Text(title) }, text = { Text(message) }, confirmButton = { Button({ open = false; onConfirm() }) { Text("Hapus") } }, dismissButton = { TextButton({ open = false }) { Text("Batal") } })
}

@Composable private fun MemberForm(save: (String, String, String) -> Unit, cancel: () -> Unit) {
    var name by remember { mutableStateOf("") }; var relation by remember { mutableStateOf("") }; var blood by remember { mutableStateOf("") }; var height by remember { mutableStateOf("") }; var weight by remember { mutableStateOf("") }
    val valid = Validation.name(name) && Validation.relation(relation) && (height.isBlank() || Validation.height(height)) && (weight.isBlank() || Validation.weight(weight))
    AlertDialog(onDismissRequest = cancel, title = { Text("Tambah Anggota") }, text = { Column { OutlinedTextField(name, { name = it }, label = { Text("Nama lengkap") }); OutlinedTextField(relation, { relation = it }, label = { Text("Hubungan") }); OutlinedTextField(blood, { blood = it }, label = { Text("Golongan darah") }); OutlinedTextField(height, { height = it }, label = { Text("Tinggi 30-250 cm") }); OutlinedTextField(weight, { weight = it }, label = { Text("Berat 1-300 kg") }); if (!valid) Text("Periksa input anggota", color = MaterialTheme.colorScheme.error) } }, confirmButton = { Button({ save(name, relation, blood) }, enabled = valid) { Text("Simpan") } }, dismissButton = { TextButton(cancel) { Text("Batal") } })
}

@Composable private fun FacilityForm(save: (String, String, String) -> Unit, cancel: () -> Unit) = FormDialog("Tambah Faskes", listOf("Nama", "Jenis", "Kota"), save, cancel)

@Composable private fun FormDialog(title: String, labels: List<String>, save: (String, String, String) -> Unit, cancel: () -> Unit) {
    var values by remember { mutableStateOf(List(3) { "" }) }
    AlertDialog(onDismissRequest = cancel, title = { Text(title) }, text = { Column { labels.forEachIndexed { i, label -> OutlinedTextField(values[i], { v -> values = values.toMutableList().also { it[i] = v } }, Modifier.fillMaxWidth(), label = { Text(label) }) } } }, confirmButton = { Button({ if (values.all { it.trim().isNotEmpty() }) save(values[0], values[1], values[2]) }) { Text("Simpan") } }, dismissButton = { TextButton(cancel) { Text("Batal") } })
}




