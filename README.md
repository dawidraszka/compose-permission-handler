## ComposePermissionHandler
### Runtime permissions in Compose made simple

ComposePermissionHandler is a library that helps to implement runtime permission handling in [Compose](https://developer.android.com/jetpack/compose) using [Accompanist](https://github.com/google/accompanist) and it's [Jetpack Compose Permissions](https://github.com/google/accompanist/tree/main/permissions).
It's main purpose is to make permissions handling more consise.

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
