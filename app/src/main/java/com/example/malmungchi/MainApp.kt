package com.example.malmungchi



import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.malmungchi.navigation.BottomNavBar
import com.example.malmungchi.navigation.MainScreen
import com.example.malmungchi.navigation.LogNavDestinations
import com.example.malmungchi.navigation.TermsRoute
import com.malmungchi.core.model.UserDto
import com.malmungchi.core.model.WordItem
import com.malmungchi.feature.login.AppTermsScreen
import com.malmungchi.feature.login.EmailLoginScreen
import com.malmungchi.feature.login.LoginScreen
import com.malmungchi.feature.login.MarketingTermsScreen
import com.malmungchi.feature.login.PrivacyTermsScreen
import com.malmungchi.feature.login.SignUpRoute
import com.malmungchi.feature.login.TermsAgreementScreen
import com.malmungchi.feature.study.StudyReadingViewModel
import com.malmungchi.feature.study.first.StudyAppendixListScreen
import com.malmungchi.feature.study.first.StudyAppendixScreen
import com.malmungchi.feature.study.first.StudyIntroScreen
import com.malmungchi.feature.study.first.StudyReadingScreen
import com.malmungchi.feature.study.intro.PastStudyScreenRoute
import com.malmungchi.feature.study.intro.StudyWeeklyScreen
import com.malmungchi.feature.study.second.StudySecondIntroScreen
import com.malmungchi.feature.study.second.StudySecondScreen
import com.malmungchi.feature.study.third.StudyCompleteScreen
import com.malmungchi.feature.study.third.StudyThirdIntroScreen
import com.malmungchi.feature.study.third.StudyThirdResultScreenWrapper
import com.malmungchi.feature.study.third.StudyThirdScreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.malmungchi.data.net.RetrofitProvider
import com.malmungchi.feature.login.LevelTestStartScreen
import com.malmungchi.feature.mypage.RemindSettingsScreen
import com.malmungchi.feature.mypage.SettingsScreen
import com.malmungchi.feature.mypage.WordCollectionRoute
import com.malmungchi.feature.mypage.WordCollectionScreen
import com.malmungchi.feature.mypage.nickname.NicknameTestFlowScreen
import com.malmungchi.feature.mypage.nickname.NicknameTestIntroScreen
import com.malmungchi.feature.mypage.nickname.NicknameTestLoadingScreen
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.malmungchi.navigation.BottomNavItem
import com.example.malmungchi.ui.theme.SetStatusBarWhite
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.malmungchi.feature.ai.AiChatCompleteScreen
import com.malmungchi.feature.ai.ChatScreen
import com.malmungchi.feature.ai.ChatViewModel
import com.malmungchi.feature.ai.FreeChatScreen
import com.malmungchi.feature.friend.FriendAddViewModel
import com.malmungchi.feature.friend.FriendScreen
import com.malmungchi.feature.friend.RankTab
import com.malmungchi.feature.login.LevelGeneratingRoute
import com.malmungchi.feature.login.LevelReadingQuizRoute
import com.malmungchi.feature.login.LevelTestIntroRoute
import com.malmungchi.feature.login.LevelsViewModel
import com.malmungchi.feature.mypage.MyPageViewModel
import com.malmungchi.feature.mypage.badge.BadgeCollectionRoute
import com.malmungchi.feature.mypage.nickname.NicknameCardDialog
//import com.malmungchi.feature.mypage.nickname.NicknameCardScreen
import com.malmungchi.feature.quiz.QuizCategoryRoute
import com.malmungchi.feature.quiz.QuizCompleteScreen
import com.malmungchi.feature.quiz.QuizFlowViewModel
import com.malmungchi.feature.quiz.QuizLoadingScreen
import com.malmungchi.feature.quiz.QuizRetryAllResultScreen
import com.malmungchi.feature.quiz.QuizRetryHost
import com.malmungchi.feature.quiz.QuizRetryIntroScreen
import com.malmungchi.feature.quiz.QuizSolveHost
import com.malmungchi.feature.friend.FriendAddScreen
import com.google.accompanist.systemuicontroller.rememberSystemUiController




/* ────────────────────────────────────────────────────────────────────────────────
   자동 로그인(SharedPreferences 헬퍼)
   ──────────────────────────────────────────────────────────────────────────────── */
private const val PREF_NAME = "session_prefs"
private const val KEY_USER_ID = "user_id"
private const val KEY_TOKEN = "token"
private const val KEY_REFRESH = "refresh_token" // ★ 추가

private fun saveSession(context: Context, userId: Int, token: String) {
    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit()
        .putInt(KEY_USER_ID, userId)
        .putString(KEY_TOKEN, token)
        .apply()
}

// ★ 리프레시 토큰만 따로 저장/갱신할 수 있는 헬퍼(기존 콜 사이트 영향 X)
private fun saveRefreshToken(context: Context, refresh: String) {
    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit().putString(KEY_REFRESH, refresh).apply()
}

private fun readSession(context: Context): Triple<Int?, String?, String?> {
    val sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    val uid = sp.getInt(KEY_USER_ID, -1).let { if (it <= 0) null else it }
    val at = sp.getString(KEY_TOKEN, null)
    val rt = sp.getString(KEY_REFRESH, null) // ★ 추가
    return Triple(uid, at, rt)
}

private fun clearSession(context: Context) {
    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        .edit()
        .remove(KEY_USER_ID)
        .remove(KEY_TOKEN)
        .remove(KEY_REFRESH) // ★ 추가
        .apply()
}

@Composable
private fun WithBottomBar(
    navController: NavHostController,
    content: @Composable (innerPadding: PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = Color.White,                // ✅ 바탕 흰색
        contentColor = Color.Black,                  // (텍스트 대비)
        bottomBar = { BottomNavBar(navController = navController) }
    ) { inner ->
        Box(
            Modifier
                .padding(inner)
                .fillMaxSize()
                .background(Color.White)            // ✅ 컨텐츠 영역도 흰색
        ) {
            content(inner)
        }
    }
}

/* ────────────────────────────────────────────────────────────────────────────────
   그래프 루트(주간 허브)에서 뒤로가기 시 앱 종료 대신 main으로 이동
   ──────────────────────────────────────────────────────────────────────────────── */
@Composable
private fun StudyGraphBackHandler(navController: NavController) {
    val backEntry by navController.currentBackStackEntryAsState()
    val route = backEntry?.destination?.route
    val isWeeklyRoot = route == "study_weekly"

    BackHandler(enabled = isWeeklyRoot) {
        navController.navigate("main") {
            launchSingleTop = true
        }
    }
}

@Composable
fun WhiteSystemBars() {
    val systemUi = rememberSystemUiController()
    SideEffect {
        systemUi.setStatusBarColor(color = Color.White, darkIcons = true)
        systemUi.setNavigationBarColor(color = Color.White, darkIcons = true)
    }
}
//아래처럼, 바텀 네비게이션이 있는 상위 그래프(study_graph, quiz_graph, ai_graph,
@Composable
private fun BottomNavBackHandler(navController: NavController) {
    val backEntry by navController.currentBackStackEntryAsState()
    val route = backEntry?.destination?.route

    BackHandler {
        when {
            // ✅ 바텀 네비게이션 하위 그래프 중 하나면 → 항상 study_graph(홈 탭)으로 이동
            route?.startsWith("quiz") == true ||
                    route?.startsWith("ai") == true ||
                    route?.startsWith("friend") == true ||
                    route?.startsWith("mypage") == true -> {
                navController.navigate("study_graph") {
                    launchSingleTop = true
                    popUpTo("study_graph") { inclusive = false }
                }
            }

            // ✅ 나머지는 기본 popBackStack()
            else -> {
                navController.popBackStack()
            }
        }
    }
}

/* ───────────────────────────────
   전역 학습 중단 감시 핸들러
   ─────────────────────────────── */
@Composable
fun GlobalStudyBackHandler(navController: NavController) {
    val backEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backEntry?.destination?.route
    var showExitAlert by remember { mutableStateOf(false) }

    // 뒤로가기 감시: study_로 시작하고 weekly가 아닐 때만 Alert
    BackHandler(enabled = currentRoute?.startsWith("study_") == true && currentRoute != "study_weekly") {
        showExitAlert = true
    }

    if (showExitAlert) {
        com.malmungchi.feature.study.EndStudyAlert.Show(
            onConfirm = {
                showExitAlert = false
                navController.navigate("study_weekly") {
                    launchSingleTop = true
                    popUpTo("study_graph") { inclusive = false }
                }
            },
            onDismiss = { showExitAlert = false }
        )
    }
}

