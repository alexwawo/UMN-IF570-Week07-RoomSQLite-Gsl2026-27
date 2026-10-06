# IF570 Mobile Application Programming — Week 7: Room & SQLite

Repository demo untuk **Week 7 (Room & SQLite)**. Proyek: **NotesApp**, aplikasi catatan sederhana (tambah, hapus, pin catatan) yang dibangun bertahap dari Room dasar sampai Testing.

**Stack:** Kotlin · Jetpack Compose (Material 3) · Room · KSP · Hilt (opsional) · JUnit · `kotlinx-coroutines-test`

---

## Daftar Isi

1. [Tujuan Pembelajaran](#1-tujuan-pembelajaran)
2. [Prasyarat](#2-prasyarat)
3. [Cara Memakai Repository Ini](#3-cara-memakai-repository-ini)
4. [Peta Tutorial dan Tag](#4-peta-tutorial-dan-tag)
5. [Struktur Proyek Akhir](#5-struktur-proyek-akhir)
6. [Tutorial 0 — Setup Proyek](#tutorial-0--setup-proyek)
7. [Tutorial 1 — Room Dasar](#tutorial-1--room-dasar)
8. [Tutorial 2 — Flow](#tutorial-2--flow-dari-dao-ke-ui)
9. [Tutorial 3 — Repository](#tutorial-3--repository-pattern)
10. [Tutorial 4 — Dependency Injection](#tutorial-4--dependency-injection)
11. [Tutorial 5 — Migrasi Database](#tutorial-5--migrasi-database)
12. [Tutorial 6 — Testing](#tutorial-6--testing)
13. [Troubleshooting](#troubleshooting)
14. [Tugas Mandiri](#tugas-mandiri)

---

## 1. Tujuan Pembelajaran

Setelah menyelesaikan tutorial ini, Anda mampu:

- Membuat **Entity, DAO, dan Database** dengan Room.
- Mengalirkan data dari database ke UI secara reaktif dengan **Flow** dan `collectAsStateWithLifecycle`.
- Memisahkan layer dengan **Repository** dan **domain model** (arsitektur MVVM berlapis).
- Menerapkan **Dependency Injection** (manual dan Hilt).
- Mengelola perubahan skema dengan **AutoMigration** dan **Migration manual** tanpa kehilangan data pengguna.
- Menulis **DAO test, migration test, dan ViewModel test**.

> Relevan dengan **Sub-CLO 3**: membangun aplikasi dengan arsitektur MVVM yang mengintegrasikan data lokal, serta mampu menjustifikasi keputusan arsitektur dan manajemen data.

## 2. Prasyarat

- Android Studio versi stabil terbaru, emulator atau device API 26+.
- Dasar Kotlin dan Jetpack Compose (Week 1–4).
- Memahami ViewModel dan state (Week 4–6).
- Hilt hanya dibutuhkan untuk **Tutorial 4B** (opsional, ikuti instruksi dosen).

Package name proyek: `com.example.notesapp`.

## 3. Cara Memakai Repository Ini

Setiap tutorial = **satu tag Git** = **satu build yang bisa dijalankan** (kecuali `demo-5b-missing-migration` yang crash secara sengaja).

**Metode belajar yang disarankan (code-along):**

1. Clone repository ini.
2. Mulai dari tag **sebelum** tutorial yang Anda kerjakan, di branch milik Anda sendiri.
3. Ikuti langkah di README ini dan ketik sendiri kodenya.
4. Bandingkan hasil Anda dengan tag tutorial tersebut.

```bash
git clone https://github.com/<username>/IF570-week7-notesapp.git
cd IF570-week7-notesapp

# Contoh: mengerjakan Tutorial 3 dimulai dari akhir Tutorial 2
git switch -c my/demo-3 demo-2-flow

# Hanya ingin melihat hasil akhir sebuah tutorial (read-only)
git switch --detach demo-3-repository

# Membandingkan hasil Anda dengan tag
git diff demo-3-repository --stat
```

Perbandingan antar tag di browser:
`https://github.com/<username>/IF570-week7-notesapp/compare/demo-2-flow...demo-3-repository`

### Aturan penting saat berpindah tag

1. Setelah pindah tag, klik **Sync Project with Gradle Files**. Jika build aneh, jalankan **Build → Clean Project**.
2. **Versi database tidak bisa turun.** Jika pindah ke tag dengan versi DB lebih rendah di emulator yang sama, aplikasi crash. Solusi: uninstall aplikasi.
   ```bash
   adb uninstall com.example.notesapp
   ```
3. **Pengecualian: Tutorial 5.** Jangan uninstall. Data lama harus tetap ada untuk membuktikan migrasi berhasil.
4. Folder `schemas/` **harus ikut di-commit** (dipakai Tutorial 5 dan 6).

## 4. Peta Tutorial dan Tag

| Tutorial | Tag | Konsep |
|---|---|---|
| 0 | [`demo-0-setup`](../../tree/demo-0-setup) | Proyek kosong + dependency |
| 1 | [`demo-1-room-basic`](../../tree/demo-1-room-basic) | Entity, DAO, Database; ViewModel memanggil DAO langsung (coupling) |
| 2 | [`demo-2-flow`](../../tree/demo-2-flow) | `Flow`, `suspend`, `collectAsStateWithLifecycle` |
| 3 | [`demo-3-repository`](../../tree/demo-3-repository) | Repository + domain model; ViewModel tidak tahu Room |
| 4A | [`demo-4a-manual-di`](../../tree/demo-4a-manual-di) | Manual DI (`AppContainer` + ViewModel Factory) |
| 4B | [`demo-4b-hilt`](../../tree/demo-4b-hilt) | Hilt (opsional) |
| 5A | [`demo-5a-automigration`](../../tree/demo-5a-automigration) | `AutoMigration` v1 → v2 (kolom `isPinned`) |
| 5B | [`demo-5b-missing-migration`](../../tree/demo-5b-missing-migration) | Skema berubah tanpa migrasi → crash (sengaja) |
| 5C | [`demo-5c-manual-migration`](../../tree/demo-5c-manual-migration) | `Migration` manual v2 → v3 (backfill `updatedAt`) |
| 6A | [`demo-6a-dao-test`](../../tree/demo-6a-dao-test) | DAO test (instrumented) |
| 6B | [`demo-6b-migration-test`](../../tree/demo-6b-migration-test) | Migration test |
| 6C | [`demo-6c-viewmodel-test`](../../tree/demo-6c-viewmodel-test) | ViewModel test dengan fake repository |

> Jika dosen tidak membahas Hilt, **lewati Tutorial 4B**. Lanjutkan Tutorial 5 dan 6 dari `demo-4a-manual-di`.

## 5. Struktur Proyek Akhir

```
app/src/main/java/com/example/notesapp/
├── MainActivity.kt
├── NotesApplication.kt              (Tutorial 4A/4B)
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── Migrations.kt            (Tutorial 5C)
│   │   ├── NoteDao.kt
│   │   ├── NoteEntity.kt
│   │   └── NoteMapper.kt            (Tutorial 3)
│   └── repository/
│       ├── NoteRepository.kt        (Tutorial 3)
│       └── NoteRepositoryImpl.kt    (Tutorial 3)
├── di/
│   ├── AppContainer.kt              (Tutorial 4A, dihapus di 4B)
│   ├── AppViewModelProvider.kt      (Tutorial 4A, dihapus di 4B)
│   ├── DatabaseModule.kt            (Tutorial 4B)
│   └── RepositoryModule.kt          (Tutorial 4B)
├── model/
│   └── Note.kt                      (Tutorial 3)
└── ui/
    ├── NotesScreen.kt
    └── NotesViewModel.kt
app/src/androidTest/java/com/example/notesapp/
├── NoteDaoTest.kt                   (Tutorial 6A)
└── MigrationTest.kt                 (Tutorial 6B)
app/src/test/java/com/example/notesapp/
├── FakeNoteRepository.kt            (Tutorial 6C)
├── MainDispatcherRule.kt            (Tutorial 6C)
└── NotesViewModelTest.kt            (Tutorial 6C)
```

---

## Tutorial 0 — Setup Proyek

**Tag hasil:** `demo-0-setup`

### Langkah

1. Android Studio → **New Project → Empty Activity** (template Compose).
2. Name: `NotesApp`, Package: `com.example.notesapp`, Minimum SDK: API 26+, Build configuration: **Kotlin DSL**.
3. Tambahkan dependency ke `gradle/libs.versions.toml`.

> Nilai versi di bawah adalah **contoh**. Gunakan versi stabil terbaru dan pastikan **versi KSP cocok dengan versi Kotlin**.

```toml
[versions]
kotlin = "2.2.20"
ksp = "2.2.20-2.0.4"          # sesuaikan dengan versi Kotlin
room = "2.8.1"
lifecycle = "2.9.4"
hilt = "2.57.1"               # dipakai di Tutorial 4B
hiltNavigationCompose = "1.3.0"
coroutines = "1.10.2"

[libraries]
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }

[plugins]
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
androidx-room = { id = "androidx.room", version.ref = "room" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

Entri bawaan template (`androidx-core-ktx`, Compose BOM, dst.) tetap dipertahankan.

4. Root `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false
}
```

5. `app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

room {
    schemaDirectory("$projectDir/schemas")   // JSON skema masuk Git, dipakai Tutorial 5 & 6
}

dependencies {
    // ... bawaan template ...
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
}
```

6. **Pastikan `.gitignore` tidak berisi `schemas/`.**

### Cek hasil
Proyek berhasil di-sync dan di-build tanpa error. Bandingkan dengan tag `demo-0-setup`.

---

## Tutorial 1 — Room Dasar

**Mulai dari:** `demo-0-setup` · **Tag hasil:** `demo-1-room-basic`

**Konsep:** Entity, DAO, Database. **Masalah yang akan Anda temukan:** ViewModel terikat langsung ke Room (coupling) dan UI harus di-refresh manual.

> **Skema v1 dikunci:** tabel `notes`, kolom `id, title, content, createdAt`. **Jangan ubah** setelah tutorial ini.

### Langkah 1 — Entity (`data/local/NoteEntity.kt`)

```kotlin
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)
```

### Langkah 2 — DAO (`data/local/NoteDao.kt`)

```kotlin
@Dao
interface NoteDao {
    @Insert
    suspend fun insert(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    suspend fun getAll(): List<NoteEntity>
}
```

### Langkah 3 — Database (`data/local/AppDatabase.kt`)

```kotlin
@Database(entities = [NoteEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notes.db"
                ).build().also { INSTANCE = it }
            }
    }
}
```

Jalankan **Build → Make Project**. Pastikan file `app/schemas/com.example.notesapp.data.local.AppDatabase/1.json` muncul, lalu commit file tersebut.

### Langkah 4 — ViewModel (`ui/NotesViewModel.kt`)

ViewModel membuat database sendiri dan memanggil DAO langsung:

```kotlin
class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).noteDao()

    var notes by mutableStateOf<List<NoteEntity>>(emptyList())
        private set

    init { loadNotes() }

    private fun loadNotes() {
        viewModelScope.launch { notes = dao.getAll() }
    }

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            dao.insert(NoteEntity(title = title, content = content))
            loadNotes()      // refresh MANUAL
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            dao.delete(note)
            loadNotes()      // refresh MANUAL lagi
        }
    }
}
```

> Fungsi `suspend` pada DAO Room otomatis *main-safe*, jadi aman dipanggil dari `viewModelScope`.

### Langkah 5 — UI (`ui/NotesScreen.kt`)

UI dipisah menjadi dua composable:

- `NotesScreen` — **stateful**, mengambil ViewModel.
- `NotesContent` — **stateless**, hanya menerima data dan callback.

```kotlin
@Composable
fun NotesScreen(viewModel: NotesViewModel = viewModel()) {
    NotesContent(
        notes = viewModel.notes,
        onAdd = viewModel::addNote,
        onDelete = viewModel::deleteNote
    )
}
```

`NotesContent` berisi `Scaffold`, dua `OutlinedTextField` (title, content), tombol **Add**, dan `LazyColumn` dengan `ListItem` + tombol **Delete**. Kode lengkap ada di tag `demo-1-room-basic` (`ui/NotesScreen.kt`).

### Langkah 6 — `MainActivity`

```kotlin
setContent {
    NotesAppTheme {           // sesuaikan dengan nama theme dari template
        NotesScreen()
    }
}
```

### Cek hasil

1. Run aplikasi, tambah beberapa catatan, tutup lalu buka lagi: **data tetap ada**.
2. Buka **App Inspection → Database Inspector** dan lihat tabel `notes`.
3. Jawab pertanyaan berikut:
    - Mengapa `NotesViewModel` sulit di-test?
    - Apa yang terjadi pada UI jika data berubah dari tempat lain (bukan dari ViewModel ini)?
    - Mengapa UI memakai `NoteEntity` (kelas Room) langsung dianggap masalah?

---

## Tutorial 2 — Flow dari DAO ke UI

**Mulai dari:** `demo-1-room-basic` · **Tag hasil:** `demo-2-flow`

**Konsep:** `Flow`, `suspend` vs non-suspend, `StateFlow`, `collectAsStateWithLifecycle`.

### Langkah 1 — DAO mengembalikan Flow

Ganti `getAll()` menjadi `observeAll()`:

```kotlin
// BUKAN suspend: Flow dari Room bersifat reaktif
@Query("SELECT * FROM notes ORDER BY createdAt DESC")
fun observeAll(): Flow<List<NoteEntity>>
```

### Langkah 2 — ViewModel memakai `StateFlow`

```kotlin
val notes: StateFlow<List<NoteEntity>> = dao.observeAll()
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

fun addNote(title: String, content: String) {
    viewModelScope.launch { dao.insert(NoteEntity(title = title, content = content)) }
}

fun deleteNote(note: NoteEntity) {
    viewModelScope.launch { dao.delete(note) }
}
```

Hapus `loadNotes()` dan import `mutableStateOf`, `getValue`, `setValue`.

### Langkah 3 — UI

```kotlin
@Composable
fun NotesScreen(viewModel: NotesViewModel = viewModel()) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    NotesContent(
        notes = notes,
        onAdd = viewModel::addNote,
        onDelete = viewModel::deleteNote
    )
}
```

### Cek hasil

1. Tambah/hapus catatan: UI update otomatis tanpa refresh manual.
2. Di Database Inspector, ubah satu baris langsung: UI di emulator ikut berubah.
3. Bandingkan: `git diff demo-1-room-basic demo-2-flow`.

### Pertanyaan refleksi

- Apa beda `suspend fun` dan `Flow` pada DAO?
- Apa fungsi `WhileSubscribed(5_000)`?
- Apa beda `collectAsState` dan `collectAsStateWithLifecycle`?

---

## Tutorial 3 — Repository Pattern

**Mulai dari:** `demo-2-flow` · **Tag hasil:** `demo-3-repository`

**Konsep:** pemisahan layer. ViewModel dan UI **tidak boleh tahu Room**.

### Langkah 1 — Domain model (`model/Note.kt`)

```kotlin
data class Note(
    val id: Int,
    val title: String,
    val content: String,
    val createdAt: Long
)
```

### Langkah 2 — Mapper (`data/local/NoteMapper.kt`)

```kotlin
fun NoteEntity.toDomain() = Note(id = id, title = title, content = content, createdAt = createdAt)

fun Note.toEntity() = NoteEntity(id = id, title = title, content = content, createdAt = createdAt)
```

### Langkah 3 — Interface Repository (`data/repository/NoteRepository.kt`)

```kotlin
interface NoteRepository {
    fun observeNotes(): Flow<List<Note>>
    suspend fun addNote(title: String, content: String)
    suspend fun deleteNote(note: Note)
}
```

### Langkah 4 — Implementasi (`data/repository/NoteRepositoryImpl.kt`)

```kotlin
class NoteRepositoryImpl(
    private val dao: NoteDao
) : NoteRepository {

    override fun observeNotes(): Flow<List<Note>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun addNote(title: String, content: String) {
        dao.insert(NoteEntity(title = title, content = content))
    }

    override suspend fun deleteNote(note: Note) {
        dao.delete(note.toEntity())
    }
}
```

### Langkah 5 — ViewModel memakai Repository

```kotlin
class NotesViewModel(application: Application) : AndroidViewModel(application) {

    // Masih membuat dependency sendiri (belum DI), tetapi logika hanya bicara ke interface
    private val repository: NoteRepository =
        NoteRepositoryImpl(AppDatabase.getInstance(application).noteDao())

    val notes: StateFlow<List<Note>> = repository.observeNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addNote(title: String, content: String) {
        viewModelScope.launch { repository.addNote(title, content) }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch { repository.deleteNote(note) }
    }
}
```

### Langkah 6 — UI

Di `NotesScreen.kt`, ganti import dan semua `NoteEntity` menjadi `Note` (`notes: List<Note>`, `onDelete: (Note) -> Unit`).

### Cek hasil

1. `NotesViewModel` dan `NotesContent` **tidak memiliki** import `androidx.room.*` maupun `NoteEntity`.
2. Jawab: *jika besok sumber data diganti API/Supabase, bagian mana yang berubah?*
3. Sisa masalah: ViewModel masih **membuat** `NoteRepositoryImpl` dan `AppDatabase` sendiri, sehingga tidak bisa diganti dengan fake saat test. Ini dijawab di Tutorial 4.

---

## Tutorial 4 — Dependency Injection

**Konsep:** dependency dibuat **di luar** ViewModel.

### Tutorial 4A — Manual DI

**Mulai dari:** `demo-3-repository` · **Tag hasil:** `demo-4a-manual-di`

**1. `di/AppContainer.kt`**

```kotlin
interface AppContainer {
    val noteRepository: NoteRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: AppDatabase by lazy { AppDatabase.getInstance(context) }

    override val noteRepository: NoteRepository by lazy {
        NoteRepositoryImpl(database.noteDao())
    }
}
```

**2. `NotesApplication.kt`**

```kotlin
class NotesApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
```

Daftarkan di `AndroidManifest.xml`:

```xml
<application
    android:name=".NotesApplication"
    ... >
```

**3. `di/AppViewModelProvider.kt`**

```kotlin
object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            NotesViewModel(notesApplication().container.noteRepository)
        }
    }
}

fun CreationExtras.notesApplication(): NotesApplication =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as NotesApplication
```

**4. ViewModel menerima Repository lewat constructor**

```kotlin
class NotesViewModel(
    private val repository: NoteRepository
) : ViewModel() {
    // isi sama seperti Tutorial 3, tanpa AppDatabase dan Application
}
```

**5. UI**

```kotlin
@Composable
fun NotesScreen(
    viewModel: NotesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) { /* isi tidak berubah */ }
```

**Cek hasil:** `NotesViewModel` tidak lagi mengimpor `AppDatabase` dan tidak butuh `Application`. Alur objek: `Application` → `AppContainer` → `Factory` → `ViewModel`.

**Refleksi:** apa yang harus ditambahkan setiap kali ada ViewModel baru? (Petunjuk: lihat `initializer` pada Factory.)

---

### Tutorial 4B — Hilt (Opsional)

**Mulai dari:** `demo-4a-manual-di` · **Tag hasil:** `demo-4b-hilt`

**1. Gradle.** Root `build.gradle.kts`: tambah `alias(libs.plugins.hilt) apply false`. Lalu di `app/build.gradle.kts`:

```kotlin
plugins {
    // ... plugin sebelumnya ...
    alias(libs.plugins.hilt)
}

dependencies {
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
}
```

**2. Application dan Activity**

```kotlin
@HiltAndroidApp
class NotesApplication : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity() { /* isi tidak berubah */ }
```

**3. Hapus singleton manual di `AppDatabase`**

```kotlin
@Database(entities = [NoteEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}
```

**4. `di/DatabaseModule.kt`**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "notes.db").build()

    @Provides
    fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()
}
```

**5. `di/RepositoryModule.kt`**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository
}
```

**6. Anotasi**

```kotlin
class NoteRepositoryImpl @Inject constructor(private val dao: NoteDao) : NoteRepository { /* ... */ }

@HiltViewModel
class NotesViewModel @Inject constructor(private val repository: NoteRepository) : ViewModel() { /* ... */ }

@Composable
fun NotesScreen(viewModel: NotesViewModel = hiltViewModel()) { /* ... */ }
```

**7. Hapus** `di/AppContainer.kt` dan `di/AppViewModelProvider.kt`.

**Cek hasil:** jalankan `git diff demo-4a-manual-di demo-4b-hilt --stat`. ViewModel hampir tidak berubah, hanya anotasi. **ViewModel tidak peduli DI-nya manual atau Hilt**, karena ia hanya menerima `NoteRepository` lewat constructor.

---

## Tutorial 5 — Migrasi Database

**Konsep:** `AutoMigration` vs `Migration` manual.

> ### Persiapan wajib sebelum Tutorial 5
> 1. Pindah ke `demo-4b-hilt` (atau `demo-4a-manual-di` jika tanpa Hilt).
> 2. **Uninstall** aplikasi lama, lalu run.
> 3. **Tambahkan 3–4 catatan.** Ini data v1 yang harus selamat.
> 4. Setelah ini, **jangan uninstall** lagi sampai Tutorial 5C selesai.
>
> ```bash
> git switch -c my/demo-5 demo-4b-hilt
> ```

### Tutorial 5A — AutoMigration v1 → v2

**Tag hasil:** `demo-5a-automigration` · **Kasus:** menambah kolom `isPinned` (fitur pin).

**1. Entity**

```kotlin
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "0") val isPinned: Boolean = false
)
```

> Kolom `NOT NULL` baru **wajib** punya `defaultValue`, agar Room bisa mengisi baris lama.

**2. Database**

```kotlin
@Database(
    entities = [NoteEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
    exportSchema = true
)
```

**3. DAO**

```kotlin
@Query("SELECT * FROM notes ORDER BY isPinned DESC, createdAt DESC")
fun observeAll(): Flow<List<NoteEntity>>

@Query("UPDATE notes SET isPinned = :pinned WHERE id = :id")
suspend fun setPinned(id: Int, pinned: Boolean)
```

**4. Domain, Mapper, Repository, ViewModel, UI**

- `Note`: tambah `val isPinned: Boolean = false`.
- `NoteMapper`: map `isPinned` di `toDomain()` dan `toEntity()`.
- `NoteRepository`: tambah `suspend fun togglePin(note: Note)`.
- `NoteRepositoryImpl`: `dao.setPinned(note.id, !note.isPinned)`.
- `NotesViewModel`: `fun togglePin(note: Note) { viewModelScope.launch { repository.togglePin(note) } }`.
- `NotesScreen`/`NotesContent`: tambah parameter `onTogglePin: (Note) -> Unit`, tombol **Pin/Unpin** di `trailingContent`, dan ikon 📌 pada judul catatan yang di-pin.

**5. Build dan Run (tanpa uninstall).** File `schemas/.../2.json` akan muncul.

**Hasil yang diharapkan:** catatan lama **masih ada** dan tombol Pin berfungsi.

**Eksplorasi:**
- Buka `1.json` dan `2.json` berdampingan. Apa bedanya?
- Buka file hasil generate `app/build/generated/ksp/debug/kotlin/.../AppDatabase_AutoMigration_1_2_Impl.kt`. Perintah SQL apa yang ada di dalamnya?
- Database Inspector: berapa nilai `isPinned` pada baris lama?

---

### Tutorial 5B — Skema Berubah Tanpa Migrasi (Crash Disengaja)

**Tag hasil:** `demo-5b-missing-migration` · **Kasus:** menambah kolom `updatedAt`.

Tambahkan di entity:

```kotlin
@ColumnInfo(defaultValue = "0") val isPinned: Boolean = false,
@ColumnInfo(defaultValue = "0") val updatedAt: Long = 0
```

Naikkan versi, **tanpa** menambahkan migrasi 2 → 3:

```kotlin
@Database(
    entities = [NoteEntity::class],
    version = 3,                       // NAIK ke 3
    autoMigrations = [AutoMigration(from = 1, to = 2)],
    exportSchema = true
)
```

Run di emulator yang berisi data v2. Aplikasi akan **crash**. Cek Logcat:

```
java.lang.IllegalStateException: A migration from 2 to 3 was required but not found.
Please provide the necessary Migration path via
RoomDatabase.Builder.addMigration(Migration ...) or allow for destructive migrations
via one of the RoomDatabase.Builder.fallbackTo* methods.
```

**Poin belajar:**
- Room **menolak membuka** database dengan skema berbeda untuk melindungi data pengguna.
- `fallbackToDestructiveMigration()` menghilangkan crash dengan **menghapus semua data pengguna**. Boleh untuk prototype, **jangan dipakai** untuk aplikasi rilis. **Jangan tambahkan baris ini.**

---

### Tutorial 5C — Migrasi Manual v2 → v3

**Tag hasil:** `demo-5c-manual-migration`

**Kasus:** `updatedAt` harus **diisi dari `createdAt`** pada baris lama. AutoMigration hanya bisa memberi nilai default konstan, sehingga tidak cukup.

**1. `data/local/Migrations.kt`**

```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE notes ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
        // Backfill: nilai awal updatedAt = createdAt
        db.execSQL("UPDATE notes SET updatedAt = createdAt")
    }
}
```

**2. Daftarkan migrasi**

Dengan Hilt (`DatabaseModule`):

```kotlin
Room.databaseBuilder(context, AppDatabase::class.java, "notes.db")
    .addMigrations(MIGRATION_2_3)
    .build()
