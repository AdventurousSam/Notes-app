package com.sam.notes

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import android.provider.Settings.Secure.ANDROID_ID
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.tasks.await
import java.io.File

// Function to list files in Firebase Storage
suspend fun listFilesInStorageReference(storageRef: StorageReference): List<String> {
    return try {
        val result = storageRef.listAll().await()
        result.items.map { it.name }.sortedWith(compareBy { filename ->
            filename.split(" ")[0].toInt()
        })
    } catch (e: Exception) {
        emptyList()
    }
}

fun uploadFile(context: Context, folderReference: StorageReference, fileURI: Uri): UploadTask {
    val fileName = getFileName(context, fileURI)
    val fileReference = folderReference.child("/$fileName")
    return fileReference.putFile(fileURI)
}

fun getFileName(context: Context, uri: Uri): String? {
    var fileName: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            fileName = cursor.getString(nameIndex)
        }
    }
    return fileName
}

// Function to download a file from Firebase Storage
fun downloadFile(storageRef: StorageReference, fileName: String, downloadLocation: File): FileDownloadTask? {
    return try {
        val fileRef = storageRef.child(fileName)
        val downloadTask = fileRef.getFile(downloadLocation)
        downloadTask
    } catch (e: Exception) {
        null
    }
}

fun deleteFile(storageRef: StorageReference, fileName: String): Task<Void> {
    return storageRef.child(fileName).delete()
}

@SuppressLint("HardwareIds")
fun getDeviceId(context: Context): String {
    return Settings.Secure.getString(context.contentResolver, ANDROID_ID) ?: ""
}