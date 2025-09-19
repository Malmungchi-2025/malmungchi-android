package com.malmungchi.feature.ai

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.ai.R

/* ─────────────────────────────────────────────
 * 1) VM 래퍼: 상태만 뽑아서 Content에 전달
 * ───────────────────────────────────────────── */
@Composable
fun AiChatCompleteScreen(
    viewModel: ChatViewModel,
    onFinishNavigate: () -> Unit = {},
    snackbarHostState: SnackbarHostState? = null
) {
    val loading by viewModel.rewardLoading.collectAsStateWithLifecycle()
    val toastMsg by viewModel.rewardToast.collectAsStateWithLifecycle()

    // 스낵바 표시
    LaunchedEffect(toastMsg) {
        toastMsg?.let { msg ->
            snackbarHostState?.showSnackbar(message = msg)
            viewModel.consumeRewardToast()
        }
    }

    AiChatCompleteContent(
        loading = loading,
        onClickFinish = { viewModel.giveAiChatRewardAndFinish(onNavigateFinish = onFinishNavigate) }
    )
}

/* ─────────────────────────────────────────────
 * 2) 실제 UI (프리뷰에서 이 함수만 호출)
 *    버튼은 바닥에서 48dp 위에 고정
 * ───────────────────────────────────────────── */
@Composable
private fun AiChatCompleteContent(
    loading: Boolean,
    onClickFinish: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        // 본문(스크롤)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 48.dp, bottom = 120.dp), // 버튼 자리 확보
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AI 대화 완료!",
                fontSize = 24.sp,
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF195FCF),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_complete_character),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 300.dp)
                    .aspectRatio(1f, matchHeightConstraintsFirst = true),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = "15XP 획득",
                fontSize = 22.sp,
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }

        // 하단 버튼: 바닥에서 48dp 위 고정
        Button(
            onClick = onClickFinish,
            enabled = true,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .fillMaxWidth(0.5f)
                .height(48.dp)
        ) {
            Text(
                text = if (loading) "지급 중..." else "종료하기",
                fontSize = 16.sp,
                fontFamily = Pretendard,
                color = Color.White
            )
        }
    }
}

/* ─────────────────────────────────────────────
 * 3) Preview: VM 없이 Content만 호출
 * ───────────────────────────────────────────── */
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF, widthDp = 360, heightDp = 760)
@Composable
fun Preview_AiChatCompleteScreen() {
    AiChatCompleteContent(
        loading = false,
        onClickFinish = { /* no-op in preview */ }
    )
}

