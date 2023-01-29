package com.github.composepermissionhandler

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

@ExperimentalPermissionHandlerApi
class PermissionHandlerHostState(private val permissionList: List<String>) {

    constructor(permission: String) : this(permissionList = listOf(permission))

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

        override fun granted() {
            if (continuation.isActive) continuation.resume(PermissionHandlerResult.GRANTED)
        }

        override fun denied(isNextRationale: Boolean) {
            if (continuation.isActive) {
                if (isNextRationale) continuation.resume(PermissionHandlerResult.DENIED_NEXT_RATIONALE)
                else continuation.resume(PermissionHandlerResult.DENIED)
            }
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
    rationale: @Composable (permissionRequest: () -> Unit, dismissRequest: () -> Unit) -> Unit =
        { permissionRequest, _ -> SideEffect { permissionRequest() } }
) {
    val currentPermissionHandlerData = hostState.currentPermissionHandlerData ?: return

    val permissionsState =
        rememberMultiplePermissionsState(currentPermissionHandlerData.permissionList) { permissionStates ->
            val permissionGranted = permissionStates.values.all { it }
            if (permissionGranted) {
                currentPermissionHandlerData.granted()
            } else {
                hostState.updatePermissionState(PermissionState.Deny)
            }
        }

    val coroutineScope = rememberCoroutineScope()
    when (val permissionState = currentPermissionHandlerData.permissionState) {
        PermissionState.Deny ->
            currentPermissionHandlerData.denied(isNextRationale = permissionsState.shouldShowRationale)
        PermissionState.Handle -> {
            if (permissionsState.shouldShowRationale) {
                rationale(
                    permissionRequest = {
                        hostState.updatePermissionState(
                            PermissionState.HideRationale(
                                PermissionState.PermissionAction.Request
                            )
                        )
                    }, dismissRequest = {
                        hostState.updatePermissionState(
                            PermissionState.HideRationale(
                                PermissionState.PermissionAction.Dismiss
                            )
                        )
                    })
            } else {
                SideEffect {
                    coroutineScope.launch {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                }
            }
        }
        is PermissionState.HideRationale -> {
            if (permissionState.permissionAction == PermissionState.PermissionAction.Dismiss) {
                currentPermissionHandlerData.denied(isNextRationale = true)
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

fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}

internal sealed class PermissionState {
    object Handle : PermissionState()
    data class HideRationale(val permissionAction: PermissionAction) : PermissionState()
    object Deny : PermissionState()

    enum class PermissionAction {
        Request, Dismiss
    }
}

internal interface PermissionHandlerData {
    val permissionList: List<String>
    val permissionState: PermissionState
    fun copy(permissionState: PermissionState = this.permissionState): PermissionHandlerData
    fun granted()
    fun denied(isNextRationale: Boolean)
}

@ExperimentalPermissionHandlerApi
enum class PermissionHandlerResult {
    DENIED,
    DENIED_NEXT_RATIONALE,
    GRANTED;
}
