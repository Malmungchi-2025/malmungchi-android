package com.malmungchi.feature.mypage


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.core.model.VocabularyDto
import com.malmungchi.feature.mypage.R as MyPageR
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.tooling.preview.Preview
import com.malmungchi.feature.login.AvatarSelectDialog
import com.malmungchi.feature.mypage.nickname.NicknameCardDialog


//사용자 프로필 이미지
@androidx.annotation.DrawableRes
private fun avatarNameToRes(context: android.content.Context, name: String): Int {
    // 화이트리스트 매핑이 가장 안전/빠름
    return when (name) {
        "img_glass_malchi"  -> MyPageR.drawable.img_glass_malchi
        "img_malchi"        -> MyPageR.drawable.img_malchi
        "img_mungchi"       -> MyPageR.drawable.img_mungchi
        "img_glass_mungchi" -> MyPageR.drawable.img_glass_mungchi
        else                -> MyPageR.drawable.img_malchi // fallback
    }
    // ※ 만약 리소스명이 종종 바뀐다면 getIdentifier로 유연하게:
    // val id = context.resources.getIdentifier(name, "drawable", context.packageName)
    // return if (id != 0) id else MyPageR.drawable.img_malchi
}


// ===== Color & Dimens =====
private val Blue_195FCF = Color(0xFF195FCF)
private val Bg_EFF4FB = Color(0xFFEFF4FB)
private val Gray_616161 = Color(0xFF616161)
private val ScreenPadding = 20.dp
private val SectionGap = 24.dp
private val CardCorner = 16.dp

