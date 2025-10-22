package com.malmungchi.feature.mypage

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.mypage.R as MyPageR
import kotlinx.coroutines.flow.collectLatest
import android.provider.Settings
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

// ===== Colors =====
private val Blue_195FCF = Color(0xFF195FCF)
private val Gray_C9CAD4 = Color(0xFFC9CAD4)
private val ScreenPadding = 20.dp



@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onClickBack: () -> Unit = {},
    onClickRemind: () -> Unit = {},
    onClickWithdraw: () -> Unit = {},
    navigateToLogin: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var pushEnabled by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    var showLogout by remember { mutableStateOf(false) }
    var showDisableAlert by remember { mutableStateOf(false) }
    var showRemindAlert by remember { mutableStateOf(false) }

    // ✅ Lifecycle 감지 — 설정 화면 다녀온 뒤 즉시 반영
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                if (enabled != pushEnabled) {
                    pushEnabled = enabled
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // ✅ OS 설정 변경 감지 — 화면 복귀 시 다시 체크
    LaunchedEffect(Unit) {
        snapshotFlow { NotificationManagerCompat.from(context).areNotificationsEnabled() }
            .collect { enabled ->
                pushEnabled = enabled
                // viewModel.savePushStatus(enabled)  ← DB나 SharedPrefs에 저장하려면 여기에
            }
    }

    // VM 이벤트 → 로그인 화면 이동
    LaunchedEffect(Unit) {
        viewModel.navigateLogin.collectLatest { navigateToLogin() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding)
    ) {
        Spacer(Modifier.height(48.dp))
        SettingsTopBar(title = "설정", onBack = onClickBack)
        Spacer(Modifier.height(12.dp))

//        // ✅ 푸시 알림 스위치
//        SettingsItemPush(
//            checked = pushEnabled,
//            onCheckedChange = { enabled ->
//                if (enabled) {
//                    ensureNotificationChannelActive(context)
//                    pushEnabled = true
//                    // viewModel.savePushStatus(true)
//                } else {
//                    showDisableAlert = true
//                }
//            }
//        )
        SettingsItemPush(
            checked = pushEnabled,
            onCheckedChange = { enabled ->
                if (enabled) {
                    // ✅ 알림 설정창으로 이동
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)

                    // ✅ 상태값 갱신 (다음 복귀 시 LifecycleObserver로 다시 반영됨)
                    pushEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()

                } else {
                    // 기존처럼 '정말 끌까요?' 다이얼로그 표시
                    showDisableAlert = true
                }
            }
        )

        Divider(Modifier.fillMaxWidth(), color = Color(0xFFF2F2F2))

        // ✅ 리마인드 알림 클릭
        SettingsChevronItem(
            text = "리마인드 알림",
            onClick = {
                if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                    onClickRemind()
                } else {
                    showRemindAlert = true
                }
            }
        )

        Divider(Modifier.fillMaxWidth(), color = Color(0xFFF2F2F2))

        SettingsPlainItem(text = "로그아웃", onClick = { showLogout = true })
    }

    // ✅ 알림 끄기 확인 다이얼로그
    if (showDisableAlert) {
        NotificationDisableAlert.Show(
            onConfirm = {
                pushEnabled = false
                showDisableAlert = false
                // viewModel.savePushStatus(false)
            },
            onDismiss = { showDisableAlert = false }
        )
    }

    // ✅ 알림 비활성 상태 안내 다이얼로그
    if (showRemindAlert) {
        NotificationBlockedAlert.Show(
            context = context,   // ⚡ 반드시 전달
            onDismiss = { showRemindAlert = false }
        )
    }

    // ✅ 로그아웃 다이얼로그
    if (showLogout) {
        LogoutAlert.Show(
            onConfirm = {
                showLogout = false
                viewModel.logout()
            },
            onDismiss = { showLogout = false }
        )
    }
}

@Composable
private fun SettingsTopBar(
    title: String,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        )
        IconButton(
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = onBack
        ) {
            Icon(
                painter = painterResource(id = MyPageR.drawable.ic_back),
                contentDescription = "뒤로가기",
                tint = Color.Unspecified // 디자인 컬러 유지
            )
        }
    }
}

@Composable
private fun SettingsItemPush(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "푸시 알림",
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Black
            ),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Blue_195FCF,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Gray_C9CAD4
            )
        )
    }
}

@Composable
private fun SettingsChevronItem(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Black
            ),
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(id = MyPageR.drawable.ic_right),
            contentDescription = null,
            tint = Color(0xFF9E9E9E)
        )
    }
}

@Composable
private fun SettingsPlainItem(
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Black
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SettingsScreenPreview() {
    MaterialTheme { SettingsScreen() }
}


class LogoutAlert {
    companion object {
        @Composable
        fun Show(
            onConfirm: () -> Unit, // ✅ 추가
            onDismiss: () -> Unit  // ✅ 그대로 유지
        ) {
            AlertDialog(
                onDismissRequest = { onDismiss() },
                title = {
                    Text(
                        "로그아웃을 진행하시겠습니까?",
                        fontFamily = Pretendard,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    // ❌ 원래 두 줄이던 문구 제거 — 디자인 동일 유지
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // ✅ 왼쪽: 네 (아웃라인)
                        OutlinedButton(
                            onClick = { onConfirm() },
                            shape = RoundedCornerShape(50),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF195FCF)),
                            modifier = Modifier
                                .height(42.dp)
                                .weight(1f)
                        ) {
                            Text("네", fontSize = 16.sp, fontFamily = Pretendard)
                        }
                        Spacer(Modifier.width(8.dp))
                        // ✅ 오른쪽: 아니요 (파란색)
                        Button(
                            onClick = { onDismiss() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .height(42.dp)
                                .weight(1f)
                        ) {
                            Text("아니요", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White)
                        }
                    }
                },
                dismissButton = {},
                containerColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

class NotificationDisableAlert {
    companion object {
        @Composable
        fun Show(onConfirm: () -> Unit, onDismiss: () -> Unit) {
            AlertDialog(
                onDismissRequest = { onDismiss() },
                title = {
                    Text(
                        "정말 알림을 끄시겠습니까?",
                        fontFamily = Pretendard,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { onConfirm() },
                            shape = RoundedCornerShape(50),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue_195FCF),
                            modifier = Modifier.height(42.dp).weight(1f)
                        ) { Text("네", fontSize = 16.sp, fontFamily = Pretendard) }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = { onDismiss() },
                            colors = ButtonDefaults.buttonColors(containerColor = Blue_195FCF),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.height(42.dp).weight(1f)
                        ) { Text("아니요", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White) }
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

class NotificationBlockedAlert {
    companion object {
        @Composable
        fun Show(onDismiss: () -> Unit, context: Context) {
            AlertDialog(
                onDismissRequest = { onDismiss() },
                title = {
                    Text(
                        "알림이 비활성화되어 있습니다.\n설정에서 알림을 켜주세요.",
                        fontFamily = Pretendard,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { onDismiss() },
                            shape = RoundedCornerShape(50),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue_195FCF),
                            modifier = Modifier.height(42.dp).weight(1f)
                        ) { Text("닫기", fontSize = 16.sp, fontFamily = Pretendard) }

                        Spacer(Modifier.width(8.dp))

                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Blue_195FCF),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.height(42.dp).weight(1f)
                        ) { Text("설정 열기", fontSize = 16.sp, fontFamily = Pretendard, color = Color.White) }
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

/** ✅ Preview */
@Preview(showBackground = true)
@Composable
fun PreviewLogoutAlert() {
    LogoutAlert.Show(
        onConfirm = {},
        onDismiss = {}
    )
}