```

Tanpa Hilt: tambahkan `.addMigrations(MIGRATION_2_3)` pada `AppDatabase.getInstance(...)`.

`@Database` tetap `version = 3` dan `autoMigrations = [AutoMigration(from = 1, to = 2)]` dipertahankan.

**3. Domain, Mapper, Repository**

- `Note`: tambah `val updatedAt: Long = 0`.
- `NoteMapper`: map `updatedAt` di kedua arah.
- `NoteRepositoryImpl.addNote`:

```kotlin
override suspend fun addNote(title: String, content: String) {
    val now = System.currentTimeMillis()
    dao.insert(NoteEntity(title = title, content = content, createdAt = now, updatedAt = now))
}
```

**4. Run (tanpa uninstall).** Data lama tetap ada. Di Database Inspector, `updatedAt` sama dengan `createdAt` pada baris lama.

**Perbandingan:**

| Aspek | AutoMigration | Migration manual |
|---|---|---|
| Cara kerja | Room membandingkan `N.json` vs `N+1.json` | Developer menulis SQL |
| Cocok untuk | Tambah kolom/tabel, rename/hapus dengan `AutoMigrationSpec` | Transformasi/backfill data, ubah tipe kolom, pecah/gabung tabel |
| Risiko | Rendah | Lebih tinggi (SQL salah = data rusak) |
| Wajib tes | Disarankan | **Wajib** |
| Contoh di tutorial | `isPinned` (v1→2) | `updatedAt` dari `createdAt` (v2→3) |

---

## Tutorial 6 — Testing

**Konsep:** DAO test, migration test, ViewModel test dengan fake repository.

```bash
git switch -c my/demo-6 demo-5c-manual-migration
```

### Dependency test

`app/build.gradle.kts`:

```kotlin
dependencies {
    // ... sebelumnya ...

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
}
```

Jika `androidx-runner` belum ada di catalog:

```toml
androidx-runner = { group = "androidx.test", name = "runner", version = "1.7.0" }
```

Pastikan `testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"` ada di `defaultConfig` (bawaan template).

