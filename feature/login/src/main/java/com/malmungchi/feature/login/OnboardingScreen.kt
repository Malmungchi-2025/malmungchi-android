package com.malmungchi.feature.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.ExperimentalFoundationApi
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.login.R

// ── Colors ─────────────────────────────────────────────────────────────────────
private val OnboardingBlue = Color(0xFF195FCF)  // 선택
private val IndicatorIdle  = Color(0xFFC9CAD4)  // 미선택
private val TextGray       = Color(0xFF989898)  // 보조문구

// ── Layout Spec ────────────────────────────────────────────────────────────────
private val TopPadding       = 64.dp
private val BottomPadding    = 48.dp
private val HorizontalPad    = 20.dp
private val IndicatorGap     = 24.dp   // 인디케이터 ↔ 타이틀 간격
private val TitleBetweenGap  = 6.dp
private val TitleSubGap      = 14.dp
private val SubImageGap      = 20.dp
private val ButtonTopGap     = 16.dp   // 이미지 ↔ 버튼 간격

// ── Typography ─────────────────────────────────────────────────────────────────
private fun titleStyle() = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.SemiBold,
    fontSize   = 22.sp,
    color      = Color.Black,
    textAlign  = TextAlign.Center,
    lineHeight = 22.sp * 1.5f,
)

private fun bodyGrayStyle() = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.SemiBold,
    fontSize   = 14.sp,
    color      = TextGray,
    textAlign  = TextAlign.Center,
    lineHeight = 14.sp * 1.5f,
)

private fun buttonTextStyle() = TextStyle(
    fontFamily = Pretendard,
    fontWeight = FontWeight.SemiBold,
    fontSize   = 18.sp,
    color      = Color.White,
    textAlign  = TextAlign.Center,
)

// ── Model ──────────────────────────────────────────────────────────────────────
private data class OnboardingPage(
    val titleTop: String,
    val titleBottom: String? = null,
    val subGray: String? = null,
    val imageRes: Int,
    val isLast: Boolean = false,
    val buttonLabel: String = if (isLast) "시작하기" else "건너뛰기",
)

// ── Pages ──────────────────────────────────────────────────────────────────────
private val pages: List<OnboardingPage> = listOf(
    OnboardingPage(
        titleTop    = "모르는 단어를 체크하며 지문을 읽고,",
        titleBottom = "필사하며 문제까지 한 묶음!",
        subGray     = "기초, 활용, 실전, 고급 네 단계로 문해력과 어휘력을 함께 키워요.",
        imageRes    = R.drawable.img_onboarding_new1,
        buttonLabel = "건너뛰기",
    ),
    OnboardingPage(
        titleTop    = "매일 7개의 어휘 퀴지로 학습을 재밌게!",
        titleBottom = "직접 단계를 선택하여 어휘력을 쌓아보세요.",
        imageRes    = R.drawable.img_onboarding_new2,
        buttonLabel = "건너뛰기",
    ),
    OnboardingPage(
        titleTop    = "AI와 함께하는 실전 연습!",
        titleBottom = "사회초년생이 접하게 될 상황을 연습해봐요.",
        imageRes    = R.drawable.img_onboarding_new3,
        buttonLabel = "건너뛰기",
    ),
    OnboardingPage(
        titleTop    = "심화 학습을 원한다면?",
        titleBottom = "웹에서도 말뭉치를 이용해보세요.",
        subGray     = "웹에서는 내가 원하는 글과 고전문학으로 필사를 진행하고,\n배운 어휘로 나만의 글을 창작해요.",
        imageRes    = R.drawable.img_onboarding_new4, // 리소스명 확인
        isLast      = true,
        buttonLabel = "시작하기",
    )
)

// ── Pager + Screen ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = HorizontalPad)
            .padding(top = TopPadding, bottom = BottomPadding),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 인디케이터
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            pages.indices.forEach { i ->
                val selected = pagerState.currentPage == i
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(6.dp)
                        .width(if (selected) 18.dp else 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (selected) OnboardingBlue else IndicatorIdle)
                )
            }
        }

        Spacer(Modifier.height(IndicatorGap)) // 인디케이터 ↔ 텍스트 간격

        // 본문 (텍스트 + 이미지 + 버튼)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            val p = pages[page]
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 제목
                Text(p.titleTop, style = titleStyle(), modifier = Modifier.fillMaxWidth())
                p.titleBottom?.let {
                    Spacer(Modifier.height(TitleBetweenGap))
                    Text(it, style = titleStyle(), modifier = Modifier.fillMaxWidth())
                }
                // 보조문구
                p.subGray?.let {
                    Spacer(Modifier.height(TitleSubGap))
                    Text(it, style = bodyGrayStyle(), modifier = Modifier.fillMaxWidth())
                }

                Spacer(Modifier.height(SubImageGap + 2.dp))

                // 이미지 (윗부분 고정)
                Image(
                    painter = painterResource(id = p.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp)
                        .weight(1f),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter   // ★ 윗부분 절대 안잘림
                )

                Spacer(Modifier.height(ButtonTopGap))

                // 버튼 — 이미지 아래
                Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OnboardingBlue,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    val isLast = p.isLast
                    Text(text = if (isLast) "시작하기" else "건너뛰기", style = buttonTextStyle())
                }
            }
        }
    }
}

