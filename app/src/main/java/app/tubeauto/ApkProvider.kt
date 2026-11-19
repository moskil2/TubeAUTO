package app.tubeauto

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import java.io.File

/**
 * Udostępnia systemowemu instalatorowi własny plik APK TubeAUTO (tylko do odczytu),
 * pod content://app.tubeauto.apk/TubeAUTO.apk. Bez kopiowania: czyta zainstalowany plik.
 * Wzorowane na ApkProvider z UnknownAuto (GitMag, licencja MIT).
 */
class ApkProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "app.tubeauto.apk"
        private const val NAME = "TubeAUTO.apk"
        const val MIME = "application/vnd.android.package-archive"

        fun uri(): Uri = Uri.Builder().scheme("content").authority(AUTHORITY).path(NAME).build()

        private fun apkFile(context: Context) = File(context.applicationInfo.sourceDir)
    }

    private fun fileFor(uri: Uri): File {
        if (uri.lastPathSegment != NAME) throw SecurityException("not served: ${uri.lastPathSegment}")
        return apkFile(context!!)
    }

    override fun onCreate() = true

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor =
        ParcelFileDescriptor.open(fileFor(uri), ParcelFileDescriptor.MODE_READ_ONLY)

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor {
        val f = fileFor(uri)
        val cols = projection ?: arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        return MatrixCursor(cols).apply {
            addRow(cols.map {
                when (it) {
                    OpenableColumns.DISPLAY_NAME -> NAME
                    OpenableColumns.SIZE -> f.length()
                    else -> null
                }
            })
        }
    }

    override fun getType(uri: Uri) = MIME
    override fun insert(uri: Uri, values: ContentValues?): Uri = throw UnsupportedOperationException()
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = throw UnsupportedOperationException()
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = throw UnsupportedOperationException()
}