### Tutorial 6A — DAO Test

**Tag hasil:** `demo-6a-dao-test` · Instrumented test, **butuh emulator/device**. Memakai database **in-memory** (tidak menyentuh data aplikasi).

`app/src/androidTest/java/com/example/notesapp/NoteDaoTest.kt`:

```kotlin
@RunWith(AndroidJUnit4::class)
class NoteDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: NoteDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.noteDao()
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun insert_thenObserve_returnsInsertedNote() = runTest {
        dao.insert(NoteEntity(title = "Title", content = "Body"))

        val notes = dao.observeAll().first()

        assertEquals(1, notes.size)
        assertEquals("Title", notes[0].title)
    }

    @Test
    fun delete_removesNote() = runTest {
        dao.insert(NoteEntity(title = "A", content = "B"))
        val inserted = dao.observeAll().first().single()

        dao.delete(inserted)

        assertTrue(dao.observeAll().first().isEmpty())
    }

    @Test
    fun pinnedNote_isOrderedFirst() = runTest {
        dao.insert(NoteEntity(title = "old", content = "", createdAt = 1L))
        dao.insert(NoteEntity(title = "new", content = "", createdAt = 2L))
        val old = dao.observeAll().first().first { it.title == "old" }

        dao.setPinned(old.id, true)

        assertEquals(listOf("old", "new"), dao.observeAll().first().map { it.title })
    }
}
```

