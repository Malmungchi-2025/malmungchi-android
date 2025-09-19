package com.malmungchi.feature.mypage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.mypage.R as MyPageR
import kotlinx.coroutines.flow.collectLatest

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
    navigateToLogin: () -> Unit = {},              // ★ 로그아웃 후 이동 콜백
    viewModel: SettingsViewModel = hiltViewModel() // ★ VM 주입
) {
    var pushEnabled by remember { mutableStateOf(true) }
    var showLogout by remember { mutableStateOf(false) }       // ★ 상태 추가

    // ★ VM 이벤트 수신 → 로그인 화면 이동
    LaunchedEffect(Unit) {
        viewModel.navigateLogin.collectLatest { navigateToLogin() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = ScreenPadding)
    ) {
        Spacer(Modifier.height(48.dp))
        SettingsTopBar(
            title = "설정",
            onBack = onClickBack
        )
        Spacer(Modifier.height(12.dp))

        // 항목 리스트
        SettingsItemPush(
            checked = pushEnabled,
            onCheckedChange = { pushEnabled = it }
        )

        Divider(Modifier.fillMaxWidth(), color = Color(0xFFF2F2F2))
        SettingsChevronItem(
            text = "리마인드 알림",
            onClick = onClickRemind
        )

        Divider(Modifier.fillMaxWidth(), color = Color(0xFFF2F2F2))
        SettingsPlainItem(
            text = "로그아웃",
            onClick = { showLogout = true }         // ★ 다이얼로그 오픈
        )

        Divider(Modifier.fillMaxWidth(), color = Color(0xFFF2F2F2))
        SettingsPlainItem(
            text = "회원 탈퇴",
            onClick = onClickWithdraw
        )
    }

    // ★ 로그아웃 다이얼로그
    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            title = { Text("로그아웃") },
            text  = { Text("정말 로그아웃하시겠어요?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogout = false
                    viewModel.logout()              // ★ 실제 로그아웃
                }) { Text("로그아웃") }
            },
            dismissButton = {
                TextButton(onClick = { showLogout = false }) { Text("취소") }
            }
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


//package com.malmungchi.feature.mypage
//
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Divider
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Switch
//import androidx.compose.material3.SwitchDefaults
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.malmungchi.core.designsystem.Pretendard
//import com.malmungchi.feature.mypage.R as MyPageR
//
//// ===== Colors (피그마 스펙) =====
//private val Blue_195FCF = Color(0xFF195FCF)
//private val Gray_C9CAD4 = Color(0xFFC9CAD4)
//private val TextDefault = Color(0xFF262626)
//
//private val ScreenPadding = 20.dp
//
//@Composable
//fun SettingsScreen(
//    modifier: Modifier = Modifier,
//    onClickBack: () -> Unit = {},
//    onClickRemind: () -> Unit = {},     // "리마인드 알림" > 상세로 갈 때 사용
//    onClickLogout: () -> Unit = {},
//    onClickWithdraw: () -> Unit = {}
//) {
//    var pushEnabled by remember { mutableStateOf(true) } // 스위치 상태 (샘플)
//
//    Column(
//        modifier = modifier
//            .fillMaxSize()
//            .padding(horizontal = ScreenPadding)
//    ) {
//        Spacer(Modifier.height(48.dp))   // ← 마이페이지와 동일한 시작 위치
//        SettingsTopBar(
//            title = "설정",
//            onBack = onClickBack
//        )
//
//        Spacer(Modifier.height(12.dp))
//
//        // 항목 리스트
//        SettingsItemPush(
//            checked = pushEnabled,
//            onCheckedChange = { pushEnabled = it }
//        )
//
//        Divider(Modifier.fillMaxWidth(), color = Color(0xFFF2F2F2))
//        SettingsChevronItem(
//            text = "리마인드 알림",
//            onClick = onClickRemind
//        )
//        Divider(Modifier.fillMaxWidth(), color = Color(0xFFF2F2F2))
//        SettingsPlainItem(
//            text = "로그아웃",
//            onClick = onClickLogout
//        )
//        Divider(Modifier.fillMaxWidth(), color = Color(0xFFF2F2F2))
//        SettingsPlainItem(
//            text = "회원 탈퇴",
//            onClick = onClickWithdraw
//        )
//    }
//}
//
//@Composable
//private fun SettingsTopBar(
//    title: String,
//    onBack: () -> Unit
//) {
//    Box(
//        modifier = Modifier.fillMaxWidth(),
//        contentAlignment = Alignment.Center
//    ) {
//        // 타이틀 (Pretendard 20 SemiBold / 가운데 정렬)
//        Text(
//            text = title,
//            modifier = Modifier.fillMaxWidth(),
//            textAlign = TextAlign.Center,
//            style = TextStyle(
//                fontFamily = Pretendard,
//                fontSize = 20.sp,
//                fontWeight = FontWeight.SemiBold,
//                color = Color.Black
//            )
//        )
//        // 좌측 뒤로가기
//        IconButton(
//            modifier = Modifier.align(Alignment.CenterStart),
//            onClick = onBack
//        ) {
//            Icon(
//                painter = painterResource(id = MyPageR.drawable.ic_back),
//                contentDescription = "뒤로가기",
//                tint = MaterialTheme.colorScheme.onBackground
//            )
//        }
//    }
//}
//
//@Composable
//private fun SettingsItemPush(
//    checked: Boolean,
//    onCheckedChange: (Boolean) -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .heightIn(min = 56.dp)
//            .padding(vertical = 10.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(
//            text = "푸시 알림",
//            style = TextStyle(
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.Medium,
//                fontSize = 16.sp,
//                color = Color.Black
//            ),
//            modifier = Modifier.weight(1f)
//        )
//        Switch(
//            checked = checked,
//            onCheckedChange = onCheckedChange,
//            colors = SwitchDefaults.colors(
//                checkedThumbColor = Color.White,
//                checkedTrackColor = Blue_195FCF,
//                uncheckedThumbColor = Color.White,
//                uncheckedTrackColor = Gray_C9CAD4
//            )
//        )
//    }
//}
//
//@Composable
//private fun SettingsChevronItem(
//    text: String,
//    onClick: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .heightIn(min = 56.dp)
//            .clickable { onClick() }
//            .padding(vertical = 10.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(
//            text = text,
//            style = TextStyle(
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.Medium,
//                fontSize = 16.sp,
//                color = Color.Black
//            ),
//            modifier = Modifier.weight(1f)
//        )
//        Icon(
//            painter = painterResource(id = MyPageR.drawable.ic_right),
//            contentDescription = null,
//            tint = Color(0xFF9E9E9E)
//        )
//    }
//}
//
//@Composable
//private fun SettingsPlainItem(
//    text: String,
//    onClick: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .heightIn(min = 56.dp)
//            .clickable { onClick() }
//            .padding(vertical = 10.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(
//            text = text,
//            style = TextStyle(
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.Medium,
//                fontSize = 16.sp,
//                color = Color.Black
//            )
//        )
//    }
//}
//
//@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
//@Composable
//private fun SettingsScreenPreview() {
//    MaterialTheme {
//        SettingsScreen()
//    }
//}