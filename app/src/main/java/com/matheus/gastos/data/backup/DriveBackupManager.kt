package com.matheus.gastos.data.backup

import android.app.Activity
import android.content.Context
import android.net.Uri
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.drive.Drive
import com.google.android.gms.drive.DriveClient
import com.google.android.gms.drive.DriveContents
import com.google.android.gms.drive.DriveFile
import com.google.android.gms.drive.DriveFolder
import com.google.android.gms.drive.MetadataChangeSet
import com.google.android.gms.tasks.Tasks
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.services.drive.model.FileList
import com.matheus.gastos.data.entity.Category
import com.matheus.gastos.data.entity.Subcategory
import com.matheus.gastos.data.entity.CreditCard
import com.matheus.gastos.data.entity.Transaction
import com.matheus.gastos.data.entity.BudgetLimit
import com.matheus.gastos.data.entity.AppSettings
import com.matheus.gastos.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter
import java.io.InputStreamReader
import java.util.UUID
import java.util.concurrent.TimeUnit

// Backup data structure
@kotlinx.serialization.Serializable
data class BackupData(
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val categories: List<Category>,
    val subcategories: List<Subcategory>,
    val creditCards: List<CreditCard>,
    val transactions: List<Transaction>,
    val budgetLimits: List<BudgetLimit>,
    val settings: List<AppSettings>
)

class DriveBackupManager(private val context: Context) {
    private val SCOPE_DRIVE_FILE = Scope(DriveScopes.DRIVE_FILE)
    private val googleSignInClient: GoogleSignInClient
    private var driveClient: DriveClient? = null
    private var credential: GoogleAccountCredential? = null
    private var driveService: com.google.api.services.drive.Drive? = null

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestScopes(SCOPE_DRIVE_FILE)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    fun signIn(activity: Activity, requestCode: Int) {
        val intent = googleSignInClient.signInIntent
        activity.startActivityForResult(intent, requestCode)
    }

    fun handleSignInResult(result: com.google.android.gms.auth.api.signin.GoogleSignInAccount?): Boolean {
        result?.let { account ->
            credential = GoogleAccountCredential.usingOAuth2(context, listOf(DriveScopes.DRIVE_FILE))
                .setSelectedAccount(account.account)
            driveClient = Drive.getDriveClient(context, account)
            driveService = com.google.api.services.drive.Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            ).setApplicationName("GastosApp").build()
            true
        } ?: false
    }

    fun isSignedIn(): Boolean = GoogleSignIn.getLastSignedInAccount(context) != null

    fun signOut() {
        googleSignInClient.signOut()
        driveClient = null
        credential = null
        driveService = null
    }

    // Backup to Drive
    suspend fun backupToDrive(
        database: AppDatabase,
        fileName: String = "gastos_backup_${System.currentTimeMillis()}.json"
    ): Result<Uri> = kotlinx.coroutines.runCatching {
        val data = collectBackupData(database)
        val json = Json { prettyPrint = true }.encodeToString(data)
        
        val driveContents = Tasks.await(driveClient!!.newDriveContents())
        val outputStream = driveContents.outputStream
        val writer = OutputStreamWriter(outputStream)
        writer.write(json)
        writer.close()
        
        val metadata = MetadataChangeSet.Builder()
            .setTitle(fileName)
            .setMimeType("application/json")
            .setStarred(true)
            .build()
        
        val file = Tasks.await(driveClient!!.rootFolder.createFile(driveClient!!, metadata, driveContents))
        Uri.parse("gdrive://${file.driveId}")
    }

    // Restore from Drive
    suspend fun restoreFromDrive(fileId: String, database: AppDatabase): Result<Unit> = kotlinx.coroutines.runCatching {
        val driveFile = driveClient!!.getFile(com.google.android.gms.drive.DriveId.decodeFromString(fileId)).await()
        val contents = driveFile.open(driveClient!!, DriveFile.MODE_READ_ONLY, null).await()
        val inputStream = contents.driveContents.inputStream
        val reader = InputStreamReader(inputStream)
        val json = reader.readText()
        reader.close()
        
        val data = Json { prettyPrint = true }.decodeFromString<BackupData>(json)
        restoreData(database, data)
    }

    // List backup files
    suspend fun listBackups(): Result<List<BackupFileInfo>> = kotlinx.coroutines.runCatching {
        val query = "mimeType='application/json' and name contains 'gastos_backup_' and trashed=false"
        val request = driveService!!.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name, createdTime, size)")
            .setOrderBy("createdTime desc")
            .setPageSize(20)
        
        val fileList: FileList = request.execute()
        fileList.files?.map { file ->
            BackupFileInfo(
                id = file.id!!,
                name = file.name!!,
                createdTime = file.createdTime!!.value,
                size = file.size!!
            )
        } ?: emptyList()
    }

    // Export to local file (for sharing)
    suspend fun exportToFile(database: AppDatabase, fileName: String = "gastos_export_${System.currentTimeMillis()}.json"): Result<Uri> = kotlinx.coroutines.runCatching {
        val data = collectBackupData(database)
        val json = Json { prettyPrint = true }.encodeToString(data)
        
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Documents/GastosApp")
        }
        
        val resolver = context.contentResolver
        val uri = resolver.insert(android.provider.MediaStore.Files.getContentUri("external"), contentValues)!!
        resolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
        uri
    }

    // Import from local file
    suspend fun importFromFile(uri: Uri, database: AppDatabase): Result<Unit> = kotlinx.coroutines.runCatching {
        val inputStream = context.contentResolver.openInputStream(uri)!!
        val json = inputStream.readText()
        inputStream.close()
        val data = Json { prettyPrint = true }.decodeFromString<BackupData>(json)
        restoreData(database, data)
    }

    private suspend fun collectBackupData(database: AppDatabase): BackupData {
        return kotlinx.coroutines.withContext(Dispatchers.IO) {
            BackupData(
                categories = database.categoryDao().getAll().first(),
                subcategories = database.subcategoryDao().getByCategoryId(0).first() + database.categoryDao().getAll().first().flatMap { cat ->
                    database.subcategoryDao().getByCategoryId(cat.id).first()
                },
                creditCards = database.creditCardDao().getAll().first(),
                transactions = database.transactionDao().getByDateRange(0, Long.MAX_VALUE).first(),
                budgetLimits = database.budgetLimitDao().getActive().first(),
                settings = database.appSettingsDao().getAll().first()
            )
        }
    }

    private suspend fun restoreData(database: AppDatabase, data: BackupData) {
        database.runTransaction {
            // Clear existing data (optional - could merge instead)
            database.transactionDao().deleteAll()
            database.budgetLimitDao().deleteAll()
            database.creditCardDao().deleteAll()
            database.subcategoryDao().deleteAll()
            database.categoryDao().deleteAll()
            database.appSettingsDao().deleteAll()
            
            // Insert backed up data
            data.categories.forEach { database.categoryDao().insert(it) }
            data.subcategories.forEach { database.subcategoryDao().insert(it) }
            data.creditCards.forEach { database.creditCardDao().insert(it) }
            data.transactions.forEach { database.transactionDao().insert(it) }
            data.budgetLimits.forEach { database.budgetLimitDao().insert(it) }
            data.settings.forEach { database.appSettingsDao().insert(it) }
        }
    }

    data class BackupFileInfo(
        val id: String,
        val name: String,
        val createdTime: Long,
        val size: Long
    )
}

// Extension for DriveClient
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.tasks.await

fun <T> Task<T>.await(): T = Tasks.await(this, 30, TimeUnit.SECONDS)