Jalankan dengan klik ▶ di samping nama class, atau `./gradlew connectedDebugAndroidTest`.

**Poin belajar:** `inMemoryDatabaseBuilder` cepat dan terisolasi; test memverifikasi **query SQL** (urutan pinned); `Flow.first()` mengambil emisi pertama sehingga test tetap sederhana.

---

### Tutorial 6B — Migration Test

**Tag hasil:** `demo-6b-migration-test` · Memakai file skema `1.json`, `2.json`, `3.json` di folder `schemas/`.

`app/src/androidTest/java/com/example/notesapp/MigrationTest.kt`:

```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val testDbName = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate1To2_keepsData_andDefaultsNotPinned() {
        helper.createDatabase(testDbName, 1).apply {
            execSQL("INSERT INTO notes (id, title, content, createdAt) VALUES (1, 'T', 'C', 1000)")
            close()
        }

        val db = helper.runMigrationsAndValidate(testDbName, 2, true)

        db.query("SELECT title, isPinned FROM notes WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("T", cursor.getString(0))
            assertEquals(0, cursor.getInt(1))
        }
    }

    @Test
    fun migrate2To3_backfillsUpdatedAtFromCreatedAt() {
        helper.createDatabase(testDbName, 2).apply {
            execSQL(
                "INSERT INTO notes (id, title, content, createdAt, isPinned) " +
                    "VALUES (1, 'T', 'C', 1000, 0)"
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(testDbName, 3, true, MIGRATION_2_3)

        db.query("SELECT updatedAt FROM notes WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1000L, cursor.getLong(0))
        }
    }

    @Test
    fun migrateAll_1To3() {
        helper.createDatabase(testDbName, 1).close()

        val context = ApplicationProvider.getApplicationContext<Context>()
        Room.databaseBuilder(context, AppDatabase::class.java, testDbName)
            .addMigrations(MIGRATION_2_3)
            .build()
            .apply {
                openHelper.writableDatabase.close()
                close()
            }
    }
}
```