// ===== Entry: ViewModel 연동 버전 =====
@Composable
fun MyPageRoute(
    viewModel: MyPageViewModel = hiltViewModel(),
    onClickSettings: () -> Unit = {},
    onClickViewAllWords: () -> Unit = {},
    onClickViewAllBadges: () -> Unit = {},
    onClickViewNicknameTest: () -> Unit = {},
    onClickViewNicknameCard: (nicknameTitle: String, userName: String) -> Unit = { _, _ -> }
) {
    val ui by viewModel.ui.collectAsState()

    var showAvatarDialog by rememberSaveable { mutableStateOf(false) }

    // ✅ 최초 1회만 데이터 로드 (중복 방지)
    LaunchedEffect(viewModel) {
        viewModel.loadIfNeeded()
    }
//    LaunchedEffect(Unit) {
//        viewModel.loadIfNeeded()
//    }

    // ✅ 최근 단어 인덱스 관리
    val pageCount = ui.recentVocab.size
    var recentIndex by rememberSaveable(pageCount) { mutableStateOf(0) }
    if (recentIndex >= pageCount) recentIndex = (pageCount - 1).coerceAtLeast(0)

    // ✅ 로딩 중에도 이전 UI 유지하기
    // → remember로 마지막 정상 상태 저장
    var lastNonEmptyUi by remember { mutableStateOf<MyPageUiState?>(null) }
    if (ui.user != null) lastNonEmptyUi = ui
    val displayUi = lastNonEmptyUi ?: ui

    // ✅ 상태별 처리
    when {
        ui.error != null -> {
            Box(
                Modifier.fillMaxSize().background(Color.White),
                contentAlignment = Alignment.Center
            ) { Text("에러: ${ui.error}") }
        }

        //ui.loading && displayUi.user == null -> {
        ui.loading && displayUi.user == null && lastNonEmptyUi == null -> {
            // 데이터가 전혀 없는 첫 로딩 상태에서만 로딩 표시
            Box(
                Modifier.fillMaxSize().background(Color.White),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = Blue_195FCF) }
        }

        else -> {
            // ✅ 닉네임 카드 다이얼로그 열림 여부
            var showNicknameCard by rememberSaveable { mutableStateOf(false) }

            val nicknameTitle = displayUi.user?.nickname_title
            val context = androidx.compose.ui.platform.LocalContext.current
            val avatarRes = remember(displayUi.avatarName) {
                avatarNameToRes(context, displayUi.avatarName)
            }

            // ✅ 마이페이지 메인 화면 표시
            MyPageScreen(
                userName = displayUi.userName,
                levelLabel = displayUi.levelLabel,
                nextStage = displayUi.nextStageUi,
                onClickSettings = onClickSettings,
                onClickViewAllWords = onClickViewAllWords,
                onClickViewAllBadges = onClickViewAllBadges,
                onClickNickname = {
                    if (!nicknameTitle.isNullOrBlank()) {
                        showNicknameCard = true
                    } else {
                        onClickViewNicknameTest()
                    }
                },
                profileIconRes = avatarRes,
                recentItems = displayUi.recentVocab,
                currentRecentIndex = recentIndex,
                onChangeRecentIndex = { recentIndex = it },
                onClickChangeAvatar = { showAvatarDialog = true }
            )

            // ✅ 닉네임 카드 다이얼로그
            if (showNicknameCard) {
                NicknameCardDialog(
                    nickname = nicknameTitle,
                    onExit = { showNicknameCard = false },
                    onSaveImage = { _ -> showNicknameCard = false }
                )
            }

            // ✅ 아바타 선택 다이얼로그
            if (showAvatarDialog) {
                AvatarSelectDialog(
                    name = displayUi.userName,
                    onConfirm = { selected ->
                        viewModel.updateAvatar(selected)
                        showAvatarDialog = false
                    },
                    onDismiss = { showAvatarDialog = false }
                )
            }
        }
    }
}
//@Composable
//fun MyPageRoute(
//    viewModel: MyPageViewModel = hiltViewModel(),
//    onClickSettings: () -> Unit = {},
//    onClickViewAllWords: () -> Unit = {},
//    onClickViewAllBadges: () -> Unit = {},
//    onClickViewNicknameTest: () -> Unit = {},
//    onClickViewNicknameCard: (nicknameTitle: String, userName: String) -> Unit = { _, _ -> } // 별명 카드로 이동하는 콜백
//) {
//    val ui by viewModel.ui.collectAsState()
//
//    var showAvatarDialog by rememberSaveable { mutableStateOf(false) }
//
//    // ✅ 1️⃣ 최초 1회만 데이터 로드 (중복 방지)
//    LaunchedEffect(Unit) {
//        viewModel.loadIfNeeded()
//    }
//
//    // ✅ 2️⃣ 최근 단어 인덱스 관리
//    val pageCount = ui.recentVocab.size
//    var recentIndex by rememberSaveable(pageCount) { mutableStateOf(0) }
//    if (recentIndex >= pageCount) recentIndex = (pageCount - 1).coerceAtLeast(0)
//
//    // ✅ 3️⃣ 상태별 처리
//    if (ui.error != null) {
//        Box(
//            Modifier.fillMaxSize().background(Color.White),
//            contentAlignment = Alignment.Center
//        ) {
//            Text("에러: ${ui.error}")
//        }
//    } else {
//        // ✅ 닉네임 카드 다이얼로그 열림 여부
//        var showNicknameCard by rememberSaveable { mutableStateOf(false) }
//
//        val nicknameTitle = ui.user?.nickname_title
//        val context = androidx.compose.ui.platform.LocalContext.current
//        val avatarRes = remember(ui.avatarName) {
//            avatarNameToRes(context, ui.avatarName)
//        }
//
//        // ✅ 마이페이지 메인 화면 표시
//        MyPageScreen(
//            userName = ui.userName,
//            levelLabel = ui.levelLabel,
//            nextStage = ui.nextStageUi,
//            onClickSettings = onClickSettings,
//            onClickViewAllWords = onClickViewAllWords,
//            onClickViewAllBadges = onClickViewAllBadges,
//            onClickNickname = {
//                if (!nicknameTitle.isNullOrBlank()) {
//                    showNicknameCard = true
//                } else {
//                    onClickViewNicknameTest()
//                }
//            },
//            profileIconRes = avatarRes,
//            recentItems = ui.recentVocab,
//            currentRecentIndex = recentIndex,
//            onChangeRecentIndex = { recentIndex = it },
//            onClickChangeAvatar = { showAvatarDialog = true } // ✅ 아바타 클릭 시 다이얼로그 열기
//        )
//
//        // ✅ 닉네임 카드 다이얼로그 (마이페이지 위 오버레이)
//        if (showNicknameCard) {
//            NicknameCardDialog(
//                nickname = nicknameTitle, // ex) "언어연금술사"
//                onExit = { showNicknameCard = false },
//                onSaveImage = { nick ->
//                    // TODO: 저장 구현 (원래 쓰던 로직 연결)
//                    // ex) viewModel.saveCardImage(nick)
//                    showNicknameCard = false
//                }
//            )
//
//        }
//
//        // ✅ 아바타 선택 다이얼로그
//        if (showAvatarDialog) {
//            AvatarSelectDialog(
//                name = ui.userName,
//                onConfirm = { selected ->
//                    viewModel.updateAvatar(selected) // ✅ 서버 + UI 즉시 반영
//                    showAvatarDialog = false
//                },
//                onDismiss = { showAvatarDialog = false }
//            )
//        }
//    }
//}
//@Composable
//fun MyPageRoute(
//    viewModel: MyPageViewModel = hiltViewModel(),
//    onClickSettings: () -> Unit = {},
//    onClickViewAllWords: () -> Unit = {},
//    onClickViewAllBadges: () -> Unit = {},
//    onClickViewNicknameTest: () -> Unit = {},
//    onClickViewNicknameCard: (nicknameTitle: String, userName: String) -> Unit = { _, _ -> }  // 별명 카드로 이동하는 콜백 추가
//) {
//    val ui by viewModel.ui.collectAsState()
//
//    LaunchedEffect(Unit) { viewModel.load() }
//
//    // 최근 단어 5개 인덱스
//    val pageCount = ui.recentVocab.size
//    var recentIndex by rememberSaveable(pageCount) { mutableStateOf(0) }
//    if (recentIndex >= pageCount) recentIndex = (pageCount - 1).coerceAtLeast(0)
//
//    when {
//        ui.loading -> Box(
//            Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
//            Text("불러오는 중…")
//        }
//        ui.error != null -> Box(Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
//            Text("에러: ${ui.error}")
//        }
//        else -> {
//            // ✅ 다이얼로그 열림 상태
//            var showNicknameCard by rememberSaveable { mutableStateOf(false) }
//            // ✅ 유저 닉네임 타이틀(없을 수 있음)
//            val nicknameTitle = ui.user?.nickname_title
//
//            val context = androidx.compose.ui.platform.LocalContext.current
//            val avatarRes = remember(ui.avatarName) {
//                avatarNameToRes(context, ui.avatarName)
//            }
//
//
//            MyPageScreen(
//                userName = ui.userName,
//                levelLabel = ui.levelLabel,
//                nextStage = ui.nextStageUi,
//                onClickSettings = onClickSettings,
//                onClickViewAllWords = onClickViewAllWords,
//                onClickViewAllBadges = onClickViewAllBadges,
//
//                // ✅ 말풍선(치치의 어휘/문해력은?) 클릭 → 다이얼로그 오픈
//                onClickNickname = {
//                    // 닉네임이 있을 때만 카드 오픈, 없으면 테스트로
//                    if (!nicknameTitle.isNullOrBlank()) {
//                        showNicknameCard = true
//                    } else {
//                        onClickViewNicknameTest()
//                    }
//                },
//
//                // ✅ 프로필 아이콘에 실제 사용자 아바타 전달
//                profileIconRes = avatarRes,
//
//                recentItems = ui.recentVocab,
//                currentRecentIndex = recentIndex,
//                onChangeRecentIndex = { recentIndex = it }
//            )
//
//            // ✅ 다이얼로그 표시(마이페이지 위 오버레이)
//            if (showNicknameCard) {
//                NicknameCardDialog(
//                    nickname = nicknameTitle, // ex) "언어연금술사"
//                    onExit = { showNicknameCard = false }, // 닫기(스크림 탭/백 포함)
//                    onSaveImage = { nick ->
//                        // TODO: 저장 구현(원래 쓰던 로직 연결)
//                        // ex) viewModel.saveCardImage(nick)
//                        showNicknameCard = false
//                    }
//                )
//            }
//        }
//    }
//}
//        else -> {
//            // 별명 검사
//            if (ui.user?.nickname_title.isNullOrBlank()) {
//                // 별명이 없다면 별명 테스트 화면으로 이동
//                MyPageScreen(
//                    userName = ui.userName,
//                    levelLabel = ui.levelLabel,
//                    nextStage = ui.nextStageUi,
//                    onClickSettings = onClickSettings,
//                    onClickViewAllWords = onClickViewAllWords,
//                    onClickViewAllBadges = onClickViewAllBadges,
//                    onClickNicknameTest = { onClickViewNicknameTest() },  // 별명 테스트 클릭 시
//                    recentItems = ui.recentVocab,
//                    currentRecentIndex = 0,
//                    onChangeRecentIndex = {}
//                )
//            } else {
//                // 별명이 있으면 별명 카드 화면으로 이동
//                NicknameCardScreen(
//                    userName = ui.userName, // ui.userName을 전달
//                    nickname = ui.user?.nickname_title ?: "별명 없음",  // 별명 제목이 없으면 기본값
//                    onExit = onClickViewNicknameCard  // 완료 시 별명 화면으로 돌아가기
//                )
//            }
//        }
//    }
//}

