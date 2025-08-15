package com.malmungchi.feature.study.first


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malmungchi.feature.study.Pretendard
import com.malmungchi.feature.study.R
import kotlinx.coroutines.delay

@Composable
fun StudyIntroScreen(
    onStart: () -> Unit,
    onNavigateNext: () -> Unit // 기본값 제거
    //levelText: String = "1단계",
    //onNavigateNext: () -> Unit = {} // ✅ 콜백으로 다음 화면 이동 처리
) {
    // ✅ 최신 콜백을 항상 참조
    val next by rememberUpdatedState(onNavigateNext)

    // 3초 후 자동 이동
//    LaunchedEffect(Unit) {
//        kotlinx.coroutines.delay(3000)
//        android.util.Log.d("NAV", ">> study_intro 타이머 끝, onNavigateNext 호출")
//        onNext() // ✅ stale 캡처 방지
//    }
    LaunchedEffect(Unit) {
        android.util.Log.d("NAV", ">> study_intro 타이머 시작")
        delay(1500)

        android.util.Log.d("NAV", ">> study_intro 타이머 끝, next() 호출 직전")
        next()                            // ✅ 진짜 호출 (괄호 필수!)
        android.util.Log.d("NAV", ">> study_intro next() 호출 완료")
    }

    LaunchedEffect(Unit) { android.util.Log.d("NAV", ">> study_intro 진입") }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "1단계",
                color = Color(0xFF3F51B5),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Pretendard
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "글을 집중해서 읽으며,\n모르는 단어에 체크해 보세요!",
                color = Color(0xFF333333),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = Pretendard,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun PreviewStudyIntroScreen() {
//    StudyIntroScreen(levelText = "1단계")
//}