> `MigrationTestHelper` otomatis mengenali `AutoMigration` yang dideklarasikan di `@Database`. Migrasi manual dikirim lewat parameter terakhir `runMigrationsAndValidate`.

**Eksperimen merusak:** ubah SQL di `MIGRATION_2_3` menjadi `SET updatedAt = 0`, lalu jalankan test. Test `migrate2To3_...` harus **gagal**. Inilah nilai migration test. Kembalikan SQL ke semula setelah selesai.

**Poin belajar:** tanpa file skema (`exportSchema = false`), test ini tidak bisa dibuat. Itu alasan folder `schemas/` harus di-commit.

---

### Tutorial 6C — ViewModel Test dengan Fake Repository

**Tag hasil:** `demo-6c-viewmodel-test` · Local unit test (JVM, **tanpa emulator**, sangat cepat). Mungkin dilakukan karena ViewModel hanya bergantung ke interface `NoteRepository`.

**1. `app/src/test/.../FakeNoteRepository.kt`**

```kotlin
class FakeNoteRepository : NoteRepository {

    private val notes = MutableStateFlow<List<Note>>(emptyList())
    private var nextId = 1

    override fun observeNotes(): Flow<List<Note>> = notes

    override suspend fun addNote(title: String, content: String) {
        val now = System.currentTimeMillis()
        notes.update { it + Note(nextId++, title, content, now, isPinned = false, updatedAt = now) }
    }

    override suspend fun deleteNote(note: Note) {
        notes.update { list -> list.filterNot { it.id == note.id } }
    }

    override suspend fun togglePin(note: Note) {
        notes.update { list ->
            list.map { if (it.id == note.id) it.copy(isPinned = !it.isPinned) else it }
        }
    }
}
```

