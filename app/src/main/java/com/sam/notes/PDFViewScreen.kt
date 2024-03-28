package com.sam.notes

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.doOnLayout
import com.sam.pdfReader.PDFView
import java.io.File
import java.io.FileInputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PDFViewerScreen(navigateUp: () -> Unit, context: Context, filename: String) {
    val inputStream = FileInputStream(File(context.filesDir, filename))
    val interactionSource = remember{ MutableInteractionSource() }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
                navigationIcon = {
                    Icon(
                            Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            modifier = Modifier
                                .padding(start = 15.dp, end = 16.dp)
                                .clickable(
                                        interactionSource = interactionSource,
                                        indication = rememberRipple(
                                                bounded = false,
                                                color = Color.Black,
                                                radius = 22.dp
                                        ),
                                        onClick = {
                                            navigateUp()
                                        }
                                ),
                            contentDescription = "back",
                            tint = Color.White
                    )
                },
                title = {
                    Text(
                            text = filename,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                    )
                }, colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Black
        )
        )
    }) {
        Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AndroidView(
                    factory = { context ->
                        val adView = PDFView(context, null)
                        adView.fromStream(inputStream).nightMode(false)
                            .enableDoubletap(true)
                            .spacing(0)
                            .load()
                        adView
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp),
                    update = { pdfViewer ->
                        pdfViewer.doOnLayout {}
                    })
        }
    }
}