/* ────────────────────────────────────────────────────────────────────────────────
   MainApp (전체)
   ──────────────────────────────────────────────────────────────────────────────── */
@Composable
fun MainApp() {

    WhiteSystemBars()
    val navController = rememberNavController()


    // ✅ 항상 최상단에서 감시하는 Alert 핸들러 추가
    GlobalStudyBackHandler(navController)

    val appContext = LocalContext.current

    // ✅ 새 백핸들러 적용
//    BottomNavBackHandler(navController)

    // ✅ 전역 백핸들러: 스택 있으면 뒤로, 없으면 main으로(종료 방지)
    BackHandler {
        val current = navController.currentBackStackEntry?.destination?.route
        val hasPrev = navController.previousBackStackEntry != null

        when {
            hasPrev -> navController.navigateUp()
            current != "main" -> {
                navController.navigate("main") { launchSingleTop = true }
            }
            else -> {
                // main에서 더 이상 갈 데 없으면 '아무것도 하지 않음' → 종료 방지
            }
        }
    }

    LogNavDestinations(navController)

    // 시작은 splash에서 자동 로그인 여부 판단
    //NavHost(navController, startDestination = "splash") {
    // ✅ 온보딩을 가장 먼저 보여줌
    NavHost(navController, startDestination = "onboarding") {

        // ✅ 온보딩 화면 (항상 노출)
        composable("onboarding") {
            // feature 모듈의 OnboardingScreen 사용
            com.malmungchi.feature.login.OnboardingScreen(
                onFinish = {
                    // 온보딩 종료 → 기존 splash 로직으로 위임
                    navController.navigate("splash") {
                        popUpTo("onboarding") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                //autoAdvanceMillis = 1500L
            )
        }

        composable("splash") {
            LaunchedEffect(Unit) {
                val appCtx = appContext.applicationContext
                val (uid, token, _) = readSession(appCtx)

                val auth = RetrofitProvider.getAuthApi(
                    context = appCtx,
                    onUnauthorized = {
                        clearSession(appCtx)
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )

                val meResult: UserDto? = if (uid != null && !token.isNullOrBlank()) {
                    runCatching { withContext(Dispatchers.IO) { auth.me() } }
                        .fold(
                            onSuccess = { res -> if (res.success) (res.user ?: res.result) else null },
                            onFailure = { null }
                        )
                } else null

                if (meResult != null) {
                    val level = meResult.level ?: 0
                    if (level <= 0) {
                        // 레벨 0 → 레벨 테스트 인트로
                        navController.navigate("level_intro") {
                            popUpTo("splash") { inclusive = true }
                            launchSingleTop = true
                        }
//                        navController.navigate("level_test_start") {
//                            popUpTo("splash") { inclusive = true }
//                            launchSingleTop = true
//                        }
                    } else {
                        // 레벨 1+ → 학습 그래프
                        navController.navigate("study_graph") {
                            popUpTo("splash") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                } else {
                    clearSession(appCtx)
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
//            Box(
//                modifier = Modifier.fillMaxSize(),
//                contentAlignment = Alignment.Center
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.img_start),
//                    contentDescription = "앱 시작 이미지",
//                    modifier = Modifier.size(160.dp)   // ⬅️ 원하시는 크기로 조절
//                )
//            }

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
//        composable("splash") {
//            LaunchedEffect(Unit) {
//                // ✅ 항상 applicationContext 사용
//                val appCtx = appContext.applicationContext
//
//                // ✅ Triple은 3개로 받기
//                val (uid, token, _) = readSession(appCtx)
//
//                val isValid = if (uid != null && !token.isNullOrBlank()) {
//                    val auth = RetrofitProvider.getAuthApi(
//                        context = appCtx,
//                        onUnauthorized = {
//                            clearSession(appCtx)
//                            navController.navigate("login") {
//                                popUpTo("splash") { inclusive = true }
//                                launchSingleTop = true
//                            }
//                        }
//                    )
//                    runCatching {
//                        withContext(Dispatchers.IO) { auth.me() }
//                    }.fold(
//                        onSuccess = { res -> res.success },
//                        onFailure = { false }
//                    )
//                } else false
//
//                if (isValid) {
//                    navController.navigate("study_graph") {
//                        popUpTo("splash") { inclusive = true }
//                        launchSingleTop = true
//                    }
//                } else {
//                    clearSession(appCtx)
//                    navController.navigate("login") {
//                        popUpTo("splash") { inclusive = true }
//                        launchSingleTop = true
//                    }
//                }
//            }
//
//            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                CircularProgressIndicator()
//            }
//        }


//        composable("splash") {
//            LaunchedEffect(Unit) {
//                val (uid, token) = readSession(appContext)
//                if (uid != null && !token.isNullOrBlank()) {
//                    navController.navigate("study_graph") {
//                        popUpTo("splash") { inclusive = true }
//                        launchSingleTop = true
//                    }
//                } else {
//                    navController.navigate("login") {
//                        popUpTo("splash") { inclusive = true }
//                        launchSingleTop = true
//                    }
//                }
//            }
//            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                CircularProgressIndicator()
//            }
//        }

        // 로그인
        composable("login") {
            LoginScreen(
                onEmailLogin = {
                    navController.navigate("email_login") { launchSingleTop = true }
                },
                onSignUp = {
                    navController.navigate(TermsRoute.Agreement)
                },
                onKakao = { /* 소셜 로그인 연결 시 사용 */ },
                onNaver = { /* 소셜 로그인 연결 시 사용 */ },
                onGoogle = { /* 소셜 로그인 연결 시 사용 */ }
            )
        }

        // 약관
        composable(TermsRoute.Agreement) {
            // ✅ 상세 화면에서 돌아올 때 저장된 값을 관찰
            val agreeApp by navController.currentBackStackEntry!!
                .savedStateHandle
                .getStateFlow("agree_app", false)
                .collectAsState()

            // ✅ 추가: 개인정보 동의 수신
            val agreePrivacy by navController.currentBackStackEntry!!
                .savedStateHandle
                .getStateFlow("agree_privacy", false)
                .collectAsState()

            // ✅ 추가: 마케팅 동의 수신
            val agreeMarketing by navController.currentBackStackEntry!!
                .savedStateHandle
                .getStateFlow("agree_marketing", false)
                .collectAsState()

            TermsAgreementScreen(
                onOpenAppTerms = { navController.navigate(TermsRoute.App) },
                onOpenPrivacy = { navController.navigate(TermsRoute.Privacy) },
                onOpenMarketing = { navController.navigate(TermsRoute.Marketing) },
                onAgreeContinue = {
                    navController.navigate("sign_up_flow") {
                        popUpTo(TermsRoute.Agreement) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                // ⬇️ 새로 추가한 파라미터
                externalAppAgree = agreeApp,
                externalPrivacyAgree = agreePrivacy,
                externalMarketingAgree = agreeMarketing
            )
        }
        composable(TermsRoute.App) {
            AppTermsScreen(
                onBack = { navController.popBackStack() },
                onDone = {
                    // ✅ 상세 화면에서 '동의' 눌렀다는 결과를 이전 화면에 남김
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("agree_app", true)
                    navController.popBackStack()
                }
            )
        }
        composable(TermsRoute.Privacy) {
            PrivacyTermsScreen(
                onBack = { navController.popBackStack() },
                onDone = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("agree_privacy", true)   // ✅ 개인정보 동의 신호
                    navController.popBackStack()
                }
//                onDone = { navController.popBackStack() }
            )
        }
        composable(TermsRoute.Marketing) {
            MarketingTermsScreen(
                onBack = { navController.popBackStack() },
                onDone = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("agree_marketing", true)  // ✅ 마케팅 동의 신호
                    navController.popBackStack()
                }
                //onDone = { navController.popBackStack() }
            )
        }

        // 회원가입 플로우
        composable("sign_up_flow") {
            SignUpRoute(
                onBack = { navController.popBackStack() },
                onRegistered = {
                    // 가입 성공 → 이메일 로그인
                    navController.navigate("email_login") {
                        popUpTo("login") { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 이메일 로그인 (성공 시 세션 저장 + 그래프로 이동)
        composable("email_login") {
            val scope = rememberCoroutineScope()

            EmailLoginScreen(
                onBack = { navController.popBackStack() },
                onLoginSuccess = { userId, token ->
                    com.malmungchi.data.session.SessionManager.set(userId, token)
                    saveSession(appContext, userId, token)

                    val appCtx = appContext.applicationContext
                    val auth = RetrofitProvider.getAuthApi(appCtx) {
                        clearSession(appCtx)
                        navController.navigate("login") {
                            popUpTo("email_login") { inclusive = true }
                            launchSingleTop = true
                        }
                    }

                    scope.launch {
                        val meResult: UserDto? = runCatching { withContext(Dispatchers.IO) { auth.me() } }
                            .fold(
                                onSuccess = { res -> if (res.success) (res.user ?: res.result) else null },
                                onFailure = { null }
                            )

                        val level = meResult?.level ?: 0
                        if (level <= 0) {
                            // ✅ 레벨 0 → 레벨 테스트 인트로
//                            navController.navigate("level_test_start") {
//                                popUpTo("login") { inclusive = true }
//                                launchSingleTop = true
//                            }
                            // ✅ 레벨 0 → 레벨 테스트 인트로
                            navController.navigate("level_intro") {
                                popUpTo("email_login") { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            // ✅ 레벨 1+ → 학습 그래프
                            navController.navigate("study_graph") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                }
            )
        }
//        composable("email_login") {
//            EmailLoginScreen(
//                onBack = { navController.popBackStack() },
//                onLoginSuccess = { userId, token ->
//                    // 기존 세션 매니저 유지
//                    com.malmungchi.data.session.SessionManager.set(userId, token)
//                    // 자동 로그인 저장
//                    saveSession(appContext, userId, token)
//
//                    navController.navigate("study_graph") {
//                        // 로그인/약관 스택 제거 → 뒤로가기 시 로그인으로 회귀 방지
//                        popUpTo("login") { inclusive = true }
//                        launchSingleTop = true
//                    }
//                }
//            )
//        }

//        // 레벨 테스트 인트로
//        composable("level_test_start") {
//            LevelTestStartScreen(
//                onBackClick = { navController.popBackStack() },
//                onStartClick = {
//                    // stage = 0 → 최초 진단
//                    navController.navigate("level_test/0") {
//                        launchSingleTop = true
//                    }
//                }
//            )
//        }
//        // 레벨 테스트 본편(Route)
//        composable("level_test/{stage}") { backStackEntry ->
//            val stageInt = backStackEntry.arguments?.getString("stage")?.toIntOrNull() ?: 0
//            LevelTestRoute(
//                userName = "", // 필요 시 me()로 이름 받아 기억해뒀다가 넘겨도 OK
//                stageInt = stageInt,
//                onBack = { navController.popBackStack() },
//                onGoStudy = {
//                    // 제출 후 결과 CTA → 학습 그래프
//                    navController.navigate("study_graph") {
//                        launchSingleTop = true
//                        popUpTo("level_test_start") { inclusive = true }
//                    }
//                }
//            )
//        }
        // ───────── 레벨 테스트 플로우 ─────────
        // 기존 위치에 추가
        navigation(
            route = "level_graph",
            startDestination = "level_intro"
        ) {
            composable("level_intro") { backStackEntry ->
                val parent = remember(backStackEntry) {
                    navController.getBackStackEntry("level_graph")
                }
                val vm: LevelsViewModel = hiltViewModel(parent)

                LevelTestIntroRoute(
                    onGoGenerating = { stage ->
                        navController.navigate("level_generating") { launchSingleTop = true }
                    },
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.navigate("study_graph") { launchSingleTop = true }
                        }
                    },
                    // ↓ 전달
                    viewModel = vm
                )
            }

            composable("level_generating") { backStackEntry ->
                val parent = remember(backStackEntry) {
                    navController.getBackStackEntry("level_graph")
                }
                val vm: LevelsViewModel = hiltViewModel(parent)

                LevelGeneratingRoute(
                    onReady = { _,_,_ ->
                        navController.navigate("level_reading") { launchSingleTop = true }
                    },
                    onCancel = {
                        if (!navController.popBackStack("level_intro", false)) {
                            navController.navigate("level_intro") { launchSingleTop = true }
                        }
                    },
                    viewModel = vm
                )
            }

            composable("level_reading") { backStackEntry ->
                val parent = remember(backStackEntry) {
                    navController.getBackStackEntry("level_graph")
                }
                val vm: LevelsViewModel = hiltViewModel(parent)

                LevelReadingQuizRoute(
                    onBackClick = {
                        if (!navController.popBackStack("level_generating", false)) {
                            navController.navigate("level_intro") { launchSingleTop = true }
                        }
                    },
                    onRetry = {
                        vm.reset()
                        navController.navigate("level_intro") {
                            popUpTo("level_graph") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onGoHome = {
                        navController.navigate("study_graph") {
                            launchSingleTop = true
                            popUpTo("level_graph") { inclusive = true } // 스택 정리
                        }
                    },
                    viewModel = vm
                )
            }
        }

//        // ───────── 레벨 테스트 플로우 ─────────
//
//        // 1) 인트로
//        composable("level_intro") {
//            LevelTestIntroRoute(
//                onGoGenerating = { stage ->
//                    navController.navigate("level_generating") { launchSingleTop = true }
//                },
//                onBackClick = {
//                    // 일반 뒤로 실패 시 홈으로 안전망
//                    if (!navController.navigateUp()) {
//                        navController.navigate("study_graph") { launchSingleTop = true }
//                    }
//                }
//            )
//        }
//
//        // 2) 생성중
//        composable("level_generating") {
//            LevelGeneratingRoute(
//                onReady = { stage, passage, questions ->
//                    navController.navigate("level_reading") { launchSingleTop = true }
//                },
//                onCancel = {
//                    // 생성중의 '이전'은 인트로로 고정
//                    if (!navController.popBackStack("level_intro", inclusive = false)) {
//                        navController.navigate("level_intro") { launchSingleTop = true }
//                    }
//                }
//            )
//        }
//
//        // 3) 읽기/퀴즈/결과
//        composable("level_reading") {
//            LevelReadingQuizRoute(
//                onBackClick = {
//                    // 읽기의 '이전'은 생성중으로 고정
//                    if (!navController.popBackStack("level_generating", inclusive = false)) {
//                        // 생성중이 스택에 없으면 인트로로
//                        navController.navigate("level_intro") { launchSingleTop = true }
//                    }
//                },
//                onRetry = {
//                    // 다시하기는 인트로로 완전히 복귀
//                    navController.navigate("level_intro") {
//                        launchSingleTop = true
//                        popUpTo("level_intro") { inclusive = true }
//                    }
//                },
//                onGoHome = {
//                    // 시작하기(학습 진입)
//                    navController.navigate("study_graph") {
//                        launchSingleTop = true
//                        popUpTo("level_intro") { inclusive = true } // 레벨 테스트 스택 정리
//                    }
//                }
//            )
//        }

        // 메인(하단바)
        composable("main") {
            // ✅ main에서 시스템 백은 무시 → 앱 종료 방지
            BackHandler(enabled = true) { /* no-op */ }
            // 루트 컨트롤러만 쓰게 강제: MainScreen 사용 X
            LaunchedEffect(Unit) {
                navController.navigate("study_graph") {
                    popUpTo("main") { inclusive = true }  // main 제거
                    launchSingleTop = true
                    restoreState = true
                }
            }
            // 간단한 로더만 보여줌
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

//            MainScreen(
//                onStartStudyFlow = { navController.navigate("study_graph") { launchSingleTop = true } },
//                onOpenSettings   = { navController.navigate("settings") },
//                onOpenWordCollection = { navController.navigate("word_collection") } // ★ 추가
//            )
        }

        // 학습 그래프 (루트: 주간 허브)
//        navigation(
//            route = "study_graph",
//            startDestination = "study_weekly"
//        ) {
//
//
//            // 주간 허브
//            composable("study_weekly") { backStackEntry ->
//                // 루트 뒤로가기 → main으로(앱 종료 방지)
//                SetStatusBarWhite()
//                val systemUi = rememberSystemUiController()
//                SideEffect {
//                    systemUi.setStatusBarColor(color = Color.White, darkIcons = true)
//                    systemUi.setNavigationBarColor(color = Color.White, darkIcons = true, navigationBarContrastEnforced = false)
//                }
//
//                StudyGraphBackHandler(navController)
//
//                val parentEntry = remember(backStackEntry) {
//                    navController.getBackStackEntry("study_graph")
//                }
//                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
//
//                val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE) // "YYYY-MM-DD"
//                val body = vm.quote.collectAsState().value
//                val studiedDates by vm.studiedDates.collectAsState(initial = emptySet())
//
//                // ✅ 화면 진입 시 "오늘의 글감" 불러오기
//                LaunchedEffect(Unit) {
//                    vm.fetchTodayQuote()
//                    vm.refreshStudiedDatesForWeek(LocalDate.parse(today))
//                }
//
//                LaunchedEffect(today) {
//                    vm.refreshStudiedDatesForWeek(LocalDate.parse(today))
//                }
//                Scaffold(
//                    containerColor = Color.White,
//                    bottomBar = {
//                        BottomNavBar(navController = navController as NavHostController) }
//                ) { innerPadding ->
//                    Box(Modifier.padding(innerPadding) .background(Color.White)) {
//                        StudyWeeklyScreen(
//                            initialDateLabel = today,
//                            onDateChange = { label ->
//                                runCatching { LocalDate.parse(label) }.onSuccess { picked ->
//                                    vm.fetchPastStudyByDate(picked)
//                                    vm.refreshStudiedDatesForWeek(picked)
//                                }
//                            },
//                            bodyText = body,
//                            onBackClick = { navController.popBackStack() },
//                            onGoStudyClick = {
//                                navController.navigate("study_intro") {
//                                    launchSingleTop = true
//                                    restoreState = true
//                                }
//                            },
//                            onOpenPastStudy = { label ->
//                                navController.navigate("past_study/$label") {
//                                    launchSingleTop = true
//                                    restoreState = true
//                                }
//                            },
//                            hasStudy = { day -> studiedDates.contains(day) }
//                        )
//                    }
//                }
//
////                StudyWeeklyScreen(
////                    initialDateLabel = today,
////                    onDateChange = { label ->
////                        runCatching { LocalDate.parse(label) }.onSuccess { picked ->
////                            vm.fetchPastStudyByDate(picked)
////                            vm.refreshStudiedDatesForWeek(picked)
////                        }
////                    },
////                    bodyText = body,
////                    onBackClick = { navController.popBackStack() },
////                    onGoStudyClick = {
////                        navController.navigate("study_intro") {
////                            launchSingleTop = true
////                            restoreState = true
////                        }
////                    },
////                    onOpenPastStudy = { label ->
////                        navController.navigate("past_study/$label") {
////                            launchSingleTop = true
////                            restoreState = true
////                        }
////                    },
////                    hasStudy = { day -> studiedDates.contains(day) }
////                )
//            }
//
//            // 지난 학습 상세
//            composable("past_study/{date}") { backStackEntry ->
//                val parentEntry = remember(backStackEntry) {
//                    navController.getBackStackEntry("study_graph")
//                }
//                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
//
//                val dateParam = backStackEntry.arguments?.getString("date") // "YYYY-MM-DD"
//                val localDate = dateParam?.let { LocalDate.parse(it) }
//
//                LaunchedEffect(dateParam) {
//                    localDate?.let { vm.fetchPastStudyByDate(it) }
//                }
//
//                PastStudyScreenRoute(
//                    dateLabel = dateParam?.replace("-", ".") ?: "",
//                    viewModel = vm,
//                    onLoad = null,
//                    onBackClick = { navController.popBackStack() }
//                )
//            }
//
//            // 1단계 인트로
//            composable("study_intro") { backStackEntry ->
//                val parentEntry = remember(backStackEntry) {
//                    navController.getBackStackEntry("study_graph")
//                }
//                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
//
//                StudyIntroScreen(
//                    onStart = { /* optional */ },
//                    onNavigateNext = {
//                        navController.navigate("study_reading") {
//                            launchSingleTop = true
//                            restoreState = true
//                        }
//                    }
//                )
//            }
//
//            // 1단계 본문 → Appendix
//            composable("study_reading") { backStackEntry ->
//                val parentEntry = remember(backStackEntry) {
//                    navController.getBackStackEntry("study_graph")
//                }
//                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
//
//                StudyReadingScreen(
//                    viewModel = vm,
//                    onNextClick = {
//                        navController.navigate("appendix") {
//                            launchSingleTop = true
//                            restoreState = true
//                            popUpTo("study_graph") { inclusive = false }
//                        }
//                    },
//                    onBackClick = { navController.popBackStack() }
//                )
//            }
//
//            // Appendix → AppendixList
//            composable("appendix") {
//                StudyAppendixScreen(
//                    onNavigateNext = {
//                        navController.navigate("appendix_list") {
//                            launchSingleTop = true
//                            restoreState = true
//                            popUpTo("study_graph") { inclusive = false }
//                        }
//                    }
//                )
//            }
//
//            // AppendixList → 2단계 Intro
//            composable("appendix_list") { backStackEntry ->
//                val parentEntry = remember(backStackEntry) {
//                    navController.getBackStackEntry("study_graph")
//                }
//                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
//
//                val sidState = vm.studyId.collectAsState()
//                val sid = sidState.value
//
//                LaunchedEffect(sid) {
//                    android.util.Log.d("NAV", ">> appendix_list (sid=$sid)")
//                }
//
//                if (sid == null) {
//                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        CircularProgressIndicator()
//                    }
//                } else {
//                    StudyAppendixListScreen(
//                        studyId = sid,
//                        viewModel = vm,
//                        onBackClick = {
//                            navController.navigate("study_reading") {
//                                launchSingleTop = true
//                                restoreState = true
//                                popUpTo("study_graph") { inclusive = false }
//                            }
//                        },
//                        onNavigateNext = {
//                            navController.navigate("study_second_intro") {
//                                launchSingleTop = true
//                                restoreState = true
//                                popUpTo("study_graph") { inclusive = false }
//                            }
//                        }
//                    )
//                }
//            }
//
//            // 2단계 Intro → 2단계 본문
//            composable("study_second_intro") {
//                StudySecondIntroScreen(
//                    onNavigateNext = {
//                        navController.navigate("study_second") {
//                            launchSingleTop = true
//                            restoreState = true
//                            popUpTo("study_graph") { inclusive = false }
//                        }
//                    }
//                )
//            }
//
//            // 2단계 본문 → 3단계 Intro
//            composable("study_second") { backStackEntry ->
//                val parentEntry = remember(backStackEntry) {
//                    navController.getBackStackEntry("study_graph")
//                }
//                val viewModel: StudyReadingViewModel = hiltViewModel(parentEntry)
//
//                StudySecondScreen(
//                    viewModel = viewModel,
//                    onBackClick = {
//                        navController.navigate("appendix_list") {      // ✅ 정확한 라우트명
//                            popUpTo("study_second") { inclusive = true }
//                            launchSingleTop = true
//                        }
//                    },
//                    //onBackClick = { navController.popBackStack() },
//                    onNextClick = {
//                        navController.navigate("study_third_intro") {
//                            launchSingleTop = true
//                            restoreState = true
//                            popUpTo("study_graph") { inclusive = false }
//                        }
//                    }
//                )
//            }
//
//            // 3단계 Intro → 3단계 본문
//            composable("study_third_intro") {
//                StudyThirdIntroScreen(
//                    onNavigateNext = {
//                        navController.navigate("study_third") {
//                            launchSingleTop = true
//                            restoreState = true
//                            popUpTo("study_graph") { inclusive = false }
//                        }
//                    }
//                )
//            }
//
//            // 3단계 본문 → 결과
//            composable("study_third") { backStackEntry ->
//                val parentEntry = remember(backStackEntry) {
//                    navController.getBackStackEntry("study_graph")
//                }
//                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
//
//                val id = vm.studyId.collectAsState().value
//                val text = vm.quote.collectAsState().value
//
//                LaunchedEffect(id, text) {
//                    if (id == null || text.isBlank()) {
//                        vm.fetchTodayQuote()
//                    }
//                }
//
//                if (id == null || text.isBlank()) {
//                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        CircularProgressIndicator()
//                    }
//                } else {
//                    StudyThirdScreen(
//                        studyId = id,
//                        text = text,
//                        viewModel = vm,
//                        onBackClick = { navController.popBackStack("study_second", inclusive = false) },
//                        onNextClick = {
//                            navController.navigate("study_third_result/$id") {
//                                launchSingleTop = true
//                                restoreState = true
//                                //popUpTo("study_graph") { inclusive = false }
//                            }
//                        }
//                    )
//                }
//            }
//
//            // 결과 → 완료
//            composable("study_third_result/{studyId}") { backStackEntry ->
//                val parentEntry = remember(backStackEntry) {
//                    navController.getBackStackEntry("study_graph")
//                }
//                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
//
//                val id = backStackEntry.arguments?.getString("studyId")?.toIntOrNull()
//                if (id == null) {
//                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        Text("잘못된 접근입니다.")
//                    }
//                } else {
//                    StudyThirdResultScreenWrapper(
//                        studyId = id,
//                        viewModel = vm,
//                        onBackClick = { navController.popBackStack("study_third", inclusive = false) },
//                        onFinishClick = {
//                            navController.navigate("study_third_complete") {
//                                launchSingleTop = true
//                                restoreState = true
//                                popUpTo("study_graph") { inclusive = false }
//                            }
//                        }
//                    )
//                }
//            }
//
//            // 완료 → 메인
//            composable("study_third_complete") {
//                val viewModel: StudyReadingViewModel = hiltViewModel()  // ViewModel 가져오기
//
//                StudyCompleteScreen(
//                    viewModel = viewModel,   // ★ viewModel 전달
//                    onNextClick = {
//                        navController.navigate("main") {
//                            launchSingleTop = true
//                            popUpTo("study_graph") { inclusive = true }
//                        }
//                    }
//                )
//            }
//        }
        navigation(
            route = "study_graph",
            startDestination = "study_graph_entry"   // ✅ 여기만 바꾸면 끝!
            //startDestination = "study_weekly"
        ) {

            // ✅ 공용: 항상 활성화되는 invisible Composable


            // ✅ 그래프 진입 시 전역 핸들러 1회 등록
            composable("study_graph_entry") {
                LaunchedEffect(Unit) {
                    // 전역 핸들러 navigate 전에 study_graph에 남게 함
                    if (navController.currentDestination?.route != "study_graph_global_handler") {
                        navController.navigate("study_graph_global_handler") {
                            launchSingleTop = true
                        }
                    }

                    // 주간 허브로 이동
                    navController.navigate("study_weekly") {
                        launchSingleTop = true
                    }
                }
                Box(Modifier.size(0.dp))
            }

            composable("study_graph_global_handler") {
                val parentEntry = remember(it) { navController.getBackStackEntry("study_graph") }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
                var showExitAlert by remember { mutableStateOf(false) }
                val backEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backEntry?.destination?.route

                BackHandler(enabled = currentRoute != "study_weekly") {
                    showExitAlert = true
                }

                if (showExitAlert) {
                    com.malmungchi.feature.study.EndStudyAlert.Show(
                        onConfirm = {
                            showExitAlert = false
                            vm.refreshStudyProgressForWeek(LocalDate.now())
                            navController.navigate("study_weekly") {
                                launchSingleTop = true
                                popUpTo("study_graph") { inclusive = false }
                            }
                        },
                        onDismiss = { showExitAlert = false }
                    )
                }

                // 빈 UI
                Box(Modifier.size(0.dp))
            }

            // ───────────── 주간 허브 ─────────────
            composable("study_weekly") { backStackEntry ->
                SetStatusBarWhite()
                val systemUi = rememberSystemUiController()
                SideEffect {
                    systemUi.setStatusBarColor(color = Color.White, darkIcons = true)
                    systemUi.setNavigationBarColor(color = Color.White, darkIcons = true, navigationBarContrastEnforced = false)
                }

                StudyGraphBackHandler(navController)

                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)

                val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                val body = vm.quote.collectAsState().value

                // ✅ 주간 학습 상태 갱신 (진입 시)
                LaunchedEffect(Unit) {
                    vm.fetchTodayQuote()
                    vm.refreshStudyProgressForWeek(LocalDate.parse(today))
                }

                Scaffold(
                    containerColor = Color.White,
                    bottomBar = { BottomNavBar(navController = navController as NavHostController) }
                ) { innerPadding ->
                    Box(Modifier.padding(innerPadding).background(Color.White)) {
                        StudyWeeklyScreen(
                            vm = vm,  // ✅ 추가
                            initialDateLabel = today,
                            onDateChange = { label ->
                                runCatching { LocalDate.parse(label) }.onSuccess { picked ->
                                    vm.fetchPastStudyByDate(picked)
                                    vm.refreshStudyProgressForWeek(picked)
                                }
                            },
                            bodyText = body,
                            onBackClick = { navController.popBackStack() },
                            onGoStudyClick = {
                                navController.navigate("study_intro") {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onOpenPastStudy = { label ->
                                navController.navigate("past_study/$label") {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }

                        )
                    }
                }
            }

            // ───────────── 지난 학습 상세 ─────────────
            composable("past_study/{date}") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)

                val dateParam = backStackEntry.arguments?.getString("date")
                val localDate = dateParam?.let { LocalDate.parse(it) }

                LaunchedEffect(dateParam) {
                    localDate?.let { vm.fetchPastStudyByDate(it) }
                }

                PastStudyScreenRoute(
                    dateLabel = dateParam?.replace("-", ".") ?: "",
                    viewModel = vm,
                    onLoad = null,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // ───────────── 1단계 Intro ─────────────
            composable("study_intro") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)

                StudyIntroScreen(
                    onStart = {},
                    onNavigateNext = {
                        navController.navigate("study_reading") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // ───────────── 1단계 본문 → Appendix ─────────────
            composable("study_reading") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)

                StudyReadingScreen(
                    viewModel = vm,
                    onNextClick = {
                        navController.navigate("appendix") {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo("study_graph") { inclusive = false }
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }

            // ───────────── Appendix → AppendixList ─────────────
            composable("appendix") {
                StudyAppendixScreen(
                    onNavigateNext = {
                        navController.navigate("appendix_list") {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo("study_graph") { inclusive = false }
                        }
                    }
                )
            }

            // ───────────── AppendixList → 2단계 Intro ─────────────
            composable("appendix_list") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
                val sid = vm.studyId.collectAsState().value

                if (sid == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    StudyAppendixListScreen(
                        studyId = sid,
                        viewModel = vm,
                        onBackClick = {
                            navController.navigate("study_reading") {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo("study_graph") { inclusive = false }
                            }
                        },
                        onNavigateNext = {
                            navController.navigate("study_second_intro") {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo("study_graph") { inclusive = false }
                            }
                        }
                    )
                }
            }

            // ───────────── 2단계 Intro ─────────────
            composable("study_second_intro") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)

                // ✅ 1단계 완료 표시
                LaunchedEffect(Unit) { vm.markStepComplete(1) }

                StudySecondIntroScreen(
                    onNavigateNext = {
                        navController.navigate("study_second") {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo("study_graph") { inclusive = false }
                        }
                    }
                )
            }

            // ───────────── 2단계 본문 ─────────────
            composable("study_second") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)

                StudySecondScreen(
                    viewModel = vm,
                    onBackClick = {
                        navController.navigate("appendix_list") {
                            popUpTo("study_second") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNextClick = {
                        navController.navigate("study_third_intro") {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo("study_graph") { inclusive = false }
                        }
                    }
                )
            }

            // ───────────── 3단계 Intro ─────────────
            composable("study_third_intro") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)

                // ✅ 2단계 완료 표시
                LaunchedEffect(Unit) { vm.markStepComplete(2) }

                StudyThirdIntroScreen(
                    onNavigateNext = {
                        navController.navigate("study_third") {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo("study_graph") { inclusive = false }
                        }
                    }
                )
            }

            // ───────────── 3단계 본문 → 결과 ─────────────
            composable("study_third") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)

                val id = vm.studyId.collectAsState().value
                val text = vm.quote.collectAsState().value

                LaunchedEffect(id, text) {
                    if (id == null || text.isBlank()) vm.fetchTodayQuote()
                }

                if (id == null || text.isBlank()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    StudyThirdScreen(
                        studyId = id,
                        text = text,
                        viewModel = vm,
                        onBackClick = { navController.popBackStack("study_second", inclusive = false) },
                        onNextClick = {
                            navController.navigate("study_third_result/$id") {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }

            // ───────────── 결과 → 완료 ─────────────
            composable("study_third_result/{studyId}") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
                val id = backStackEntry.arguments?.getString("studyId")?.toIntOrNull()

                if (id == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("잘못된 접근입니다.")
                    }
                } else {
                    StudyThirdResultScreenWrapper(
                        studyId = id,
                        viewModel = vm,
                        onBackClick = { navController.popBackStack("study_third", inclusive = false) },
                        onFinishClick = {
                            navController.navigate("study_third_complete") {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo("study_graph") { inclusive = false }
                            }
                        }
                    )
                }
            }

            // ───────────── 완료 화면 ─────────────
            composable("study_third_complete") {
                val vm: StudyReadingViewModel = hiltViewModel()

                // ✅ 3단계 완료 표시
                LaunchedEffect(Unit) { vm.markStepComplete(3) }

                StudyCompleteScreen(
                    viewModel = vm,
                    onNextClick = {
                        // ✅ 완료 후 주간화면 새로고침 후 메인으로
                        vm.refreshStudyProgressForWeek(LocalDate.now())
                        navController.navigate("main") {
                            launchSingleTop = true
                            popUpTo("study_graph") { inclusive = true }
                        }
                    }
                )
            }
        }

        // 탭 라우트 → MainScreen으로 위임 (초간단 라우터)
//        composable("quiz") {
//            MainScreen(
//                initialTab = "quiz", // 👈 MainScreen이 이 값을 보고 탭 선택
//                onStartStudyFlow = { navController.navigate("study_graph") { launchSingleTop = true } }
//                ,
//                onOpenSettings   = { navController.navigate("settings") }
//            )
//        }

// ─────────────────────────────────────────────
// 🧩 퀴즈 그래프 (QuizScreen → Loading → Solve → Retry → Result → Complete)
// ─────────────────────────────────────────────
        navigation(
            route = "quiz_graph",
            startDestination = "quiz_home"
        ) {
            // 0) 홈(카테고리)
            composable("quiz_home") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("quiz_graph")
                }
                val vm: QuizFlowViewModel = hiltViewModel(parentEntry)
                WithBottomBar(navController as NavHostController) {
                    QuizCategoryRoute(
                        vm = vm,
                        onPickCategory = { label ->
                            Log.d("NAV", "Pick category: $label")
                            // 한글 라우트 안전하게 인코딩
                            val arg = java.net.URLEncoder.encode(
                                label, java.nio.charset.StandardCharsets.UTF_8.toString()
                            )
                            navController.navigate("quiz_loading/$arg") { launchSingleTop = true }
                        }
                    )
                }
            }

            // 1) 로딩 화면: route arg(라벨)로 startQuiz 실행
            composable(
                route = "quiz_loading/{cat}",
                arguments = listOf(
                    androidx.navigation.navArgument("cat") {
                        type = androidx.navigation.NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("quiz_graph")
                }
                val vm: QuizFlowViewModel = hiltViewModel(parentEntry)

                val raw = backStackEntry.arguments?.getString("cat")!!
                val catLabel = remember(raw) {
                    java.net.URLDecoder.decode(raw, java.nio.charset.StandardCharsets.UTF_8.toString())
                }

                // 🚀 여기서 실제 세트 생성/호출 (String 라벨로!)
                LaunchedEffect(catLabel) {
                    vm.startQuiz(catLabel)   // 내부에서 createOrGetBatch API 호출 → ui.loading=true
                }

                QuizLoadingScreen(
                    vm = vm,
                    onBackToHome = {
                        // 아이콘 back & 시스템 back 동일
                        navController.popBackStack("quiz_home", inclusive = false)
                    },
                    onReadyToSolve = {
                        // 세트 로드 완료 → 풀이로
                        navController.navigate("quiz_solve") { launchSingleTop = true }
                    }
                )
            }

            // 2) 7문항 풀이
            composable("quiz_solve") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("quiz_graph")
                }
                val vm: QuizFlowViewModel = hiltViewModel(parentEntry)

                QuizSolveHost(
                    vm = vm,
                    onQuitToHome = {
                        navController.navigate("quiz_home") {
                            popUpTo("quiz_graph") { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onAllFinished = {
                        navController.navigate("quiz_retry_intro") { launchSingleTop = true }
                    },
                    postSubmitDelayMs = 1000L
                )
            }

            // 3) 재도전 인트로
            composable("quiz_retry_intro") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("quiz_graph")
                }
                val vm: QuizFlowViewModel = hiltViewModel(parentEntry)

                LaunchedEffect(Unit) {
                    vm.startRetryFromWrong()
                    kotlinx.coroutines.delay(600)
                    if (vm.ui.value.finished) {
                        navController.navigate("quiz_retry_result") { launchSingleTop = true }
                    } else {
                        navController.navigate("quiz_retry_solve") { launchSingleTop = true }
                    }
                }

                QuizRetryIntroScreen()
            }

            // 4) 재도전 풀이
            composable("quiz_retry_solve") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("quiz_graph")
                }
                val vm: QuizFlowViewModel = hiltViewModel(parentEntry)

                QuizRetryHost(
                    vm = vm,
                    onFinish = {
                        navController.navigate("quiz_retry_result") { launchSingleTop = true }
                    },
                    onBack = { navController.navigateUp() }
                )
            }

            // 5) 재도전 결과
            composable("quiz_retry_result") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("quiz_graph")
                }
                val vm: QuizFlowViewModel = hiltViewModel(parentEntry)
                val ui by vm.ui.collectAsState()

                val results = remember { vm.buildRetryResultItems() }
                QuizRetryAllResultScreen(
                    categoryTitle = ui.headerTitle,
                    results = results,
                    onBack = { navController.popBackStack() },
                    onFinishClick = {
                        navController.navigate("quiz_complete") { launchSingleTop = true }
                    }
                )
            }

            // 6) 완료
            composable("quiz_complete") {
                QuizCompleteScreen(
                    vm = hiltViewModel<QuizFlowViewModel>(),
                    onNextClick = {
                        navController.navigate("quiz_home") {
                            popUpTo("quiz_graph") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        navigation(
            route = "ai_graph",
            startDestination = "ai"
        ) {
            composable("ai") {
                WithBottomBar(navController as NavHostController) {
                    com.malmungchi.feature.ai.AiScreen(
                        onStartAiChat = { navController.navigate("ai_chat") },
                        onFreeChat = { navController.navigate("free_chat") } // ✅ 자유대화 이동
                    )
                }
            }

            // ===== 대화연습(취준생 맞춤 상황) =====
            composable("ai_chat") {
                val vm: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

                ChatScreen(
                    vm = vm,
                    onBack = {
                        if (!navController.popBackStack()) {
                            navController.navigate("ai") {
                                popUpTo("ai_graph") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    },
                    onExit = {
                        navController.navigate("ai_chat_complete") {
                            launchSingleTop = true
                        }
                    },
                    onContinue = {
                        val state = vm.ui.value
                        if (!state.isRecording && !state.isLoading) vm.startRecording()
                    }
                )
            }

            // ===== 자유 대화 =====
            composable("free_chat") {
                val vm: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

                FreeChatScreen( // ← 제목만 "자유 대화"인 동일 UI/로직 화면
                    vm = vm,
                    onBack = {
                        if (!navController.popBackStack()) {
                            navController.navigate("ai") {
                                popUpTo("ai_graph") { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    },
                    onExit = {
                        // ✅ 흐름 동일: 완료화면으로 이동
                        navController.navigate("ai_chat_complete") {
                            launchSingleTop = true
                        }
                    },
                    onContinue = {
                        val state = vm.ui.value
                        if (!state.isRecording && !state.isLoading) vm.startRecording()
                    }
                )
            }

            // ===== 완료 화면 (공용) =====
            composable("ai_chat_complete") { backStackEntry ->
                // 그래프 스코프 VM 공유 (완료화면에서 포인트/이력 표시 등 필요 시)
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("ai_graph")
                }
                val vm: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(parentEntry)

                AiChatCompleteScreen(
                    viewModel = vm,
                    onFinishNavigate = {
                        navController.navigate("ai") {
                            popUpTo("ai_graph") { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

//        navigation(
//            route = "ai_graph",
//            startDestination = "ai"
//        ) {
//            composable("ai") {
//                WithBottomBar(navController as NavHostController) {
//                    com.malmungchi.feature.ai.AiScreen(
//                        onStartAiChat = { navController.navigate("ai_chat") },
//                        onFreeChat = { /* 자유대화 */ }
//                    )
//                }
//            }
//
//            composable("ai_chat") {
//                val vm: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
//
//                ChatScreen(
//                    vm = vm,
//                    // ← 뒤로가기: 기존 그대로 (3회 미만이면 다이얼로그는 ChatScreen 내부에서 처리됨)
//                    onBack = {
//                        if (!navController.popBackStack()) {
//                            navController.navigate("ai") {
//                                popUpTo("ai_graph") { inclusive = false }
//                                launchSingleTop = true
//                            }
//                        }
//                    },
//                    // ✅ “대화 종료하기” → 완료 화면으로
//                    onExit = {
//                        navController.navigate("ai_chat_complete") {
//                            launchSingleTop = true
//                        }
//                    },
//                    onContinue = {
//                        val state = vm.ui.value
//                        if (!state.isRecording && !state.isLoading) vm.startRecording()
//                    }
//                )
//            }
//
//            // ✅ 완료 화면: 같은 그래프("ai_graph")에 스코프된 ChatViewModel 주입
//            composable("ai_chat_complete") {
//                val parentEntry = remember(navController) {
//                    navController.getBackStackEntry("ai_graph")
//                }
//                val vm: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(parentEntry)
//
//                AiChatCompleteScreen(
//                    viewModel = vm,
//                    onFinishNavigate = {
//                        // “종료하기” → AiScreen 으로 이동(스택 정리)
//                        navController.navigate("ai") {
//                            popUpTo("ai_graph") { inclusive = false }
//                            launchSingleTop = true
//                        }
//                    }
//                    // snackbarHostState는 옵션이니 필요하면 넘겨줘도 됩니다.
//                )
//            }
//        }


//        navigation(
//            route = "ai_graph",
//            startDestination = "ai"
//        ) {
//            composable("ai") {
//                WithBottomBar(navController as NavHostController) {
//                    com.malmungchi.feature.ai.AiScreen(
//                        onStartAiChat = { navController.navigate("ai_chat") },
//                        onFreeChat = { /* 자유대화 화면 이동 시 여기 */ }
//                    )
//                }
//            }
//
//            composable("ai_chat") {
//                // ✅ ViewModel 주입 (ChatScreen에 넘겨주기)
//                val vm: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
//
//                ChatScreen(
//                    vm = vm,
//                    onBack = {
//                        if (!navController.popBackStack()) {
//                            navController.navigate("ai") {
//                                popUpTo("ai_graph") { inclusive = false }
//                                launchSingleTop = true
//                            }
//                        }
//                    },
//                    onExit = {
//                        // ✅ "대화 종료하기" → AiScreen으로 이동
//                        navController.navigate("ai") {
//                            popUpTo("ai_graph") { inclusive = false } // 그래프 루트 유지
//                            launchSingleTop = true
//                        }
//                    },
//                    onContinue = {
//                        // ✅ "대화 이어가기" → 같은 화면에서 즉시 음성 녹음 재시작
//                        val state = vm.ui.value
//                        if (!state.isRecording && !state.isLoading) {
//                            vm.startRecording()
//                        }
//                        // 이미 녹음/전송 중이면 아무 것도 하지 않음
//                    }
//                )
//            }
//        }

//        navigation(
//            route = "ai_graph",
//            startDestination = "ai"
//        ) {
//            composable("ai") {
//                WithBottomBar(navController as NavHostController) {
//                    com.malmungchi.feature.ai.AiScreen(
//                        onStartAiChat = { navController.navigate("ai_chat") },
//                        onFreeChat = { /* 자유대화 화면 이동 시 여기 */ }
//                    )
//                }
//            }
//            composable("ai_chat") {
//                ChatScreen(
//                    onBack = {
//                        if (!navController.popBackStack()) {
//                            navController.navigate("ai") {
//                                popUpTo("ai_graph") { inclusive = false }
//                                launchSingleTop = true
//                            }
//                        }
//                    }
//                )
//            }
//        }

//        // 변경 (루트 NavController + 동일 BottomBar 사용)
//        composable("ai") {
//            WithBottomBar(navController as NavHostController) {
//                com.malmungchi.feature.ai.AiScreen()
//            }
//        }
//        composable("friend") {
//            WithBottomBar(navController as NavHostController) {
//                com.malmungchi.feature.friend.FriendScreen()
//            }
//        }
        navigation(
            route = BottomNavItem.Friend.route, // "friend_graph"
            startDestination = "friend/home"
        ) {
            composable("friend/home") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(BottomNavItem.Friend.route)
                }
                val vm: FriendAddViewModel = hiltViewModel(parentEntry)
                val ui by vm.ui.collectAsState()
                val ranks = when (ui.rankTab) {
                    RankTab.FRIEND -> ui.friends
                    RankTab.ALL    -> ui.all
                }

                // ✅ 바텀바 래핑
                WithBottomBar(navController as NavHostController) {
                    FriendScreen(
                        onAddFriend = { navController.navigate("friend/add") },
                        tab = ui.rankTab,
                        onSelectFriendTab = { vm.switchTab(RankTab.FRIEND) },
                        onSelectAllTab   = { vm.switchTab(RankTab.ALL) },
                        ranks = ranks,
                        loading = ui.rankLoading
                    )
                }
            }

            composable("friend/add") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(BottomNavItem.Friend.route)
                }
                val vm: FriendAddViewModel = hiltViewModel(parentEntry)
                val ui by vm.ui.collectAsState()
                val clipboard = LocalClipboardManager.current

                // ✅ 바텀바 래핑
                WithBottomBar(navController as NavHostController) {
                    FriendAddScreen(
                        myCode = ui.myCode,
                        foundFriend = ui.foundFriend,
                        isAdded = ui.isAdded,
                        loading = ui.loading,
                        onBack = { navController.popBackStack() },
                        onSearch = { code -> vm.searchAndAdd(code) },
                        onAddFriend = { vm.refresh() },
                        onViewRank = {
                            vm.switchTab(RankTab.FRIEND)
                            navController.popBackStack()
                        },
                        onCopyMyCode = { code ->
                            clipboard.setText(AnnotatedString(code))
                        }
                    )
                }
            }
        }

//        composable("ai") {
//            MainScreen(initialTab = "ai", onStartStudyFlow = { navController.navigate("study_graph") { launchSingleTop = true } },onOpenSettings   = { navController.navigate("settings") })
//        }
//        composable("friend") {
//            MainScreen(initialTab = "friend", onStartStudyFlow = { navController.navigate("study_graph") { launchSingleTop = true } },onOpenSettings   = { navController.navigate("settings") })
//        }
//        composable("mypage") {
//            MainScreen(initialTab = "mypage", onStartStudyFlow = { navController.navigate("study_graph") { launchSingleTop = true } },onOpenSettings   = { navController.navigate("settings") })
//        }

//        composable("mypage") {
//            com.malmungchi.feature.mypage.MyPageRoute(
//                onClickSettings = { navController.navigate("settings") },
//                onClickViewAllWords = { navController.navigate("word_collection") },
//                onClickViewAllBadges = { /* TODO */ },
//                onClickViewNicknameTest = {                 // 🔹 말풍선 탭 → 인트로
//                    navController.navigate("nickname_test_intro") { launchSingleTop = true }
//                }
//            )
//        }
//        composable("mypage") {
//            WithBottomBar(navController as NavHostController) {
//                com.malmungchi.feature.mypage.MyPageRoute(
//                    onClickSettings = { navController.navigate("settings") },
//                    onClickViewAllWords = { navController.navigate("word_collection") },
//                    // ✅ 여기만 채우기
//                    onClickViewAllBadges = {
//                        navController.navigate("badges")
//                    },
//
//                    //onClickViewAllBadges = { /* TODO */ },
//                    onClickViewNicknameTest = {
//                        navController.navigate("nickname_test_intro") { launchSingleTop = true }
//                    },
//                    onClickViewNicknameCard = { nicknameTitle, userName ->
//                        val n = Uri.encode(nicknameTitle)
//                        val u = Uri.encode(userName)
//                        navController.navigate("nickname_card_screen?nickname=$n&userName=$u") {
//                            launchSingleTop = true
//                        }
//                    }
//                )
//            }
//        }
        navigation(
            route = "mypage_graph",
            startDestination = "mypage"
        ) {
            composable("mypage") { backStackEntry ->
                // ✅ 이 위치(Composable 내부)에서만 remember/hiltViewModel 사용 가능
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("mypage_graph")
                }
                val vm: MyPageViewModel = hiltViewModel(parentEntry)

                WithBottomBar(navController as NavHostController) {
                    com.malmungchi.feature.mypage.MyPageRoute(
                        viewModel = vm, // ✅ 전달
                        onClickSettings = { navController.navigate("settings") },
                        onClickViewAllWords = { navController.navigate("word_collection") },
                        onClickViewAllBadges = { navController.navigate("badges") },
                        onClickViewNicknameTest = {
                            navController.navigate("nickname_test_intro") { launchSingleTop = true }
                        },
                        onClickViewNicknameCard = { nicknameTitle, userName ->
                            val n = Uri.encode(nicknameTitle)
                            val u = Uri.encode(userName)
                            navController.navigate("nickname_card_screen?nickname=$n&userName=$u") {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }

            composable("word_collection") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("mypage_graph")
                }
                val vm: MyPageViewModel = hiltViewModel(parentEntry)

                WordCollectionRoute(
                    viewModel = vm,
                    onBack = {
                        navController.navigate("mypage") {
                            popUpTo("word_collection") { inclusive = true }
                            launchSingleTop = true
                        }
                        vm.loadIfNeeded(forcePartialRefresh = true)
                    }
                )
            }

        }

        // ★ 라우트 정의를 파라미터 포함으로
        composable(
            route = "nickname_card_screen?nickname={nickname}&userName={userName}",
            arguments = listOf(
                navArgument("nickname") { type = NavType.StringType; defaultValue = "" },
                navArgument("userName") { type = NavType.StringType; defaultValue = "" } // 사용 안 함
            )
        ) { backStackEntry ->
            val nicknameArg = backStackEntry.arguments?.getString("nickname").orEmpty()
            // val userNameArg = backStackEntry.arguments?.getString("userName").orEmpty() // 미사용

            // ✅ 화면 대신 모달 다이얼로그를 바로 띄운다
            NicknameCardDialog(
                nickname = nicknameArg.ifBlank { null },     // 빈 문자열이면 null 처리 → 로딩 일러스트
                onExit = { navController.popBackStack() },    // 닫기 = 기존 ic_back 역할
                onSaveImage = { selectedNickname ->
                    // TODO: 저장 로직 연결(필요 시)
                    // ex) viewModel.saveCardImage(selectedNickname)
                }
            )
        }
//        composable(
//            route = "nickname_card_screen?nickname={nickname}&userName={userName}",
//            arguments = listOf(
//                navArgument("nickname") { type = NavType.StringType; defaultValue = "" },
//                navArgument("userName") { type = NavType.StringType; defaultValue = "" }
//            )
//        ) { backStackEntry ->
//            val nicknameArg = backStackEntry.arguments?.getString("nickname").orEmpty()
//            val userNameArg = backStackEntry.arguments?.getString("userName").orEmpty()
//
//            // ✅ 여기서는 새 ViewModel 만들지 말고, 전달받은 값으로 바로 그린다
//            NicknameCardScreen(
//                userName = userNameArg,
//                nickname = nicknameArg,
//                onExit = { navController.popBackStack() },
//                onSaveImage = { /* 필요시 구현 */ }
//            )
//        }
        composable("nickname_test_intro") {
            NicknameTestIntroScreen(
                onBackClick = {
                    navController.navigate("mypage") {
                        popUpTo("nickname_test_intro") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onStartClick = {
                    navController.navigate("nickname_test_loading") { launchSingleTop = true }
                }
            )
        }




        composable("nickname_test_loading") {
            NicknameTestLoadingScreen(
                onBackClick = {
                    navController.navigate("mypage") {
                        popUpTo("nickname_test_loading") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                // 필요 시 로딩 완료/탭 시 다음으로
                onNavigateNext = {
                    navController.navigate("nickname_test_flow") {
                        launchSingleTop = true
                    }
                }
            )
        }

// 핵심: Flow 내에서 1~9 → 10~11 → 12~18 → Finished(=결과) 까지 한 화면에서 오케스트레이션
        composable("nickname_test_flow") {
            NicknameTestFlowScreen(
                onExitToMyPage = {
                    navController.navigate("mypage") {
                        popUpTo("nickname_test_intro") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRetryFromStart = {
                    navController.navigate("nickname_test_loading") {
                        popUpTo("nickname_test_flow") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
//            NicknameTestFlowScreen(
//                // 결과 화면에서 “나가기” → 마이페이지
//                onExitToMyPage = {
//                    navController.navigate("mypage") {
//                        popUpTo("nickname_test_intro") { inclusive = true }
//                        launchSingleTop = true
//                    }
//                },
//                // 결과 화면에서 “다시하기” → 로딩부터 재시작
//                onRetryFromStart = {
//                    navController.navigate("nickname_test_loading") {
//                        popUpTo("nickname_test_flow") { inclusive = true }
//                        launchSingleTop = true
//                    }
//                }
//            )
        }

        composable("settings") {
            SettingsScreen(
                onClickBack = { navController.popBackStack() },
                onClickRemind = { navController.navigate("remind_settings") },
                onClickWithdraw = { navController.navigate("withdraw") }, // 회원 탈퇴 화면 이동 예시
                navigateToLogin = {
                    navController.navigate("login") {
                        // 백스택 전부 제거 → 로그인 화면만 남음
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("badges") {
            // ic_back 누르면 MyPage로 복귀
            BadgeCollectionRoute(
                onBack = { navController.popBackStack() }
            )
        }



//        composable("settings") {
//            SettingsScreen(
//                onClickBack = { navController.popBackStack() },
//                onClickRemind = { navController.navigate("remind_settings") },
//                onClickLogout = { /* TODO */ },
//                onClickWithdraw = { /* TODO */ }
//            )
//        }
        composable("remind_settings") {
            val scope = rememberCoroutineScope()
            RemindSettingsScreen(
                onBack = { navController.popBackStack() },
                onSave = { list ->
                    Log.d("REMIND", "onSave called: $list")   // ✅ 꼭 보이게
                    // 스낵바가 잠깐이라도 보이도록 살짝 지연
                    scope.launch {
                        kotlinx.coroutines.delay(600)
                        navController.popBackStack()  // 저장 후 뒤로
                    }
                }
            )
        }
        // MainApp() 의 NavHost {...} 안


//        composable("nickname_test_intro") {
//            NicknameTestIntroScreen(
//                onBackClick = {
//                    // 인트로에서 back → 마이페이지로
//                    navController.navigate("mypage") {
//                        popUpTo("nickname_test_intro") { inclusive = true }
//                        launchSingleTop = true
//                    }
//                },
//                onStartClick = {
//                    // 시작하기 → 로딩
//                    navController.navigate("nickname_test_loading") { launchSingleTop = true }
//                }
//            )
//        }

//        composable("nickname_test_loading") {
//            NicknameTestLoadingScreen(
//                onBackClick = {
//                    navController.navigate("mypage") {
//                        popUpTo("nickname_test_loading") { inclusive = true }
//                        launchSingleTop = true
//                    }
//                },
//                onNavigateNext = {                       // ✅ 필수 파라미터 전달
//                    navController.navigate("nickname_test_flow") {
//                        launchSingleTop = true
//                    }
//                }
//            )
//        }
//        composable("word_collection") {
//            var favOnly by remember { mutableStateOf(false) }
//
//            WordCollectionScreen(
//                onBack = {
//                    // 마이페이지 화면으로 복귀
//                    navController.navigate("mypage") {
//                        popUpTo("word_collection") { inclusive = true } // 현재 화면 제거
//                        launchSingleTop = true
//                    }
//                },
//                filterFavoriteOnly = favOnly,
//                onToggleFilterFavorite = { favOnly = it },
//                items = emptyList()
//            )
//        }


    }
}