**2. `app/src/test/.../MainDispatcherRule.kt`**

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(dispatcher)
    override fun finished(description: Description) = Dispatchers.resetMain()
}
```

> `viewModelScope` memakai `Dispatchers.Main`, yang tidak ada di unit test JVM. Rule ini menggantinya dengan test dispatcher.

**3. `app/src/test/.../NotesViewModelTest.kt`**

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeNoteRepository
    private lateinit var viewModel: NotesViewModel

    @Before
    fun setUp() {
        repository = FakeNoteRepository()
        viewModel = NotesViewModel(repository)   // tanpa Application, tanpa Room
    }

    @Test
    fun initialState_isEmpty() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.notes.collect {}
        }

        assertTrue(viewModel.notes.value.isEmpty())
    }

    @Test
    fun addNote_updatesNotes() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.notes.collect {}
        }

        viewModel.addNote("Title", "Body")

        assertEquals(1, viewModel.notes.value.size)
        assertEquals("Title", viewModel.notes.value.first().title)
    }

    @Test
    fun deleteNote_removesFromNotes() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.notes.collect {}
        }
        viewModel.addNote("A", "B")

        viewModel.deleteNote(viewModel.notes.value.first())

        assertTrue(viewModel.notes.value.isEmpty())
    }

    @Test
    fun togglePin_flipsPinnedFlag() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.notes.collect {}
        }
        viewModel.addNote("A", "B")

        viewModel.togglePin(viewModel.notes.value.first())

        assertTrue(viewModel.notes.value.first().isPinned)
    }
}
```