// ===== Public Screen (UI만) =====
@Composable
fun MyPageScreen(
    modifier: Modifier = Modifier,
    userName: String,
    levelLabel: String,
    nextStage: NextStageUi?,
    //levelProgress: Float,
    onClickSettings: () -> Unit = {},
    onClickViewAllWords: () -> Unit = {},
    onClickViewAllBadges: () -> Unit = {},
    onClickNicknameTest: () -> Unit = {},
    // 최근 단어 데이터/인덱스
    onClickNickname: () -> Unit = {},
    recentItems: List<VocabularyDto> = emptyList(),
    currentRecentIndex: Int = 0,
    onChangeRecentIndex: (Int) -> Unit = {},
    // ✅ 추가: 호출부에서 넘겨주는 사용자 아바타 리소스
    @androidx.annotation.DrawableRes profileIconRes: Int,
    onClickChangeAvatar: () -> Unit = {} // ✅ 추가
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = ScreenPadding)
    ) {
        Spacer(Modifier.height(36.dp))
        MyPageTopBar(
            title = "마이페이지",
            onClickSettings = onClickSettings
        )

        Spacer(Modifier.height(32.dp))
        ProfileBlock(
            userName = userName,
            questionLabel = "치치의 어휘/문해력은?",
            profileIconRes = profileIconRes,   // ✅ 여기!
            //profileIconRes = MyPageR.drawable.ic_mypage_icon,
            onClickQuestion = onClickNickname,
            onClickAvatar = { onClickChangeAvatar() } // ✅ 새 콜백 연결

            //onClickQuestion = { onClickNicknameTest() }
        )
        var showLevelSheet by rememberSaveable { mutableStateOf(false) }

        Spacer(Modifier.height(20.dp))
        LevelBlock(
            userName = userName,
            currentLevelLabel = levelLabel,
            next = nextStage,
            onClickInfo = { showLevelSheet = true }   // ← 아이콘 탭 시 열기
        )

// ↓↓↓ 여기 추가: showLevelSheet가 true면 시트 표시
        if (showLevelSheet) {
            LevelInfoBottomSheet(
                next = nextStage,
                onDismiss = { showLevelSheet = false } // 닫기 시 원래 화면 그대로
            )
        }

        // ===== 단어 수집함 =====
        Spacer(Modifier.height(SectionGap))
        SectionHeader(title = "단어 수집함", action = "모두보기", onAction = onClickViewAllWords)
        Spacer(Modifier.height(12.dp))
        WordCollectionCard(
            items = recentItems,
            index = currentRecentIndex,
            //onPrev = { if (recentItems.isNotEmpty()) onChangeRecentIndex(currentRecentIndex - 1) },
            //onNext = { if (recentItems.isNotEmpty()) onChangeRecentIndex(currentRecentIndex + 1) },
            onClick = onClickViewAllWords,
            onSelectIndex = { tapped -> onChangeRecentIndex(tapped) } // ← 추가

        )

        // ===== 배지 수집함 =====
        Spacer(Modifier.height(SectionGap))
        SectionHeader(title = "배지 수집함", action = "모두보기", onAction = onClickViewAllBadges)
        Spacer(Modifier.height(12.dp))
        BadgeCollectionCard()
        Spacer(Modifier.height(24.dp))
    }
}

