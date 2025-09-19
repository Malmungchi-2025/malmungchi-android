package com.malmungchi.feature.ai

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun MicButton(vm: ChatViewModel) {
    val context = LocalContext.current
    val permission = android.Manifest.permission.RECORD_AUDIO

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) vm.startRecording()
        else Toast.makeText(context, "마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
    }

    val granted = ContextCompat.checkSelfPermission(context, permission) ==
            PackageManager.PERMISSION_GRANTED

    val isBusy = vm.ui.value.isRecording || vm.ui.value.isLoading

    Image(
        painter = painterResource(
            id = if (isBusy) R.drawable.ic_chat_mike_ing else R.drawable.ic_chat_mike
        ),
        contentDescription = "Mic",
        modifier = Modifier
            .size(56.dp) // ✅ 두 리소스 동일 사이즈 강제
            .clickable {
                if (vm.ui.value.isLoading) return@clickable
                if (!vm.ui.value.isRecording) {
                    if (granted) vm.startRecording() else launcher.launch(permission)
                } else {
                    vm.stopAndSend()
                }
            }
    )
}