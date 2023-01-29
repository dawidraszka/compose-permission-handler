package com.github.composepermissionhandler

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

@ExperimentalPermissionHandlerApi
class PermissionHandlerHostState(private val permissionList: List<String>) {

    private val mutex = Mutex()
    internal var currentPermissionHandlerData by mutableStateOf<PermissionHandlerData?>(null)

    suspend fun handlePermissions(): PermissionHandlerResult =
        mutex.withLock {
            try {
                return suspendCancellableCoroutine { continuation ->
                    currentPermissionHandlerData =
                        PermissionHandlerDataImpl(
                            continuation,
                            permissionList
                        )
                }
            } finally {
                currentPermissionHandlerData = null
            }
        }

    private class PermissionHandlerDataImpl(
        private val continuation: CancellableContinuation<PermissionHandlerResult>,
        override val permissionList: List<String>,
        override val permissionState: PermissionState = PermissionState.Handle
    ) : PermissionHandlerData {

        override fun copy(permissionState: PermissionState) =
            PermissionHandlerDataImpl(continuation, permissionList, permissionState)

        override fun grant() {
            if (continuation.isActive) continuation.resume(PermissionHandlerResult.GRANTED)
        }

        override fun deny() {
            if (continuation.isActive) continuation.resume(PermissionHandlerResult.DENIED)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as PermissionHandlerDataImpl

            if (continuation != other.continuation) return false
            if (permissionList != other.permissionList) return false
            if (permissionState != other.permissionState) return false

            return true
        }

        override fun hashCode(): Int {
            var result = continuation.hashCode()
            result = 31 * result + permissionList.hashCode()
            result = 31 * result + permissionState.hashCode()
            return result
        }
    }

    internal fun updatePermissionState(permissionState: PermissionState) {
        currentPermissionHandlerData = currentPermissionHandlerData?.copy(permissionState)
    }
}

@ExperimentalPermissionHandlerApi
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsHandlerHost(
    hostState: PermissionHandlerHostState,
    showSnackbar: suspend () -> SnackbarResult,
    snackbarCoroutineScope: CoroutineScope = rememberCoroutineScope(),
    rationaleDialog: @Composable (permissionRequest: () -> Unit, dismissRequest: () -> Unit) -> Unit =
        { permissionRequest, _ -> SideEffect { permissionRequest() } }
) {
    val currentPermissionHandlerData = hostState.currentPermissionHandlerData ?: return

    val permissionsState =
        rememberMultiplePermissionsState(currentPermissionHandlerData.permissionList) { permissionStates ->
            val permissionGranted = permissionStates.values.all { it }
            if (permissionGranted) {
                currentPermissionHandlerData.grant()
            } else {
                hostState.updatePermissionState(PermissionState.Denied)
            }
        }

    val coroutineScope = rememberCoroutineScope()
    when (val permissionState = currentPermissionHandlerData.permissionState) {
        PermissionState.Denied -> {
            if (!permissionsState.shouldShowRationale) {
                val context = LocalContext.current
                SideEffect {
                    snackbarCoroutineScope.launch {
                        when (showSnackbar()) {
                            SnackbarResult.Dismissed -> {} //no-op
                            SnackbarResult.ActionPerformed -> openAppSettings(context)
                        }
                    }
                }
            }
            currentPermissionHandlerData.deny()
        }

        PermissionState.Handle -> {
            snackbarCoroutineScope.coroutineContext.cancelChildren()
            if (permissionsState.shouldShowRationale) {
                rationaleDialog(
                    permissionRequest = {
                        hostState.updatePermissionState(PermissionState.HideDialog(PermissionState.PermissionAction.Request))
                    }, dismissRequest = {
                        hostState.updatePermissionState(PermissionState.HideDialog(PermissionState.PermissionAction.Dismiss))
                    })
            } else {
                SideEffect {
                    coroutineScope.launch {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                }
            }
        }
        is PermissionState.HideDialog -> {
            if (permissionState.permissionAction == PermissionState.PermissionAction.Dismiss) {
                currentPermissionHandlerData.deny()
            } else {
                SideEffect {
                    coroutineScope.launch {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                }
            }
        }
    }
}

private fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}

internal sealed class PermissionState {
    object Handle : PermissionState()
    data class HideDialog(val permissionAction: PermissionAction) : PermissionState()
    object Denied : PermissionState()

    enum class PermissionAction {
        Request, Dismiss
    }
}

internal interface PermissionHandlerData {
    val permissionList: List<String>
    val permissionState: PermissionState
    fun copy(permissionState: PermissionState = this.permissionState): PermissionHandlerData
    fun grant()
    fun deny()
}

@ExperimentalPermissionHandlerApi
enum class PermissionHandlerResult {
    DENIED,
    GRANTED;

    fun isGranted() = this == GRANTED
}