// ===== Components =====
@Composable
private fun MyPageTopBar(
    title: String,
    onClickSettings: () -> Unit
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
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Row(modifier = Modifier.align(Alignment.CenterEnd)) {
            IconButton(onClick = onClickSettings) {
                Icon(
                    painter = painterResource(id = MyPageR.drawable.ic_setting),
                    contentDescription = "설정"
                )
            }
        }
    }
}



// ==== Profile ====
private val AVATAR_SIZE = 80.dp
private val AVATAR_TO_TEXT_GAP = 12.dp
private val TOP_PADDING = (-4).dp
private val BOTTOM_PADDING = 2.dp
private val BUBBLE_H_PADDING = 16.dp
private val BUBBLE_V_PADDING = 10.dp
private val BUBBLE_CORNER = RoundedCornerShape(999.dp)
private val NAME_TEXT_SIZE = 16.sp
private val NAME_TEXT_WEIGHT = FontWeight.SemiBold
private val BUBBLE_TEXT_SIZE = 12.sp
private val BUBBLE_TEXT_WEIGHT = FontWeight.Medium

@Composable
private fun ProfileBlock(
    userName: String,
    questionLabel: String,
    profileIconRes: Int,
    onClickQuestion: () -> Unit = {},
    onClickAvatar: () -> Unit = {} // 추가
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(AVATAR_SIZE),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = profileIconRes),
            contentDescription = "프로필",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(AVATAR_SIZE).clickable { onClickAvatar() } // 클릭 시 다이얼로그 열기
        )

        Spacer(Modifier.width(AVATAR_TO_TEXT_GAP))

        Column(
            modifier = Modifier
                .weight(1f)
                .background(Color.White)
                .fillMaxHeight(),

        ) {
            Row(
                modifier = Modifier
                    .padding(top = 0.dp)
                    .offset(y = TOP_PADDING),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = userName, // ← 서버 me() 값 바인딩
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = NAME_TEXT_WEIGHT,
                        fontSize = NAME_TEXT_SIZE,
                        color = Color(0xFF262626)
                    )
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    painter = painterResource(id = MyPageR.drawable.ic_pencil),
                    contentDescription = "이름 수정",
                    tint = Gray_616161,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .padding(bottom = BOTTOM_PADDING)
                    .background(Bg_EFF4FB, shape = BUBBLE_CORNER)
                    .clickable { onClickQuestion() }
                    .padding(horizontal = BUBBLE_H_PADDING, vertical = BUBBLE_V_PADDING)
            ) {
                Text(
                    text = questionLabel,
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontSize = BUBBLE_TEXT_SIZE,
                        fontWeight = BUBBLE_TEXT_WEIGHT,
                        color = Color(0xFF262626)
                    )
                )
            }
        }
    }
}