// ── Single Page Previews ───────────────────────────────────────────────────────
@Composable
private fun OnboardingPagePreviewContainer(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = HorizontalPad)
            .padding(top = TopPadding, bottom = BottomPadding),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // fake indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            pages.indices.forEach { i ->
                val selected = pages.indexOf(page) == i
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(6.dp)
                        .width(if (selected) 18.dp else 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (selected) OnboardingBlue else IndicatorIdle)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // 내용 (텍스트 → 이미지 → 버튼)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(16.dp))
            Text(page.titleTop, style = titleStyle(), modifier = Modifier.fillMaxWidth())
            page.titleBottom?.let {
                Spacer(Modifier.height(TitleBetweenGap))
                Text(it, style = titleStyle(), modifier = Modifier.fillMaxWidth())
            }
            page.subGray?.let {
                Spacer(Modifier.height(TitleSubGap))
                Text(it, style = bodyGrayStyle(), modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(SubImageGap +16.dp))

            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 240.dp)
                    .weight(1f),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter    // ★ 추가: Preview도 동일하게
            )

            Spacer(Modifier.height(ButtonTopGap))

            // 버튼 — 이미지 아래
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = -20.dp) // ✅ Column의 좌우 padding(=HorizontalPad 20dp)을 상쇄
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()   // ✅ 가로 꽉 채움 (좌우 여백 제거)
                        .height(52.dp),   // ✅ 세로 그대로 유지
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OnboardingBlue,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        text = page.buttonLabel,
                        style = buttonTextStyle()
                    )
                }
            }
        }
    }
}

// ── Previews ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingScreenPreview_All() {
    OnboardingScreen(onFinish = {})
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingPreview_Page1() { OnboardingPagePreviewContainer(pages[0]) }

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingPreview_Page2() { OnboardingPagePreviewContainer(pages[1]) }

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingPreview_Page3() { OnboardingPagePreviewContainer(pages[2]) }

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingPreview_Page4() { OnboardingPagePreviewContainer(pages[3]) }

//package com.malmungchi.feature.login
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.tooling.preview.Preview
//import kotlinx.coroutines.delay
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.Image
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.unit.dp
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.sp
//import com.malmungchi.core.designsystem.Pretendard
//
//
//// 색상 상수: 195FCF
//private val OnboardingBlue = Color(0xFF195FCF)
//
//// 버튼 텍스트 스타일 (회원가입 플로우와 동일 톤)
//private fun onboardingButtonText() = TextStyle(
//    fontFamily = Pretendard,     // ← 디자인 시스템 폰트
//    fontSize = 16.sp,
//    fontWeight = FontWeight.SemiBold,
//    color = Color.White
//)
//
//@Composable
//fun OnboardingScreen(
//    onFinish: () -> Unit,
//    autoAdvanceMillis: Long = 1500L,
//) {
//    val images = remember {
//        listOf(
//            R.drawable.img_onboard1,
//            R.drawable.img_onboard2,
//            R.drawable.img_onboard3,
//            R.drawable.img_onboard4,
//            R.drawable.img_onboard5,
//            R.drawable.img_onboard6,
//            R.drawable.img_onboard7,
//            R.drawable.img_onboard8,
//            R.drawable.img_onboard9,
//        )
//    }
//
//
//
////    var index by remember { mutableStateOf(0) }
////    var isSkipping by remember { mutableStateOf(false) }   // ★ 추가
////
////    LaunchedEffect(index) {
////        if (index < images.lastIndex) {
////            delay(autoAdvanceMillis)
////            index += 1
////        } else {
////            delay(autoAdvanceMillis)
////            onFinish()
////        }
////    }
//    var index by remember { mutableStateOf(0) }
//    var isSkipping by remember { mutableStateOf(false) }
//
//    // 자동 진행: skip 중이면 즉시 중단
//    LaunchedEffect(index, isSkipping) {
//        if (isSkipping) return@LaunchedEffect
//        if (index < images.lastIndex) {
//            delay(autoAdvanceMillis)
//            if (!isSkipping) index += 1
//        } else {
//            delay(autoAdvanceMillis)
//            if (!isSkipping) onFinish()
//        }
//    }
//
//
//    // 탭 진행: 라벨(next@)을 붙여 조기 리턴
//    val onNext: () -> Unit = next@{
//        if (isSkipping) return@next
//        if (index < images.lastIndex) index += 1 else onFinish()
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            // ripple 제거 (원치 않으면 빼도 됨)
//            .clickable(
//                interactionSource = remember { MutableInteractionSource() },
//                indication = null
//            ) { onNext() },
//        contentAlignment = Alignment.Center
//    ) {
//        Image(
//            painter = painterResource(id = images[index]),
//            contentDescription = null,
//            modifier = Modifier.fillMaxSize(),
//            // ✅ 화면을 빈틈 없이 꽉 채움 (비율 유지, 가장자리 약간 크롭)
//            contentScale = ContentScale.Crop
//        )
//        // 👉 Skip 버튼 (OTP "인증 완료" 버튼과 동일 스타일)
//        Button(
//            onClick = {
//                isSkipping = true                   // ★ 자동/탭 진행 모두 차단
//                onFinish()                          // ★ 곧바로 다음 화면으로
//            },
//            enabled = !isSkipping, //중복 클릭 방지
//            modifier = Modifier
//                .align(Alignment.TopEnd)
//                .padding(end = 20.dp)     // 오른쪽 여백만
//                .offset(y = 640.dp)
//                .height(52.dp)
//                .defaultMinSize(minWidth = 100.dp),
//            shape = RoundedCornerShape(14.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = OnboardingBlue,   // ← 교체
//                contentColor = Color.White
//            ),
//            elevation = ButtonDefaults.buttonElevation(0.dp)
//        ) {
//            Text("Skip", style = onboardingButtonText()) // ← 교체
//        }
//    }
//}
//
//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//private fun OnboardingScreenPreview() {
//    OnboardingScreen(onFinish = {})
//}