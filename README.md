## ComposePermissionHandler
### Runtime permissions in Jetpack Compose made simple

ComposePermissionHandler is a library that helps to implement runtime permission handling in [Jetpack Compose](https://developer.android.com/jetpack/compose).
It's main purpose is to make permissions handling more concise.

### Versioning
There are multiple version currently being released, depending on [Compose UI version](https://developer.android.com/jetpack/androidx/releases/compose-ui) used in your project:

| <b>Compose UI version</b> | <b>ComposePermissionHandler version</b> |
|:-------------------------:|:---------------------------------------:|
|  Compose UI 1.0 (1.0.x)   |                  1.1.0                  |
|  Compose UI 1.1 (1.1.x)   |                  1.2.0                  |
|  Compose UI 1.2 (1.2.x)   |                  1.3.0                  |

<b>Version 1.0.0 is legacy and should not be used as it may cause compatibility problems!</b>

### Declaring dependency
Add `jitpack` to your repositories:
```
repositories {
    maven("https://jitpack.io")
}
```
Add the dependency:
```kotlin
dependencies {
    implementation("com.github.dawidraszka.compose-permission-handler:core:[Version]")
    // Only required if you want to use utils package
    implementation("com.github.dawidraszka.compose-permission-handler:utils:[Version]")
}
```

### How to use it
1. Inside your `@Composable` function create `PermissionHandlerHostState` and `PermissionHandlerHost`:
```kotlin
@Composable
fun SampleScreen(){
    val permissionHandlerHostState = PermissionHandlerHostState(permissionList = /* ... */)
    PermissionHandlerHost(hostState = permissionHandlerHostState)
}
```
2. Create coroutine scope:
```kotlin
val coroutineScope = rememberCoroutineScope()
```
3. Use `@Composable` with `onClick` parameter to make permission call (it is possible to do it in other places too, like SideEffect):
```kotlin
Button(onClick = {
    coroutineScope.launch {
        when(permissionHandlerHostState.handlePermissions()){
            PermissionHandlerResult.GRANTED -> { /* Permissions granted. Do your action here */ }
            PermissionHandlerResult.DENIED -> { /* Permissions were denied. Communicate to user
            denial for instance, with a Snackbar */ }
            PermissionHandlerResult.DENIED_NEXT_RATIONALE -> { /* Permissions were denied, but 
            there will be one more try with rationale. Usually, there's no need to do anything here. */ }
        }
    }
}) {
    Text(text = "Action requiring permissions")
}
```

For more detailed usage please head over to [sample](https://github.com/dawidraszka/compose-permission-handler/tree/main/sample) or [source code with documentation](https://github.com/dawidraszka/compose-permission-handler/blob/main/core/src/main/java/com/dawidraszka/composepermissionhandler/ComposePermissionHandler.kt)!

### Comparison to a more standard approach
Imagine a case in which you want to perform an action on button click. This action requires some permissions, so they have to be handled. You want to follow Google's footsteps and you want to create a flow similar to the one in [Google Maps app](ttps://play.google.com/store/apps/details?id=com.google.android.apps.maps). That means showing a snackbar if permissions have been denied (for any reason), unless there's a rationale to be shown in which case, you want to show a dialog. The snackbar should have an action to quickly send the user to settings. Here's how you might implement in classic way:
```kotlin
@Composable
fun SampleScreen() {
    val snackbarHostState = SnackbarHostState()
    // Needed to determine whether to show snackbar or not
    var showGoToSettings by remember { mutableStateOf(false) }

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val imagesPermissionState = rememberPermissionState(permission) { permissionState ->
        if (permissionState) {
            // Do your action
        } else {
            // If permission was denied for any reason, show the snackbar...
            showGoToSettings = true
        }
    }

    if (imagesPermissionState.status.shouldShowRationale) {
        // ... unless there is rationale available, in which case, override the flag
        showGoToSettings = false
    }

    var openRationaleDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    // Show snackbar if permissions were denied and there's no rationale to be displayed
    if (showGoToSettings) {
        LaunchedEffect(key1 = snackbarHostState) {
            val snackbarResult = snackbarHostState.showSnackbar(
                "App permission denied.",
                "Settings",
                duration = SnackbarDuration.Short
            )
            when (snackbarResult) {
                SnackbarResult.Dismissed -> {}
                SnackbarResult.ActionPerformed -> {
                    // Open app settings if snackbar's action was performed
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", context.packageName, null)
                    )
                    context.startActivity(intent)
                }
            }
            showGoToSettings = false
        }
    }

    val coroutineScope = rememberCoroutineScope()
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        // Button which requests permissions (or 'launches' action specified in permission request
        // result callback
        Button(onClick = {
            coroutineScope.launch {
                // Hide the snackbar in case one is still being displayed
                snackbarHostState.currentSnackbarData?.dismiss()
                // If there is rationale to be displayed, set the flag. Otherwise, launch request.
                if (imagesPermissionState.status.shouldShowRationale) {
                    openRationaleDialog = true
                } else {
                    imagesPermissionState.launchPermissionRequest()
                }
            }
        }) {
            Text("Pick image")
        }
    }

    // If there is a rationale to be displayed, do that
    if (openRationaleDialog) {
        AlertDialog(
            modifier = Modifier.padding(horizontal = 12.dp),
            onDismissRequest = { openRationaleDialog = false },
            title = {
                Text(text = "Permission Required!")
            },
            text = {
                Text("This permission is required. Please grant the permission on the next popup.")
            },
            confirmButton = {
                Button(onClick = {
                    openRationaleDialog = false
                    imagesPermissionState.launchPermissionRequest()
                }) {
                    Text(text = "Ok")
                }
            },
            dismissButton = {
                Button(onClick = { openRationaleDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}
```

**Lengthy, right?** It's also pretty hard to follow with all the states and conditions involved. Here's how the same thing can be accomplished with the library:
```kotlin
@Composable
fun SampleScreen() {
    val snackbarHostState = SnackbarHostState()

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    // Declare permission host state and permission handler host, similarly to snackbar
    val permissionHandlerHostState = PermissionHandlerHostState(permission)
    PermissionHandlerHost(hostState = permissionHandlerHostState,
        // You don't have to specify rationale if you don't need it
        rationale = { permissionRequest, dismissRequest -> // Handy callbacks to make code concise
            AlertDialog(
                modifier = Modifier.padding(horizontal = 12.dp),
                onDismissRequest = dismissRequest,
                title = {
                    Text(text = "Permission Required!")
                },
                text = {
                    Text("This permission is required. Please grant the permission on the next popup.")
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

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        Button(onClick = {
            coroutineScope.launch {
                // Hide the snackbar in case one is still being displayed
                snackbarHostState.currentSnackbarData?.dismiss()
                // Make the call to handle permissions
                when (permissionHandlerHostState.handlePermissions()) {
                    PermissionHandlerResult.GRANTED -> { /* Do your action */ }
                    PermissionHandlerResult.DENIED -> {
                        // No need to check anything as library differentiates between denied state
                        // and denied, but next is a rationale
                        val snackbarResult = snackbarHostState.showSnackbar(
                            "App permission denied.",
                            "Settings",
                            duration = SnackbarDuration.Short
                        )
                        when (snackbarResult) {
                            SnackbarResult.Dismissed -> {}
                            SnackbarResult.ActionPerformed -> {
                                openAppSettings(context)
                            }
                        }
                    }
                    PermissionHandlerResult.DENIED_NEXT_RATIONALE -> { /* Usually there's no need to 
                    do anything here */ }
                }
            }
        }) {
            Text("Pick image")
        }
    }
}
```

The code can be simplified even further with [utils package](https://github.com/dawidraszka/compose-permission-handler/tree/main/utils):
```kotlin
when (permissionHandlerHostState.handlePermissions()) {
    PermissionHandlerResult.GRANTED -> { /* Do your action */ }
    PermissionHandlerResult.DENIED -> {
        snackbarHostState.showAppSettingsSnackbar(
            message = "App permission denied",
            openSettingsActionLabel = "Settings",
            context = context
        )
    }
    PermissionHandlerResult.DENIED_NEXT_RATIONALE -> {}
}
```

### Final note
Shout out to [@tomczyn](https://github.com/tomczyn) and his [article](https://easycontext.io/automatic-app-versioning-the-easy-way/) about app versioning which helped me setting up versioning for this library!
