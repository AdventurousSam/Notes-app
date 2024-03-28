package com.sam.notes

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File

@Composable
fun CS304notes(auth: FirebaseAuth, onOpen: (String?) -> Unit) {
    val context = LocalContext.current
    val storage = Firebase.storage
    val storageRef = storage.reference
    val cs304ref = storageRef.child("/CS304")
    val fileNames = remember { mutableStateOf<List<String>>(emptyList()) }
    
    var uploadText by remember { mutableStateOf("Upload Files") }

    val chooseFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {uris ->
        uris.forEach { fileUri ->
            uploadFile(context, cs304ref, fileUri)
                .addOnProgressListener { snapshot ->
                    val progress = (100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount
                    uploadText = "Upload progress: ${progress.toInt()}%" }
                .addOnSuccessListener { uploadText = "Upload files" }
                .addOnFailureListener { exception -> uploadText = "Upload failed: ${exception.message}"}

        }
    }

    val uploadOnClick = if (auth.currentUser?.email == "b22127@students.iitmandi.ac.in" || auth.currentUser?.email == "sameerkabir8@gmail.com") {
        { chooseFileLauncher.launch("*/*") }
    } else { {Toast.makeText(context, "Leave Sam's work for him, focus on your own!", Toast.LENGTH_LONG).show()} }

    LaunchedEffect(Unit) {
        val files = listFilesInStorageReference(cs304ref)
        fileNames.value = files
    }
    Column {
        Button(onClick = uploadOnClick, modifier = Modifier.padding(20.dp)) {
            Text(uploadText, fontSize = 20.sp)
        }
        LazyColumn(
                modifier = Modifier.padding(12.dp)
        ) {
            items(fileNames.value) { fileName ->
                NotesItem(auth, context, fileName, cs304ref, onOpen)
            }
        }
    }
}

@Composable
fun NotesItem(
    auth: FirebaseAuth,
    context: Context,
    fileName: String,
    storageRef: StorageReference,
    onOpen: (String?) -> Unit
) {
    var downloadStatus by remember { mutableStateOf("") }
    val deleteOnClick: () -> Unit = if (auth.currentUser?.email == "b22127@students.iitmandi.ac.in" || auth.currentUser?.email == "sameerkabir8@gmail.com") {
        { deleteFile(storageRef, fileName) }
    } else { {Toast.makeText(context, "Don't try to be over smart", Toast.LENGTH_LONG).show()} }
    Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(SpringSpec(Spring.DampingRatioNoBouncy, Spring.StiffnessLow))
                .clickable {
                    val dir = context.filesDir
                    val file = File(dir, fileName)
                    if (file.exists()) {
                        onOpen(fileName)
                    } else {
                        downloadFile(storageRef, fileName, file)
                            ?.addOnProgressListener { snapshot ->
                                val progress =
                                    (100.0 * snapshot.bytesTransferred) / snapshot.totalByteCount
                                downloadStatus = "    ${progress.toInt()}%"
                            }
                            ?.addOnSuccessListener { _ ->
                                // File downloaded successfully
                                downloadStatus = ""
                                onOpen(fileName)
                            }
                            ?.addOnFailureListener { exception ->
                                // Error occurred during download
                                downloadStatus = "Error"
                                "Download failed: ${exception.message}"
                            }
                    }
                }
    ) {
        Row {
            Text(
                    fileName + downloadStatus,
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(0.8f),
                    fontSize = 16.sp
            )
            IconButton(onClick = deleteOnClick) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete this file", tint = Color(0xFF884747)
                )
            }
        }
    }
}