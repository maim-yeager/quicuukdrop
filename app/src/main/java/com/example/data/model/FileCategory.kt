package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class FileCategory(
    val title: String,
    val icon: ImageVector,
    val color: Color
) {
    ALL("All", Icons.Default.InsertDriveFile, Color(0xFF64B5F6)),
    IMAGE("Images", Icons.Default.Image, Color(0xFFFF4081)),
    VIDEO("Videos", Icons.Default.Videocam, Color(0xFF7C4DFF)),
    AUDIO("Audio", Icons.Default.AudioFile, Color(0xFF00E5FF)),
    DOCUMENT("Documents", Icons.Default.Description, Color(0xFFFFB300)),
    APK("Apps", Icons.Default.Android, Color(0xFF00E676)),
    ZIP("Archives", Icons.Default.FolderZip, Color(0xFFFF6D00)),
    OTHER("Other", Icons.Default.InsertDriveFile, Color(0xFF90A4AE))
}