// ==== Level ====
@Composable
private fun LevelBlock(
    userName: String,
    currentLevelLabel: String,
    next: NextStageUi?,              // null이면 최상위(고급)
    onClickInfo: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (next == null)
                "${userName}님의 수준은 ‘$currentLevelLabel’ (최고 단계)"
            else
                "${userName}님의 수준은 ‘$currentLevelLabel’",
            style = TextStyle(
                fontFamily = Pretendard,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            ),
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = painterResource(id = MyPageR.drawable.ic_question),
            contentDescription = "레벨 설명",
            tint = Color(0xFF262626),
            modifier = Modifier
                .size(20.dp)
                .clickable { onClickInfo() }     // 👈 바텀시트 열기
        )
    }

    Spacer(Modifier.height(8.dp))

    if (next == null) {
        // 최상위 레벨: 꽉 찬 바
        ProgressBar(
            progress = 1f,
            height = 12.dp,
            trackColor = Bg_EFF4FB,
            progressColor = Blue_195FCF
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "최고 단계입니다.",
            style = TextStyle(fontFamily = Pretendard, fontSize = 12.sp, color = Gray_616161)
        )
    } else {
        ProgressBar(
            progress = next.progress,
            height = 12.dp,
            trackColor = Bg_EFF4FB,
            progressColor = Blue_195FCF
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "다음 단계 ‘${next.nextLabel}’까지 ${formatNum(next.currentPoint)} / ${formatNum(next.target)} (남은 ${formatNum(next.remain)})",
            style = TextStyle(fontFamily = Pretendard, fontSize = 12.sp, color = Gray_616161)
        )
    }
}

