package com.example.data.file

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.example.data.model.FileCategory
import com.example.data.model.SharedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaScanner(private val context: Context) {

    suspend fun getFilesForCategory(category: FileCategory): List<SharedFile> = withContext(Dispatchers.IO) {
        val fileList = mutableListOf<SharedFile>()
        try {
            when (category) {
                FileCategory.ALL -> {
                    fileList.addAll(queryImages())
                    fileList.addAll(queryVideos())
                    fileList.addAll(queryAudio())
                    fileList.addAll(queryDocuments())
                }
                FileCategory.IMAGE -> fileList.addAll(queryImages())
                FileCategory.VIDEO -> fileList.addAll(queryVideos())
                FileCategory.AUDIO -> fileList.addAll(queryAudio())
                FileCategory.DOCUMENT -> fileList.addAll(queryDocuments())
                FileCategory.APK -> fileList.addAll(queryByExtension(listOf("apk"), FileCategory.APK))
                FileCategory.ZIP -> fileList.addAll(queryByExtension(listOf("zip", "rar", "7z", "tar", "gz"), FileCategory.ZIP))
                FileCategory.OTHER -> fileList.addAll(queryByExtension(listOf("txt", "json", "xml", "csv", "bin"), FileCategory.OTHER))
            }
        } catch (_: Exception) {
            // Graceful fallback
        }

        // Sort by newest first
        fileList.sortedByDescending { it.dateModified }
    }

    private fun queryImages(): List<SharedFile> {
        val list = mutableListOf<SharedFile>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_MODIFIED
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val sizeCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val mimeCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
            val dateCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)

            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val name = it.getString(nameCol) ?: "Image_$id.jpg"
                val size = it.getLong(sizeCol)
                val mime = it.getString(mimeCol) ?: "image/jpeg"
                val date = it.getLong(dateCol) * 1000
                val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                list.add(
                    SharedFile(
                        id = "img_$id",
                        uri = uri,
                        name = name,
                        size = size.coerceAtLeast(0L),
                        mimeType = mime,
                        category = FileCategory.IMAGE,
                        dateModified = date
                    )
                )
            }
        }
        return list
    }

    private fun queryVideos(): List<SharedFile> {
        val list = mutableListOf<SharedFile>()
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_MODIFIED,
            MediaStore.Video.Media.DURATION
        )
        val sortOrder = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val sizeCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val mimeCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val dateCol = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)
            val durationCol = it.getColumnIndex(MediaStore.Video.Media.DURATION)

            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val name = it.getString(nameCol) ?: "Video_$id.mp4"
                val size = it.getLong(sizeCol)
                val mime = it.getString(mimeCol) ?: "video/mp4"
                val date = it.getLong(dateCol) * 1000
                val duration = if (durationCol != -1) it.getLong(durationCol) else 0L
                val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

                list.add(
                    SharedFile(
                        id = "vid_$id",
                        uri = uri,
                        name = name,
                        size = size.coerceAtLeast(0L),
                        mimeType = mime,
                        category = FileCategory.VIDEO,
                        dateModified = date,
                        durationMs = duration.coerceAtLeast(0L)
                    )
                )
            }
        }
        return list
    }

    private fun queryAudio(): List<SharedFile> {
        val list = mutableListOf<SharedFile>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_MODIFIED
        )
        val sortOrder = "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val sizeCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val dateCol = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val name = it.getString(nameCol) ?: "Audio_$id.mp3"
                val size = it.getLong(sizeCol)
                val mime = it.getString(mimeCol) ?: "audio/mpeg"
                val date = it.getLong(dateCol) * 1000
                val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                list.add(
                    SharedFile(
                        id = "aud_$id",
                        uri = uri,
                        name = name,
                        size = size.coerceAtLeast(0L),
                        mimeType = mime,
                        category = FileCategory.AUDIO,
                        dateModified = date
                    )
                )
            }
        }
        return list
    }

    private fun queryDocuments(): List<SharedFile> {
        val list = mutableListOf<SharedFile>()
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.MIME_TYPE} LIKE ? OR ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("application/pdf", "text/%", "%.pdf")

        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val mimeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val dateCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)

            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val name = it.getString(nameCol) ?: "Document_$id.pdf"
                val size = it.getLong(sizeCol)
                val mime = it.getString(mimeCol) ?: "application/pdf"
                val date = it.getLong(dateCol) * 1000
                val uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)

                list.add(
                    SharedFile(
                        id = "doc_$id",
                        uri = uri,
                        name = name,
                        size = size.coerceAtLeast(0L),
                        mimeType = mime,
                        category = FileCategory.DOCUMENT,
                        dateModified = date
                    )
                )
            }
        }
        return list
    }

    private fun queryByExtension(extensions: List<String>, category: FileCategory): List<SharedFile> {
        val list = mutableListOf<SharedFile>()
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_MODIFIED
        )

        val clauses = extensions.joinToString(" OR ") { "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE ?" }
        val selectionArgs = extensions.map { "%.$it" }.toTypedArray()

        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Files.getContentUri("external"),
            projection,
            clauses,
            selectionArgs,
            "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val mimeCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val dateCol = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED)

            while (it.moveToNext()) {
                val id = it.getLong(idCol)
                val name = it.getString(nameCol) ?: "File_$id"
                val size = it.getLong(sizeCol)
                val mime = it.getString(mimeCol) ?: "application/octet-stream"
                val date = it.getLong(dateCol) * 1000
                val uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)

                list.add(
                    SharedFile(
                        id = "ext_${category.name}_$id",
                        uri = uri,
                        name = name,
                        size = size.coerceAtLeast(0L),
                        mimeType = mime,
                        category = category,
                        dateModified = date
                    )
                )
            }
        }
        return list
    }

    fun parseFromUri(uri: Uri): SharedFile {
        var name = "Shared_File"
        var size = 0L
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex) ?: name
                }
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }

        val category = determineCategory(name, mimeType)
        return SharedFile(
            id = "uri_${System.currentTimeMillis()}_${name.hashCode()}",
            uri = uri,
            name = name,
            size = size,
            mimeType = mimeType,
            category = category,
            dateModified = System.currentTimeMillis()
        )
    }

    private fun determineCategory(name: String, mimeType: String): FileCategory {
        val extension = MimeTypeMap.getFileExtensionFromUrl(name).lowercase()
        return when {
            mimeType.startsWith("image/") || extension in listOf("jpg", "jpeg", "png", "webp", "gif", "svg") -> FileCategory.IMAGE
            mimeType.startsWith("video/") || extension in listOf("mp4", "mkv", "avi", "mov", "webm") -> FileCategory.VIDEO
            mimeType.startsWith("audio/") || extension in listOf("mp3", "wav", "flac", "m4a", "ogg", "aac") -> FileCategory.AUDIO
            mimeType.contains("pdf") || mimeType.contains("document") || extension in listOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt") -> FileCategory.DOCUMENT
            extension == "apk" || mimeType == "application/vnd.android.package-archive" -> FileCategory.APK
            extension in listOf("zip", "rar", "7z", "tar", "gz") -> FileCategory.ZIP
            else -> FileCategory.OTHER
        }
    }
}
