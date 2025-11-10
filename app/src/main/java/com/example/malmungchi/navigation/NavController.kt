package com.example.malmungchi.navigation

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy

/** 현재 최상단 목적지 간단 로그 (공개 API만 사용) */
fun NavController.logTop(tag: String = "NAV") {
    val dest = currentDestination
    val name = dest?.route
        ?: dest?.let { "id=${it.id}, navigator=${it.navigatorName}" }
        ?: "unknown"
    Log.d(tag, "top = $name")
}

/** (선택) NavDestination용 안전한 이름 */
fun NavDestination.safeName(): String =
    route ?: "id=$id, navigator=$navigatorName"

fun NavController.logHierarchy(tag: String = "NAV") {
    val dest = currentDestination ?: run {
        Log.d(tag, "hierarchy = <none>")
        return
    }
    val chain = dest.hierarchy
        .map { it.route ?: "id=${it.id}, navigator=${it.navigatorName}" }
        .joinToString(" -> ")
    Log.d(tag, "hierarchy = $chain")
}

@Composable
fun LogNavDestinations(navController: NavController, tag: String = "NAV") {
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, dest, _ ->
            Log.d(tag, "dest = ${dest.route ?: dest.id}")
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }
}