private fun formatNum(n: Int): String = "%,d".format(n)
//@Composable
//private fun LevelBlock(
//    userName: String,
//    levelLabel: String,
//    progress: Float
//) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Spacer(Modifier.height(16.dp))
//
//        Text(
//            text = "${userName}님의 수준은 ‘${levelLabel}’", // ← 이름/단계 문구
//            style = TextStyle(
//                fontFamily = Pretendard,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Medium,
//                color = Color.Black
//            ),
//            modifier = Modifier.weight(1f)
//        )
//        Icon(
//            painter = painterResource(id = MyPageR.drawable.ic_question),
//            contentDescription = "레벨 설명",
//            tint = Color(0xFF262626),
//            modifier = Modifier.size(20.dp)
//        )
//    }
//
//    Spacer(Modifier.height(8.dp))
//    ProgressBar(
//        progress = progress.coerceIn(0f, 1f),
//        height = 12.dp,
//        trackColor = Bg_EFF4FB,
//        progressColor = Blue_195FCF
//    )
//}

@Composable
private fun ProgressBar(
    progress: Float,
    height: Dp,
    trackColor: Color,
    progressColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .clip(RoundedCornerShape(999.dp))
                .background(progressColor)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LevelInfoBottomSheet(
    next: NextStageUi?,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White
    ) {
        LevelInfoBottomSheetContent(next = next, onDismiss = onDismiss)
    }
}
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun LevelInfoBottomSheet(
//    next: NextStageUi?,           // null이면 최고 단계
//    onDismiss: () -> Unit
//) {
//    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//    ModalBottomSheet(
//        onDismissRequest = onDismiss,
//        sheetState = sheetState,
//        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), // 상단 두 모서리만 둥글게
//        containerColor = Color.White
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 20.dp, vertical = 16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // 제목: Pretendard 18, 세미볼드, 블랙
//            Text(
//                text = "수준별 학습 구간",
//                style = TextStyle(
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 18.sp,
//                    color = Color(0xFF000000)
//                ),
//                textAlign = TextAlign.Center
//            )
//
//            Spacer(Modifier.height(10.dp))
//
//            // 설명 1: Pretendard 14, 미디엄, 블랙
//            Text(
//                text = "학습을 하며 얻은 포인트를 모아 다음 수준에 도달하세요!",
//                style = TextStyle(
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.Medium,
//                    fontSize = 14.sp,
//                    color = Color(0xFF000000),
//                    lineHeight = 20.sp
//                ),
//                textAlign = TextAlign.Center
//            )
//
//            Spacer(Modifier.height(6.dp))
//
//            // 설명 2: Pretendard 12, 미디엄, 616161
//            Text(
//                text = "해당 구간은 학습을 진행시 얻는 XP를 통해 얻을 수 있어요.",
//                style = TextStyle(
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.Medium,
//                    fontSize = 12.sp,
//                    color = Gray_616161,
//                    lineHeight = 18.sp
//                ),
//                textAlign = TextAlign.Center
//            )
//
//            Spacer(Modifier.height(16.dp))
//
//            if (next == null) {
//                InfoRow("현재 단계", "고급 (최고 단계)")
//            } else {
//                InfoRow("현재 단계", next.currentLabel)
//                InfoRow("다음 단계", next.nextLabel)
//                InfoRow("타깃 포인트", "${formatNum(next.target)}")
//                InfoRow("내 포인트", "${formatNum(next.currentPoint)}")
//                InfoRow("남은 포인트", "${formatNum(next.remain)}")
//            }
//
//            Spacer(Modifier.height(20.dp))
//
//            // 닫기 버튼: 배경 195FCF, 텍스트 흰색 16 세미볼드
//            Button(
//                onClick = onDismiss,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(44.dp),
//                shape = RoundedCornerShape(12.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Blue_195FCF,
//                    contentColor = Color.White
//                )
//            ) {
//                Text(
//                    "닫기",
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 16.sp
//                )
//            }
//
//            Spacer(Modifier.height(12.dp))
//        }
//    }
//}

//바텀시트(프로그래스바)
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = TextStyle(fontFamily = Pretendard, fontSize = 14.sp, color = Color(0xFF616161))
        )
        Text(
            value,
            style = TextStyle(fontFamily = Pretendard, fontSize = 14.sp, color = Color(0xFF262626), fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    action: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = action,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onAction)
                .padding(horizontal = 4.dp, vertical = 2.dp),
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Gray_616161
            )
        )
    }
}