//package com.malmungchi.feature.ai
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.malmungchi.core.designsystem.Pretendard
//import com.malmungchi.feature.ai.R
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
//// DI에 맞게 교체: hiltViewModel() / koinViewModel() / 전달받기
//// import androidx.hilt.navigation.compose.hiltViewModel
//// import org.koin.androidx.compose.koinViewModel
//
//@Composable
//fun AiChatCompleteScreen(
//    viewModel: ChatViewModel,            // ✅ ViewModel 주입받기 (DI/Hilt/Koin/호출측에서 전달)
//    onFinishNavigate: () -> Unit = {},   // 보상 처리 후 이동 (AiScreen 등)
//    snackbarHostState: SnackbarHostState? = null // 선택: 스낵바 출력용
//) {
//    val loading by viewModel.rewardLoading.collectAsStateWithLifecycle()
//    val toastMsg by viewModel.rewardToast.collectAsStateWithLifecycle()
//
//    // 토스트/스낵바 출력
//    LaunchedEffect(toastMsg) {
//        toastMsg?.let { msg ->
//            // 스낵바가 있으면 사용, 없으면 그냥 무시 or 다른 토스트 시스템 사용
//            snackbarHostState?.showSnackbar(message = msg)
//            viewModel.consumeRewardToast()
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//            .padding(horizontal = 16.dp, vertical = 32.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.SpaceBetween
//    ) {
//        Spacer(Modifier.height(64.dp))
//
//        Text(
//            text = "AI 대화 완료!",
//            fontSize = 24.sp,
//            fontFamily = Pretendard,
//            fontWeight = FontWeight.SemiBold,
//            color = Color(0xFF195FCF),
//            textAlign = TextAlign.Center
//        )
//
//        Column(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Image(
//                painter = painterResource(id = R.drawable.ic_complete_character),
//                contentDescription = null,
//                modifier = Modifier.size(300.dp),
//                contentScale = ContentScale.Fit
//            )
//
//            Spacer(Modifier.height(24.dp))
//
//            Text(
//                text = "15XP 획득",
//                fontSize = 22.sp,
//                fontFamily = Pretendard,
//                fontWeight = FontWeight.SemiBold,
//                color = Color.Black,
//                textAlign = TextAlign.Center
//            )
//        }
//
//        Button(
//            onClick = {
//                // ✅ 여기서 보상 API 호출 + 네비게이션 진행
//                viewModel.giveAiChatRewardAndFinish(onNavigateFinish = onFinishNavigate)
//            },
//            enabled = !loading,
//            shape = RoundedCornerShape(50),
//            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
//            modifier = Modifier
//                .fillMaxWidth(0.5f)
//                .height(48.dp)
//                .padding(bottom = 48.dp)
//        ) {
//            Text(
//                text = if (loading) "지급 중..." else "종료하기",
//                fontSize = 16.sp,
//                fontFamily = Pretendard,
//                color = Color.White
//            )
//        }
//    }
//}
//
//
//
////package com.malmungchi.feature.ai
////
////import androidx.compose.foundation.Image
////import androidx.compose.foundation.background
////import androidx.compose.foundation.layout.*
////import androidx.compose.foundation.shape.RoundedCornerShape
////import androidx.compose.material3.Button
////import androidx.compose.material3.ButtonDefaults
////import androidx.compose.material3.Text
////import androidx.compose.runtime.Composable
////import androidx.compose.ui.Alignment
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.graphics.Color
////import androidx.compose.ui.layout.ContentScale
////import androidx.compose.ui.res.painterResource
////import androidx.compose.ui.text.font.FontWeight
////import androidx.compose.ui.text.style.TextAlign
////import androidx.compose.ui.tooling.preview.Preview
////import androidx.compose.ui.unit.dp
////import androidx.compose.ui.unit.sp
////import com.malmungchi.core.designsystem.Pretendard
////import com.malmungchi.feature.ai.R
////
////@Composable
////fun AiChatCompleteScreen(
////    onFinishClick: () -> Unit = {}     // “종료하기” 눌렀을 때 AiScreen으로 이동
////) {
////    Column(
////        modifier = Modifier
////            .fillMaxSize()
////            .background(Color.White)
////            .padding(horizontal = 16.dp, vertical = 32.dp),
////        horizontalAlignment = Alignment.CenterHorizontally,
////        verticalArrangement = Arrangement.SpaceBetween
////    ) {
////        Spacer(Modifier.height(64.dp))
////
////        Text(
////            text = "AI 대화 완료!",
////            fontSize = 24.sp,
////            fontFamily = Pretendard,
////            fontWeight = FontWeight.SemiBold,
////            color = Color(0xFF195FCF),
////            textAlign = TextAlign.Center
////        )
////
////        Column(
////            modifier = Modifier.fillMaxWidth(),
////            horizontalAlignment = Alignment.CenterHorizontally,
////            verticalArrangement = Arrangement.Center
////        ) {
////            Image(
////                painter = painterResource(id = R.drawable.ic_complete_character), // 캐릭터 리소스
////                contentDescription = null,
////                modifier = Modifier.size(300.dp),
////                contentScale = ContentScale.Fit
////            )
////
////            Spacer(Modifier.height(24.dp))
////
////            Text(
////                text = "15XP 획득",
////                fontSize = 22.sp,
////                fontFamily = Pretendard,
////                fontWeight = FontWeight.SemiBold,
////                color = Color.Black,
////                textAlign = TextAlign.Center
////            )
////        }
////
////        Button(
////            onClick = onFinishClick,
////            shape = RoundedCornerShape(50),
////            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF195FCF)),
////            modifier = Modifier
////                .fillMaxWidth(0.5f)
////                .height(48.dp)
////                .padding(bottom = 48.dp)
////        ) {
////            Text(
////                text = "종료하기",
////                fontSize = 16.sp,
////                fontFamily = Pretendard,
////                color = Color.White
////            )
////        }
////    }
////}
////
////@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
////@Composable
////private fun Preview_AiChatCompleteScreen() {
////    AiChatCompleteScreen()
////}