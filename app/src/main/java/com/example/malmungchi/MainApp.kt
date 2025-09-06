package com.example.malmungchi


import android.content.Context
import androidx.activity.compose.BackHandler
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
import com.malmungchi.feature.login.LevelTestRoute
import com.malmungchi.feature.login.LevelTestStartScreen
import com.malmungchi.feature.mypage.RemindSettingsScreen
import com.malmungchi.feature.mypage.SettingsScreen
import com.malmungchi.feature.mypage.WordCollectionRoute
import com.malmungchi.feature.mypage.WordCollectionScreen
import kotlinx.coroutines.launch


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

/* ────────────────────────────────────────────────────────────────────────────────
   MainApp (전체)
   ──────────────────────────────────────────────────────────────────────────────── */
@Composable
fun MainApp() {
    val navController = rememberNavController()
    val appContext = LocalContext.current

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
                autoAdvanceMillis = 1500L
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
                        navController.navigate("level_test_start") {
                            popUpTo("splash") { inclusive = true }
                            launchSingleTop = true
                        }
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
            TermsAgreementScreen(
                onOpenAppTerms = { navController.navigate(TermsRoute.App) },
                onOpenPrivacy = { navController.navigate(TermsRoute.Privacy) },
                onOpenMarketing = { navController.navigate(TermsRoute.Marketing) },
                onAgreeContinue = {
                    navController.navigate("sign_up_flow") {
                        popUpTo(TermsRoute.Agreement) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(TermsRoute.App) {
            AppTermsScreen(
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() }
            )
        }
        composable(TermsRoute.Privacy) {
            PrivacyTermsScreen(
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() }
            )
        }
        composable(TermsRoute.Marketing) {
            MarketingTermsScreen(
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() }
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
                            navController.navigate("level_test_start") {
                                popUpTo("login") { inclusive = true }
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

        // 레벨 테스트 인트로
        composable("level_test_start") {
            LevelTestStartScreen(
                onBackClick = { navController.popBackStack() },
                onStartClick = {
                    // stage = 0 → 최초 진단
                    navController.navigate("level_test/0") {
                        launchSingleTop = true
                    }
                }
            )
        }
        // 레벨 테스트 본편(Route)
        composable("level_test/{stage}") { backStackEntry ->
            val stageInt = backStackEntry.arguments?.getString("stage")?.toIntOrNull() ?: 0
            LevelTestRoute(
                userName = "", // 필요 시 me()로 이름 받아 기억해뒀다가 넘겨도 OK
                stageInt = stageInt,
                onBack = { navController.popBackStack() },
                onGoStudy = {
                    // 제출 후 결과 CTA → 학습 그래프
                    navController.navigate("study_graph") {
                        launchSingleTop = true
                        popUpTo("level_test_start") { inclusive = true }
                    }
                }
            )
        }

        // 메인(하단바)
        composable("main") {
            // ✅ main에서 시스템 백은 무시 → 앱 종료 방지
            BackHandler(enabled = true) { /* no-op */ }

            MainScreen(
                onStartStudyFlow = { navController.navigate("study_graph") { launchSingleTop = true } },
                onOpenSettings   = { navController.navigate("settings") },
                onOpenWordCollection = { navController.navigate("word_collection") } // ★ 추가
            )
        }

        // 학습 그래프 (루트: 주간 허브)
        navigation(
            route = "study_graph",
            startDestination = "study_weekly"
        ) {
            // 주간 허브
            composable("study_weekly") { backStackEntry ->
                // 루트 뒤로가기 → main으로(앱 종료 방지)
                StudyGraphBackHandler(navController)

                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)

                val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE) // "YYYY-MM-DD"
                val body = vm.quote.collectAsState().value
                val studiedDates by vm.studiedDates.collectAsState(initial = emptySet())

                LaunchedEffect(today) {
                    vm.refreshStudiedDatesForWeek(LocalDate.parse(today))
                }
                Scaffold(
                    bottomBar = {
                        BottomNavBar(navController = navController as NavHostController) }
                ) { innerPadding ->
                    Box(Modifier.padding(innerPadding)) {
                        StudyWeeklyScreen(
                            initialDateLabel = today,
                            onDateChange = { label ->
                                runCatching { LocalDate.parse(label) }.onSuccess { picked ->
                                    vm.fetchPastStudyByDate(picked)
                                    vm.refreshStudiedDatesForWeek(picked)
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
                            },
                            hasStudy = { day -> studiedDates.contains(day) }
                        )
                    }
                }

//                StudyWeeklyScreen(
//                    initialDateLabel = today,
//                    onDateChange = { label ->
//                        runCatching { LocalDate.parse(label) }.onSuccess { picked ->
//                            vm.fetchPastStudyByDate(picked)
//                            vm.refreshStudiedDatesForWeek(picked)
//                        }
//                    },
//                    bodyText = body,
//                    onBackClick = { navController.popBackStack() },
//                    onGoStudyClick = {
//                        navController.navigate("study_intro") {
//                            launchSingleTop = true
//                            restoreState = true
//                        }
//                    },
//                    onOpenPastStudy = { label ->
//                        navController.navigate("past_study/$label") {
//                            launchSingleTop = true
//                            restoreState = true
//                        }
//                    },
//                    hasStudy = { day -> studiedDates.contains(day) }
//                )
            }

            // 지난 학습 상세
            composable("past_study/{date}") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)

                val dateParam = backStackEntry.arguments?.getString("date") // "YYYY-MM-DD"
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

            // 1단계 인트로
            composable("study_intro") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)

                StudyIntroScreen(
                    onStart = { /* optional */ },
                    onNavigateNext = {
                        navController.navigate("study_reading") {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // 1단계 본문 → Appendix
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

            // Appendix → AppendixList
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

            // AppendixList → 2단계 Intro
            composable("appendix_list") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)

                val sidState = vm.studyId.collectAsState()
                val sid = sidState.value

                LaunchedEffect(sid) {
                    android.util.Log.d("NAV", ">> appendix_list (sid=$sid)")
                }

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

            // 2단계 Intro → 2단계 본문
            composable("study_second_intro") {
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

            // 2단계 본문 → 3단계 Intro
            composable("study_second") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val viewModel: StudyReadingViewModel = hiltViewModel(parentEntry)

                StudySecondScreen(
                    viewModel = viewModel,
                    onBackClick = {
                        navController.navigate("appendix_list") {      // ✅ 정확한 라우트명
                            popUpTo("study_second") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    //onBackClick = { navController.popBackStack() },
                    onNextClick = {
                        navController.navigate("study_third_intro") {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo("study_graph") { inclusive = false }
                        }
                    }
                )
            }

            // 3단계 Intro → 3단계 본문
            composable("study_third_intro") {
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

            // 3단계 본문 → 결과
            composable("study_third") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("study_graph")
                }
                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)

                val id = vm.studyId.collectAsState().value
                val text = vm.quote.collectAsState().value

                LaunchedEffect(id, text) {
                    if (id == null || text.isBlank()) {
                        vm.fetchTodayQuote()
                    }
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
                                popUpTo("study_graph") { inclusive = false }
                            }
                        }
                    )
                }
            }

            // 결과 → 완료
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

            // 완료 → 메인
            composable("study_third_complete") {
                val viewModel: StudyReadingViewModel = hiltViewModel()  // ViewModel 가져오기

                StudyCompleteScreen(
                    viewModel = viewModel,   // ★ viewModel 전달
                    onNextClick = {
                        navController.navigate("main") {
                            launchSingleTop = true
                            popUpTo("study_graph") { inclusive = true }
                        }
                    }
                )
            }
        }

        // 탭 라우트 → MainScreen으로 위임 (초간단 라우터)
        composable("quiz") {
            MainScreen(
                initialTab = "quiz", // 👈 MainScreen이 이 값을 보고 탭 선택
                onStartStudyFlow = { navController.navigate("study_graph") { launchSingleTop = true } }
                ,
                onOpenSettings   = { navController.navigate("settings") }
            )
        }
        composable("ai") {
            MainScreen(initialTab = "ai", onStartStudyFlow = { navController.navigate("study_graph") { launchSingleTop = true } },onOpenSettings   = { navController.navigate("settings") })
        }
        composable("friend") {
            MainScreen(initialTab = "friend", onStartStudyFlow = { navController.navigate("study_graph") { launchSingleTop = true } },onOpenSettings   = { navController.navigate("settings") })
        }