// ==== 최근 단어 카드 + 인디케이터 (API 연동) ====
@Composable
private fun WordCollectionCard(
    items: List<VocabularyDto> = emptyList(),
    index: Int = 0,                       // 부모에서 내려주는 현재 인덱스
    onClick: () -> Unit = {},
    onSelectIndex: (Int) -> Unit = {}     // 부모로 페이지 변경 전달
) {
    val pageCount = items.size.coerceAtLeast(1)   // 빈 리스트 대비
    val pagerState = rememberPagerState(
        initialPage = index.coerceIn(0, pageCount - 1),
        pageCount = { pageCount }
    )
    val scope = rememberCoroutineScope()

    // ✅ 부모에서 index가 바뀌면 Pager를 그 위치로 스크롤 (동기화)
    LaunchedEffect(index, pageCount) {
        val target = index.coerceIn(0, pageCount - 1)
        if (pagerState.currentPage != target) {
            pagerState.scrollToPage(target)  // 순간이동; 애니메이션 원하면 animateScrollToPage
        }
    }

    // ✅ Pager 쪽에서 스와이프(스크롤)로 페이지가 바뀌면 부모에 알려주기
    LaunchedEffect(pagerState.currentPage, pageCount) {
        val cp = pagerState.currentPage.coerceIn(0, pageCount - 1)
        if (cp != index) onSelectIndex(cp)
    }

    // ===== 카드 영역: 페이지별로 다른 단어 보여주기 =====
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(CardCorner),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .heightIn(min = 120.dp) // 높이 살짝 보장(선택)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val item = items.getOrNull(page)
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = item?.word ?: "최근 단어가 없어요",
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item?.meaning ?: "단어를 저장하면 여기에서 바로 볼 수 있어요.",
                    style = TextStyle(
                        fontFamily = Pretendard,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                val ex = item?.example
                if (!ex.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "예문) $ex",
                        style = TextStyle(
                            fontFamily = Pretendard,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Gray_616161
                        )
                    )
                }
            }
        }
    }

    // ===== 도트 인디케이터: 탭해서 해당 페이지로 이동 =====
    if (items.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        DotsIndicator(
            count = pageCount,
            selectedIndex = pagerState.currentPage.coerceIn(0, pageCount - 1),
            selectedColor = Blue_195FCF,
            unselectedColor = Color(0xFFE0E0E0),
            onSelect = { tapped ->
                scope.launch {
                    pagerState.animateScrollToPage(tapped)
                    // animateScrollToPage가 끝나면 LaunchedEffect가 onSelectIndex(tapped) 호출해 부모와 동기화합니다.
                }
            }
        )
    }
}
//@Composable
//private fun WordCollectionCard(
//    items: List<VocabularyDto> = emptyList(),
//    index: Int = 0,
//    onClick: () -> Unit = {},
//    onSelectIndex: (Int) -> Unit = {}
//) {
//    val item = items.getOrNull(index)
//
//    Card(
//        colors = CardDefaults.cardColors(containerColor = Color.White),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//        shape = RoundedCornerShape(CardCorner),
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onClick() }
//    ) {
//        Column(modifier = Modifier.padding(20.dp)) {
//            Text(
//                text = item?.word ?: "최근 단어가 없어요",
//                style = TextStyle(
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 16.sp,
//                    color = MaterialTheme.colorScheme.onBackground
//                )
//            )
//
//            Spacer(Modifier.height(8.dp))
//            Text(
//                text = item?.meaning ?: "단어를 저장하면 여기에서 바로 볼 수 있어요.",
//                style = TextStyle(
//                    fontFamily = Pretendard,
//                    fontWeight = FontWeight.Medium,
//                    fontSize = 14.sp,
//                    lineHeight = 22.sp,
//                    color = MaterialTheme.colorScheme.onBackground
//                )
//            )
//
//            Spacer(Modifier.height(12.dp))
//            val ex = item?.example
//            if (!ex.isNullOrBlank()) {
//                Text(
//                    text = "예문) $ex",
//                    style = TextStyle(
//                        fontFamily = Pretendard,
//                        fontWeight = FontWeight.Medium,
//                        fontSize = 12.sp,
//                        lineHeight = 18.sp,
//                        color = Gray_616161
//                    )
//                )
//            }
//        }
//    }
//
//    // 카드 아래 도트 인디케이터
//    if (items.isNotEmpty()) {
//        Spacer(Modifier.height(12.dp))
//        DotsIndicator(
//            count = items.size,       // 최근 단어 개수
//            selectedIndex = index,    // 현재 보고 있는 인덱스
//            selectedColor = Blue_195FCF,
//            unselectedColor = Color(0xFFE0E0E0),
//            onSelect = onSelectIndex
//        )
//    }
//}

