package com.github.dawidraszka.composepermissionhandler.sample

import android.Manifest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.dawidraszka.composepermissionhandler.ExperimentalPermissionHandlerApi
import com.github.dawidraszka.composepermissionhandler.PermissionHandlerHostState
import com.github.dawidraszka.composepermissionhandler.PermissionHandlerResult
import com.github.dawidraszka.composepermissionhandler.PermissionHandlerHost
import com.github.dawidraszka.composepermissionhandler.openAppSettings
import kotlinx.coroutines.launch
import com.github.dawidraszka.composepermissionhandler.sample.ui.theme.ComposePermissionHandlerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposePermissionHandlerTheme {
                SampleScreen()
            }
        }
    }
}

@OptIn(ExperimentalPermissionHandlerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SampleScreen() {
    val snackbarHostState = SnackbarHostState()
    val permissionHandlerHostState =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionHandlerHostState(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            PermissionHandlerHostState(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

    PermissionHandlerHost(
        hostState = permissionHandlerHostState,
        rationale = { permissionRequest, dismissRequest ->
            AlertDialog(
                modifier = Modifier.padding(horizontal = 12.dp),
                onDismissRequest = dismissRequest,
                title = {
                    Text(text = "Permission Required!")
                },
                text = {
                    Text("This permission is required. Please grant the permission on the next screen.")
                },
                confirmButton = {
                    Button(onClick = permissionRequest) {
                        Text(text = "Ok")
                    }
                },
                dismissButton = {
                    Button(onClick = dismissRequest) {
                        Text(text = "Cancel")
                    }
                }
            )
        })

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            imageUri = uri
        }
    )

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Content(padding, imageUri) {
            coroutineScope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                when(permissionHandlerHostState.handlePermissions()){
                    PermissionHandlerResult.DENIED -> {
                        val result = snackbarHostState.showSnackbar(
                            "App permission denied",
                            "Settings",
                            duration = SnackbarDuration.Short
                        )
                        when (result) {
                            SnackbarResult.Dismissed -> {} // noop
                            SnackbarResult.ActionPerformed -> openAppSettings(context)
                        }
                    }
                    PermissionHandlerResult.GRANTED -> imagePicker.launch("image/*")
                    PermissionHandlerResult.DENIED_NEXT_RATIONALE -> {} // noop
                }
            }
        }
    }
}

@Composable
fun Content(padding: PaddingValues, imageUri: Uri?, onPermissionHandleClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Image URI: $imageUri")
        Spacer(modifier = Modifier.size(24.dp))
        Button(onClick = onPermissionHandleClick) {
            Text("Pick image")
        }
    }
}