> `backgroundScope.launch { collect {} }` diperlukan karena `stateIn(WhileSubscribed)` hanya aktif jika ada collector. Tanpa collector, `.value` tetap nilai awal.

Jalankan dengan klik ▶ di class, atau `./gradlew testDebugUnitTest`.

**Poin belajar:** di Tutorial 1, ViewModel butuh `Application` dan database asli sehingga **tidak bisa** dibuat di unit test. Sekarang cukup `NotesViewModel(FakeNoteRepository())`.

**Ringkasan jenis test:**

| Jenis test | Lokasi | Butuh emulator | Menguji | Alat kunci |
|---|---|---|---|---|
| DAO test | `androidTest` | Ya | Query SQL, urutan, constraint | `Room.inMemoryDatabaseBuilder` |
| Migration test | `androidTest` | Ya | Data selamat, skema sesuai | `MigrationTestHelper`, `schemas/*.json` |
| ViewModel test | `test` | Tidak | Logika state/aksi | Fake repository, `MainDispatcherRule` |

---

## Troubleshooting

| Gejala | Penyebab | Solusi |
|---|---|---|
| `Cannot find implementation for AppDatabase` | KSP belum jalan / plugin hilang | Pastikan `ksp(libs.androidx.room.compiler)` dan plugin `ksp`; Sync + Rebuild |
| KSP error versi tidak cocok | Versi KSP ≠ versi Kotlin | Samakan awalan versi KSP dengan versi Kotlin |
| `schemas/` tidak muncul | `schemaDirectory` belum diset atau `exportSchema = false` | Cek blok `room { schemaDirectory(...) }` |
| `MigrationTestHelper` tidak menemukan skema | Plugin Room tidak aktif / skema belum ter-generate | Pastikan plugin `androidx.room`, build ulang, cek `schemas/.../1.json` |
| `AutoMigration` gagal build | Kolom baru `NOT NULL` tanpa default | Tambahkan `@ColumnInfo(defaultValue = "...")` |
| Crash `Migration didn't properly handle` | Skema hasil SQL manual ≠ entity | Samakan tipe, `NOT NULL`, dan default value di SQL vs entity |
| `Module with the Main dispatcher had failed to initialize` | `MainDispatcherRule` belum dipasang | Tambahkan `@get:Rule val mainDispatcherRule = MainDispatcherRule()` |
| `notes.value` tetap kosong di test | Tidak ada collector aktif | Tambahkan `backgroundScope.launch { viewModel.notes.collect {} }` |
| `[Hilt] Missing @HiltAndroidApp` | Manifest tidak memakai `NotesApplication` | Cek `android:name=".NotesApplication"` |
| Crash saat pindah tag ke versi DB lebih rendah | Room tidak mendukung downgrade | `adb uninstall com.example.notesapp` |

---

## Tugas Mandiri

Kerjakan di branch Anda sendiri, mulai dari `demo-6c-viewmodel-test` (atau hasil akhir Anda).

1. **Fitur edit catatan.** Tambahkan `@Update` pada DAO dan perbarui `updatedAt` setiap catatan diedit. Pertimbangkan apakah ini memerlukan migrasi.
2. **Migrasi baru (v3 → v4).** Tambahkan kolom `category` dengan nilai default `"General"`. Pilih AutoMigration atau Migration manual dan **jelaskan alasan pilihan Anda** (C5).
3. **Tulis test untuk fitur Anda:** minimal satu DAO test, satu migration test, dan satu ViewModel test.
4. **Refleksi singkat (maks. 1 halaman):** jelaskan keputusan arsitektur Anda (mengapa Repository, mengapa Flow, mengapa migrasi manual/otomatis) dan risikonya terhadap data pengguna.

---

*IF570 Mobile Application Programming — Week 7. Ganti `<username>` pada tautan dengan akun GitHub pemilik repository.*