@Composable
private fun DotsIndicator(
    count: Int,
    selectedIndex: Int,
    selectedColor: Color,
    unselectedColor: Color,
    onSelect: (Int) -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(count) { idx ->
            Box(
                modifier = Modifier
                    .size(if (idx == selectedIndex) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (idx == selectedIndex) selectedColor else unselectedColor)
                    .clickable { onSelect(idx) }    // ← 탭해서 페이지 이동
            )
            if (idx != count - 1) Spacer(Modifier.width(8.dp))
        }
    }
}

// ===== 배지 수집함 (더미) =====
@Composable
private fun BadgeCollectionCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(start = 13.dp, end = 20.dp, top = 20.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = MyPageR.drawable.img_empty),
                            contentDescription = "배지",
                            modifier = Modifier.size(88.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "일주일 출석",
                            style = TextStyle(
                                fontFamily = Pretendard,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEFEFEF, showSystemUi = true)
@Composable
private fun LevelInfoBottomSheetPreview_NextExists() {
    MaterialTheme {
        FakeBottomSheetPreviewHost {
            LevelInfoBottomSheetContent(
                next = NextStageUi(
                    currentLabel = "활용",
                    nextLabel = "심화",
                    target = 2700,
                    currentPoint = 1350,
                    remain = 1350,
                    progress = 0.5f
                ),
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFEFEFEF, showSystemUi = true)
@Composable
private fun LevelInfoBottomSheetPreview_TopLevel() {
    MaterialTheme {
        FakeBottomSheetPreviewHost {
            LevelInfoBottomSheetContent(
                next = null,
                onDismiss = {}
            )
        }
    }
}

@Composable
private fun LevelInfoBottomSheetContent(
    next: NextStageUi?,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                top = 16.dp,     // ⬅️ 위로 16
                end = 20.dp,
                bottom = 16.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "수준별 학습 구간",
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = Color(0xFF000000)
            ),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "학습을 하며 얻은 포인트를 모아 다음 수준에 도달하세요!",
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color(0xFF000000),
                lineHeight = 20.sp
            ),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "해당 구간은 학습을 진행시 얻는 XP를 통해 얻을 수 있어요.",
            style = TextStyle(
                fontFamily = Pretendard,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Gray_616161,
                lineHeight = 18.sp
            ),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))

        if (next == null) {
            InfoRow("현재 단계", "고급 (최고 단계)")
        } else {
            InfoRow("현재 단계", next.currentLabel)
            InfoRow("다음 단계", next.nextLabel)
            InfoRow("타깃 포인트", "${formatNum(next.target)}")
            InfoRow("내 포인트", "${formatNum(next.currentPoint)}")
            InfoRow("남은 포인트", "${formatNum(next.remain)}")
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue_195FCF,
                contentColor = Color.White
            )
        ) {
            Text(
                "닫기",
                fontFamily = Pretendard,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun FakeBottomSheetPreviewHost(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x33000000))        // 반투명 배경(스크림)
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = Color.White,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.BottomCenter)
        ) {
            content()
        }
    }
}