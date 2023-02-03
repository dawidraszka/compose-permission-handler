## ComposePermissionHandler
### Runtime permissions in Compose made simple

ComposePermissionHandler is a library that helps to implement runtime permission handling in [Compose](https://developer.android.com/jetpack/compose) using [Accompanist](https://github.com/google/accompanist) and its [Jetpack Compose Permissions](https://github.com/google/accompanist/tree/main/permissions).
It's main purpose is to make permissions handling more concise.

### Declaring dependency
To be added soon!

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

For more detailed usage please head over to [sample](https://github.com/dawidraszka/ComposePermissionHandler/tree/api-streamline/sample) or [source code with documentation](https://github.com/dawidraszka/ComposePermissionHandler/blob/api-streamline/library/src/main/java/com/github/dawidraszka/composepermissionhandler/ComposePermissionHandler.kt)!

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

Of course, some things can (and should) be abstracted away (like the dialog used as rationale), to make it even cleaner!