//        composable("mypage") {
//            MainScreen(initialTab = "mypage", onStartStudyFlow = { navController.navigate("study_graph") { launchSingleTop = true } },onOpenSettings   = { navController.navigate("settings") })
//        }

        composable("mypage") {
            com.malmungchi.feature.mypage.MyPageRoute(
                onClickSettings = { navController.navigate("settings") },
                onClickViewAllWords = { navController.navigate("word_collection") },
                onClickViewAllBadges = { /* TODO: 배지 전체보기 라우트 생기면 연결 */ }
            )
        }

        composable("settings") {
            SettingsScreen(
                onClickBack = { navController.popBackStack() },
                onClickRemind = { navController.navigate("remind_settings") },
                onClickLogout = { /* TODO */ },
                onClickWithdraw = { /* TODO */ }
            )
        }
        composable("remind_settings") {
            RemindSettingsScreen(
                onBack = { navController.popBackStack() },
                onSave = { list ->
                    // TODO: list(Ampm, hour, minute) 저장 로직 (서버/로컬)에 맞게 처리
                    navController.popBackStack()  // 저장 후 뒤로
                }
            )
        }
        // MainApp() 의 NavHost {...} 안
        composable("word_collection") {
            WordCollectionRoute(
                onBack = {
                    navController.navigate("mypage") {
                        popUpTo("word_collection") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
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


