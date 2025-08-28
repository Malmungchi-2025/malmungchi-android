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

    LogNavDestinations(navController)

    // 시작은 splash에서 자동 로그인 여부 판단
    NavHost(navController, startDestination = "splash") {
        composable("splash") {
            LaunchedEffect(Unit) {
                // ✅ 항상 applicationContext 사용
                val appCtx = appContext.applicationContext

                // ✅ Triple은 3개로 받기
                val (uid, token, _) = readSession(appCtx)

                val isValid = if (uid != null && !token.isNullOrBlank()) {
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
                    runCatching {
                        withContext(Dispatchers.IO) { auth.me() }
                    }.fold(
                        onSuccess = { res -> res.success },
                        onFailure = { false }
                    )
                } else false

                if (isValid) {
                    navController.navigate("study_graph") {
                        popUpTo("splash") { inclusive = true }
                        launchSingleTop = true
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
            EmailLoginScreen(
                onBack = { navController.popBackStack() },
                onLoginSuccess = { userId, token ->
                    // 기존 세션 매니저 유지
                    com.malmungchi.data.session.SessionManager.set(userId, token)
                    // 자동 로그인 저장
                    saveSession(appContext, userId, token)

                    navController.navigate("study_graph") {
                        // 로그인/약관 스택 제거 → 뒤로가기 시 로그인으로 회귀 방지
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 메인(하단바)
        composable("main") {
            MainScreen(
                onStartStudyFlow = {
                    navController.navigate("study_graph") { launchSingleTop = true }
                }
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
                    onBackClick = { navController.popBackStack() },
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
                        onBackClick = { navController.popBackStack() },
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
                        onBackClick = { navController.popBackStack() },
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
            )
        }
        composable("ai") {
            MainScreen(initialTab = "ai", onStartStudyFlow = { navController.navigate("study_graph") { launchSingleTop = true } })
        }
        composable("friend") {
            MainScreen(initialTab = "friend", onStartStudyFlow = { navController.navigate("study_graph") { launchSingleTop = true } })
        }
        composable("mypage") {
            MainScreen(initialTab = "mypage", onStartStudyFlow = { navController.navigate("study_graph") { launchSingleTop = true } })
        }


    }
}




//
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import androidx.hilt.navigation.compose.hiltViewModel
//import androidx.navigation.navigation
//import com.example.malmungchi.navigation.MainScreen
//import com.malmungchi.feature.login.LoginScreen
//import com.malmungchi.feature.study.first.StudyIntroScreen
//import com.malmungchi.feature.study.first.StudyReadingScreen
//import com.malmungchi.feature.study.first.StudyAppendixScreen
//import com.malmungchi.feature.study.first.StudyAppendixListScreen
//import com.malmungchi.feature.study.StudyReadingViewModel
//import com.malmungchi.feature.study.second.StudySecondIntroScreen
//import com.malmungchi.feature.study.second.StudySecondScreen
//import com.malmungchi.feature.study.third.StudyCompleteScreen
//import com.malmungchi.feature.study.third.StudyResultQuestion
//import com.malmungchi.feature.study.third.StudyThirdIntroScreen
//import com.malmungchi.feature.study.third.StudyThirdResultScreen
//import com.malmungchi.feature.study.third.StudyThirdResultScreenWrapper
//import com.malmungchi.feature.study.third.StudyThirdScreen
//import androidx.compose.material3.Text
//import androidx.compose.runtime.getValue
//import com.example.malmungchi.navigation.BottomNavItem
//import com.example.malmungchi.navigation.LogNavDestinations
//import com.example.malmungchi.navigation.TermsRoute
//
//import com.malmungchi.feature.login.AppTermsScreen
//import com.malmungchi.feature.login.EmailLoginScreen
//import com.malmungchi.feature.login.MarketingTermsScreen
//import com.malmungchi.feature.login.PrivacyTermsScreen
//import com.malmungchi.feature.login.SignUpFlowScreen
//import com.malmungchi.feature.login.SignUpRoute
//import com.malmungchi.feature.login.TermsAgreementScreen
//import com.malmungchi.feature.login.TermsDetailScreen
//import com.malmungchi.feature.login.sampleAppTerms
//import com.malmungchi.feature.login.sampleMarketingTerms
//import com.malmungchi.feature.login.samplePrivacyTerms
//import com.malmungchi.feature.study.intro.PastStudyScreenRoute
//import com.malmungchi.feature.study.intro.StudyWeeklyScreen
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//
//
//@Composable
//fun MainApp() {
//    val navController = rememberNavController()
//
//    LogNavDestinations(navController) // 아래 보조 컴포저블
//    NavHost(navController, startDestination = "login") {
//
//
//    // 0) 인트로
//        composable("login") {
//            LoginScreen(
//                onEmailLogin = {   // “로그인하기” 텍스트 → 이메일 로그인 화면
//                    navController.navigate("email_login") { launchSingleTop = true }
//                },
//                onSignUp = {       // 회원가입 버튼 → 회원가입 화면(미구현 시 TODO)
//                    // navController.navigate("sign_up") { launchSingleTop = true }
//                    navController.navigate(TermsRoute.Agreement) //약관 동의 페이지.
//                },
//                onKakao = { /* ... */ },
//                onNaver = { /* ... */ },
//                onGoogle = { /* ... */ }
//            )
//        }
//
//
//        // 약관 페이지
//        composable(TermsRoute.Agreement) {
//            TermsAgreementScreen(
//                onOpenAppTerms = { navController.navigate(TermsRoute.App) },
//                onOpenPrivacy = { navController.navigate(TermsRoute.Privacy) },
//                onOpenMarketing = { navController.navigate(TermsRoute.Marketing) },
//                onAgreeContinue = {
//                    navController.navigate("sign_up_flow") {
//                        popUpTo(TermsRoute.Agreement) { inclusive = true }
//                        launchSingleTop = true
//                    }
//                }
//            )
//        }
//        // 앱 서비스 이용약관
//        composable(TermsRoute.App) {
//            AppTermsScreen(
//                onBack = { navController.popBackStack() },
//                onDone = { navController.popBackStack() }
//            )
//        }
//
//// 개인정보
//        composable(TermsRoute.Privacy) {
//            PrivacyTermsScreen(
//                onBack = { navController.popBackStack() },
//                onDone = { navController.popBackStack() }
//            )
//        }
//
//// 마케팅
//        composable(TermsRoute.Marketing) {
//            MarketingTermsScreen(
//                onBack = { navController.popBackStack() },
//                onDone = { navController.popBackStack() }
//            )
//        }
//
////        // 회원가입 단계형 UI (이름 → 이메일/OTP → 비밀번호)
////        composable("sign_up_flow") {
////            SignUpFlowScreen(
////                onBack = { navController.popBackStack() },
////
////                // 서버 연동 전: true 반환으로만 처리해 UI 흐름 확인
////                onRequestEmailOtp = { _ -> true },
////                onVerifyEmailOtp = { _, _ -> true },
////
////                // 가입 완료 → 메인으로 진입 (login 스택 정리)
////                onDone = { name, email, _ ->
////                    // TODO: 실제 가입 API 호출 자리
////                    navController.navigate("main") {
////                        popUpTo("login") { inclusive = true }
////                        launchSingleTop = true
////                    }
////                }
////            )
////        }
//        // 회원가입 단계형 UI (이름 → 이메일/OTP → 비밀번호)
//        // 기존
//        composable("sign_up_flow") {
//            SignUpRoute(
//                onBack = { navController.popBackStack() },
//                onRegistered = {
//                    // 가입 성공 시 이동 (기존: main)
//                    // navController.navigate("main") {
//                    //     popUpTo("login") { inclusive = true }
//                    //     launchSingleTop = true
//                    // }
//
//                    //  변경: 가입 성공 → 이메일 로그인 화면으로 이동
//                    navController.navigate("email_login") {
//                        // 로그인 전 플로우(약관/회원가입) 스택은 정리하고,
//                        // login 은 남겨둔 뒤 email_login 을 올린다.
//                        popUpTo("login") { inclusive = false }
//                        launchSingleTop = true
//                    }
//                }
//            )
//        }
//
////        // 앱 서비스 이용약관
////        composable(TermsRoute.App) {
////            AppTermsScreen(
////                agreed = false, // 상태 hoisting 가능
////                onAgreeChange = { /* 필요 시 상태 저장 */ },
////                onBack = { navController.popBackStack() },
////                onDone = { navController.popBackStack() } // 완료도 뒤로
////            )
////        }
////
////        // 개인정보
////        composable(TermsRoute.Privacy) {
////            PrivacyTermsScreen(
////                agreed = false,
////                onAgreeChange = { },
////                onBack = { navController.popBackStack() },
////                onDone = { navController.popBackStack() }
////            )
////        }
////
////        // 마케팅
////        composable(TermsRoute.Marketing) {
////            MarketingTermsScreen(
////                agreed = false,
////                onAgreeChange = { },
////                onBack = { navController.popBackStack() },
////                onDone = { navController.popBackStack() }
////            )
////        }
//
//    //  신규: 이메일/비번 폼 화면
//        composable("email_login") {
//            EmailLoginScreen(
//                onBack = { navController.popBackStack() },
//                onLoginSuccess = { userId, token ->
//                    com.malmungchi.data.session.SessionManager.set(userId, token)
//
//                    navController.navigate("study_graph") {
//                        popUpTo("login") { inclusive = true }
//                        launchSingleTop = true
//                    }
//                }
//            )
//        }
//
//
//    // 1) 메인(하단바) - 로그인 후 진입
//        composable("main") {
//            MainScreen(
//                onStartStudyFlow = {
//                    navController.navigate("study_graph") { launchSingleTop = true }
//                }
//            )
//        }
//
//        // ✅ 1) startDestination을 주간 허브로
//        navigation(
//            route = "study_graph",
//            startDestination = "study_weekly"   // << 기존 "study_intro" 에서 변경
//        ) {
//            // ✅ 2) 주간 허브 화면
//            composable("study_weekly") { backStackEntry ->
//                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("study_graph") }
//                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
//
//
//
//                val today = LocalDate.now().format(DateTimeFormatter.ISO_DATE) // "YYYY-MM-DD"
//                val body = vm.quote.collectAsState().value
//
//                // ✅ 학습한 날짜들(yyyy-MM-dd) 수집
//                //val studiedDates = vm.studiedDates.collectAsState(initial = emptySet()).value
//                val studiedDates by vm.studiedDates.collectAsState(initial = emptySet())
//                //val studiedDates by vm.studiedDates.collectAsState(initial = emptySet())
//
//                // 진입 직후 첫 주 데이터를 미리 당겨오기
//                LaunchedEffect(today) {
//                    vm.refreshStudiedDatesForWeek(LocalDate.parse(today))
//                }
//
//
//                // 프리뷰용(있으면 표시)
//                StudyWeeklyScreen(
//                    initialDateLabel = today,
//                    // 날짜 바뀔 때마다 프리뷰 불러오기 (404면 뷰모델에서 에러 문구 세팅됨)
//                    onDateChange = { label ->
//                        runCatching { LocalDate.parse(label) }.onSuccess { picked ->
//                            // 1) 해당 날짜 본문 요청 (기존)
//                            vm.fetchPastStudyByDate(picked)
//
//                            // 2) ✅ 이 날짜가 포함된 '주'의 학습일 목록 새로고침 (신규)
//                            vm.refreshStudiedDatesForWeek(picked)
//                        }
//                    },
//                    bodyText = body,
//                    onBackClick = { navController.popBackStack() },
//                    onGoStudyClick = {
//                        // “학습하러 가기 >” → 인트로로
//                        navController.navigate("study_intro") {
//                            launchSingleTop = true
//                            restoreState = true
//                        }
//                    },
//                    onOpenPastStudy = { label ->
//                        // 날짜 탭 → 지난 학습 상세
//                        navController.navigate("past_study/$label") {
//                            launchSingleTop = true
//                            restoreState = true
//                        }
//                    },
//                    // ✅ 여기 한 줄 때문에 컴파일 에러가 사라지고, 파란 칩이 뜹니다.
//                    hasStudy = { day -> studiedDates.contains(day) }
////                    hasStudy = { day -> studiedDates.any { it.take(10) == day } }
//                )
//            }
//
//            // ✅ 3) 지난 학습 상세 화면
//            composable("past_study/{date}") { backStackEntry ->
//                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("study_graph") }
//                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
//
//                val dateParam = backStackEntry.arguments?.getString("date") // "YYYY-MM-DD"
//                val localDate = dateParam?.let { LocalDate.parse(it) }
//
//                // 진입 시 해당 날짜 통합 조회
//                LaunchedEffect(dateParam) {
//                    localDate?.let { vm.fetchPastStudyByDate(it) }
//                }
//
//                PastStudyScreenRoute(
//                    dateLabel = dateParam?.replace("-", ".") ?: "", // 표시 전용: "YYYY.MM.DD"
//                    viewModel = vm,
//                    onLoad = null,                                  // 위 LaunchedEffect에서 호출
//                    onBackClick = { navController.popBackStack() }
//                )
//            }
//
//            // ✅ 기존 인트로/리딩 이하 플로우는 그대로 유지
//            composable("study_intro") { backStackEntry ->
//                android.util.Log.d("NAV", ">> study_intro")
//                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("study_graph") }
//                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
//
//                StudyIntroScreen(
//                    onStart = { /* optional */ },
//                    onNavigateNext = {
//                        android.util.Log.d("NAV", ">> onNavigateNext: study_reading로 이동 시도")
//                        navController.navigate("study_reading") {
//                            launchSingleTop = true
//                            restoreState = true
//                        }
//                    }
//                )
//            }
//
//
//            // 2️⃣ StudyReadingScreen → Appendix
//            composable("study_reading") { backStackEntry ->
//                android.util.Log.d("NAV", ">> study_reading")
//                val parentEntry = remember(backStackEntry) {
//                    navController.getBackStackEntry("study_graph")
//                }
//                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
//                StudyReadingScreen(
//                    viewModel = vm,
//                    onNextClick = {
//                        navController.navigate("appendix") {
//                            launchSingleTop = true
//                            restoreState = true
//                            popUpTo("study_graph") { inclusive = false } // ✅
//                        }
//
//                    },
//                    onBackClick = { navController.popBackStack() }
//                )
//            }
//
//            // 3️⃣ Appendix → 3초 후 AppendixList
//            composable("appendix") {
//                android.util.Log.d("NAV", ">> appendix")
//                StudyAppendixScreen(
//                    onNavigateNext = {
//                        navController.navigate("appendix_list") {
//                            launchSingleTop = true
//                            restoreState = true
//                            popUpTo("study_graph") { inclusive = false } // ✅
//                        }
//                    }
//                )
//            }
//
//            // 4️⃣ AppendixList → 2단계 Intro
//            composable("appendix_list") { backStackEntry ->
//                // ✅ study_graph 스코프의 공유 ViewModel 사용
//                val parentEntry = remember(backStackEntry) {
//                    navController.getBackStackEntry("study_graph")
//                }
//                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
//
//                // ⚠️ sid 먼저 선언
//                val sidState = vm.studyId.collectAsState()
//                val sid = sidState.value
//
//                // ✅ sid 값 로그는 선언 이후에
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
//                        studyId = sid,       // ✅ 하드코딩 제거
//                        viewModel = vm,
//                        onBackClick = {
//                            navController.navigate("study_reading") {
//                                launchSingleTop = true
//                                restoreState = true
//                                popUpTo("study_graph") { inclusive = false } // ✅
//                            }
//
//                        },
//                        onNavigateNext = {
//                            navController.navigate("study_second_intro") {
//                                launchSingleTop = true
//                                restoreState = true
//                                popUpTo("study_graph") { inclusive = false }  // ✅ 그래프 루트
//                            }
//                        }
//                    )
//                }
//            }
//
//            // ✅ 5️⃣ 2단계 Intro → 2단계 본문 화면
//            composable("study_second_intro") {
//                StudySecondIntroScreen(
//                    onNavigateNext = {
//                        navController.navigate("study_second") {
//                            launchSingleTop = true
//                            restoreState = true
//                            popUpTo("study_graph") { inclusive = false } // ✅
//                        }
//                    }
//                )
//            }
//
//            // ✅ 6️⃣ 2단계 본문 화면 (StudySecondScreen 연결)
//            // StudySecondScreen → onNextClick 에서 다음으로 이동
//            composable("study_second") { backStackEntry ->   // ✅ 파라미터 추가
//                val parentEntry = remember(backStackEntry) {
//                    navController.getBackStackEntry("study_graph")
//                }
//                val viewModel: StudyReadingViewModel = hiltViewModel(parentEntry) // ✅ 그래프 스코프 공유
////            composable("study_second") {
//////                val viewModel = hiltViewModel<StudyReadingViewModel>()
////                val parentEntry = remember(backStackEntry) { navController.getBackStackEntry("study_graph") }
////                val viewModel: StudyReadingViewModel = hiltViewModel(parentEntry) // ✅ 그래프 스코프 공유
//                StudySecondScreen(
//                    //token = "dummy_token",
//                    viewModel = viewModel,
//                    onBackClick = { navController.popBackStack() },
//                    onNextClick = {
//                        navController.navigate("study_third_intro") {
//                            launchSingleTop = true
//                            restoreState = true
//                            popUpTo("study_graph") { inclusive = false }     // ✅ 그래프 루트
//                        }
//                    }
//                )
//            }
//
//            // ✅ 7️⃣ StudyThirdIntroScreen → 3초 후 StudyThirdScreen
//            composable("study_third_intro") {
//                StudyThirdIntroScreen(
//                    onNavigateNext = {
//                        navController.navigate("study_third") {
//                            launchSingleTop = true
//                            restoreState = true
//                            popUpTo("study_graph") { inclusive = false } // ✅
//                        }
//                    }
//                )
//            }
//            composable("study_third") { backStackEntry ->
//                val parentEntry = remember(backStackEntry) {
//                    navController.getBackStackEntry("study_graph")
//                }
//                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
//
//                val token = "dummy_token"
//                val id = vm.studyId.collectAsState().value
//                val text = vm.quote.collectAsState().value
//
//                // 🔁 혹시 이전 단계에서 못 채웠다면 여기서라도 한 번 확보
//                LaunchedEffect(id, text) {
//                    if (id == null || text.isBlank()) {
//                        //vm.fetchTodayQuote(token)   // 최소한 진행 가능하도록 방어
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
//                        //token = token,
//                        studyId = id,
//                        text = text,
//                        viewModel = vm,
//                        onBackClick = { navController.popBackStack() },
//                        onNextClick = {
//                            navController.navigate("study_third_result/$id") {
//                                launchSingleTop = true
//                                restoreState = true
//                                popUpTo("study_graph") { inclusive = false } // ✅
//                            }
//
//                        }
//                    )
//                }
//            }
//
//            composable("study_third_result/{studyId}") { backStackEntry ->
//                val parentEntry = remember(backStackEntry) {
//                    navController.getBackStackEntry("study_graph")
//                }
//                val vm: StudyReadingViewModel = hiltViewModel(parentEntry)
//
//                val id = backStackEntry.arguments?.getString("studyId")?.toIntOrNull()
//                if (id == null) {
//                    // 안전 처리
//                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        Text("잘못된 접근입니다.")
//                    }
//                } else {
//                    StudyThirdResultScreenWrapper(
//                        //token = "dummy_token",
//                        studyId = id,
//                        viewModel = vm,
//                        onBackClick = { navController.popBackStack() },
//                        onFinishClick = {
//                            navController.navigate("study_third_complete") {
//                                launchSingleTop = true
//                                restoreState = true
//                                popUpTo("study_graph") { inclusive = false } // ✅
//                            }
//                        }
//                    )
//                }
//            }
//
////            // ✅ 8️⃣ StudyThirdScreen (3단계 본문)
////            composable("study_third") {
////                val viewModel = hiltViewModel<StudyReadingViewModel>()
////                val token = "dummy_token" // TODO 실제 토큰
////                val studyIdState = viewModel.studyId.collectAsState()
////                val quoteState = viewModel.quote.collectAsState()
////
////                val id = studyIdState.value
////                val text = quoteState.value
////
////                if (id == null || text.isBlank()) {
////                    Box(
////                        Modifier.fillMaxSize(),
////                        contentAlignment = Alignment.Center
////                    ) { CircularProgressIndicator() }
////                } else {
////                    StudyThirdScreen(
////                        token = token,
////                        studyId = id,
////                        text = text,
////                        viewModel = viewModel,
////                        onBackClick = { navController.popBackStack() },
////                        onNextClick = {
////                            navController.navigate("study_third_result/$id") {
////                                popUpTo("study_third") { inclusive = true }
////                            }
////                        }
////                    )
////                }
////            }
////
////            // ✅ 결과 화면: studyId 파라미터 받음
////            composable("study_third_result/{studyId}") { backStackEntry ->
////                val id = backStackEntry.arguments?.getString("studyId")!!.toInt()
////                val viewModel = hiltViewModel<StudyReadingViewModel>()
////                val token = "dummy_token" // TODO 실제 토큰
////
////                StudyThirdResultScreenWrapper(
////                    token = token,
////                    studyId = id,
////                    viewModel = viewModel,
////                    onBackClick = { navController.popBackStack() },
////                    onFinishClick = {
////                        navController.navigate("study_third_complete") {
////                            popUpTo("study_third") { inclusive = true }
////                        }
////                    }
////                )
////            }
//
//            // ✅ 🔟 StudyCompleteScreen (완료 화면)
//            composable("study_third_complete") {
//                StudyCompleteScreen(
//                    onNextClick = {
//                        // 완료 후 메인으로
//                        navController.navigate("main") {
//                            launchSingleTop = true
//                            popUpTo("study_graph") { inclusive = true } // ✅ 학습 플로우 전체 비움
//                        }
////                        navController.navigate("main") {
////                            popUpTo("study_third_complete") { inclusive = true }
////                        }
//                    }
//                )
//            }
//        }
//
//
//    }
//}

//// 7️⃣ 메인 화면
//composable("main") { MainScreen() }
//
//// 8️⃣ 로그인 화면
//composable("login") {
//    LoginScreen(
//        onLoginSuccess = {
//            navController.navigate("main") {
//                popUpTo("login") { inclusive = true }
//            }
//        }
//    )
//}
//import androidx.compose.runtime.Composable
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.rememberNavController
//import com.example.malmungchi.navigation.MainScreen
//import com.malmungchi.feature.login.LoginScreen
//import com.malmungchi.feature.study.first.StudyIntroScreen
//import com.malmungchi.feature.study.first.StudyReadingScreen
//import androidx.hilt.navigation.compose.hiltViewModel
//import com.malmungchi.feature.study.StudyReadingViewModel
//import com.malmungchi.feature.study.first.StudyAppendixScreen
//import com.malmungchi.feature.study.first.StudyAppendixListScreen
//import com.malmungchi.feature.study.second.StudySecondScreen
//
//@Composable
//fun MainApp() {
//    val navController = rememberNavController()
//
//    NavHost(
//        navController = navController,
//        startDestination = "study_intro"
//    ) {
//        // 1️⃣ Intro → 3초 후 ReadingScreen
//        composable("study_intro") {
//            StudyIntroScreen(
//                onNavigateNext = {
//                    navController.navigate("study_reading") {
//                        popUpTo("study_intro") { inclusive = true }
//                    }
//                }
//            )
//        }
//
//        // 2️⃣ StudyReadingScreen → Appendix
//        composable("study_reading") {
//            val viewModel = hiltViewModel<StudyReadingViewModel>()
//            StudyReadingScreen(
//                viewModel = viewModel,
//                onNextClick = {
//                    navController.navigate("appendix") {   // ✅ 수정됨
//                        popUpTo("study_reading") { inclusive = true }
//                    }
//                },
//                onBackClick = { navController.popBackStack() }
//            )
//        }
//
//        // 3️⃣ Appendix → 3초 후 AppendixList
//        composable("appendix") {
//            StudyAppendixScreen(
//                onNavigateNext = {
//                    navController.navigate("appendix_list") {   // ✅ 수정됨
//                        popUpTo("appendix") { inclusive = true }
//                    }
//                }
//            )
//        }
//
//        // MainApp.kt
//        composable("appendix_list") {
//            val viewModel = hiltViewModel<StudyReadingViewModel>()
//            StudyAppendixListScreen(
//                token = "dummy_token",
//                studyId = 1,
//                viewModel = viewModel,
//                onBackClick = {
//                    navController.navigate("study_reading") {   // ✅ 명시적으로 이동
//                        popUpTo("appendix_list") { inclusive = true }
//                    }
//                },
//                onNavigateNext = {
//                    navController.navigate("main") {
//                        popUpTo("appendix_list") { inclusive = true }
//                    }
//                }
//            )
//        }
//        composable("study_second_intro") {
//            StudySecondIntroScreen(
//                onNavigateNext = {
//                    navController.navigate("study_second") {   // ✅ 2단계 본문 화면으로 이동
//                        popUpTo("study_second_intro") { inclusive = true }
//                    }
//                }
//            )
//        }
//
//        // 5️⃣ 메인 화면
//        composable("main") { MainScreen() }
//
//        // 6️⃣ 로그인 화면
//        composable("login") {
//            LoginScreen(
//                onLoginSuccess = {
//                    navController.navigate("main") {
//                        popUpTo("login") { inclusive = true }
//                    }
//                }
//            )
//        }
//    }
//}



//@Composable
//fun MainApp() {
//    val navController = rememberNavController()
//
//    NavHost(
//        navController = navController,
//        startDestination = "study_intro"
//    ) {
//        // 1️⃣ Intro → 3초 후 ReadingScreen
//        composable("study_intro") {
//            StudyIntroScreen(
//                onNavigateNext = {
//                    navController.navigate("study_reading") {
//                        popUpTo("study_intro") { inclusive = true }
//                    }
//                }
//            )
//        }
//
//        // 2️⃣ StudyReadingScreen (버튼 클릭 → main)
//        composable("study_reading") {
//            val viewModel = hiltViewModel<StudyReadingViewModel>()
//            StudyReadingScreen(
//                viewModel = viewModel,
//                onNextClick = {
//                    navController.navigate("main") {
//                        popUpTo("study_reading") { inclusive = true }
//                    }
//                },
//                onBackClick = {
//                    navController.popBackStack()
//                }
//            )
//        }
//
//        // 3️⃣ Appendix (3초 후 → main)
//        composable("appendix") {
//            StudyAppendixScreen(
//                onNavigateNext = {
//                    navController.navigate("main") {   // ✅ appendix → main
//                        popUpTo("appendix") { inclusive = true }
//                    }
//                }
//            )
//        }
//
//        // 4️⃣ AppendixList (단어 리스트 → main)
//        composable("appendix_list") {
//            val viewModel = hiltViewModel<StudyReadingViewModel>()
//            // ✅ API는 나중에 연결, 지금은 목업으로만 호출
//            StudyAppendixListScreen(
//                token = "dummy_token",
//                studyId = 1,  // 일단 더미 ID
//                viewModel = viewModel,
//                onBackClick = { navController.popBackStack() },
//                onNavigateNext = {
//                    navController.navigate("main") {
//                        popUpTo("appendix_list") { inclusive = true }
//                    }
//                }
//            )
//        }
//
//        // 5️⃣ 메인 화면
//        composable("main") { MainScreen() }
//
//        // 6️⃣ 로그인 화면 (현재 미사용)
//        composable("login") {
//            LoginScreen(
//                onLoginSuccess = {
//                    navController.navigate("main") {
//                        popUpTo("login") { inclusive = true }
//                    }
//                }
//            )
//        }
//    }
//}

//@Composable
//fun MainApp() {
//    val navController = rememberNavController()
//
//    NavHost(
//        navController = navController,
//        startDestination = "login"   // ✅ 첫 화면은 로그인
//    ) {
//        composable("login") {
//            LoginScreen(
//                onLoginSuccess = {
//                    navController.navigate("main") {
//                        popUpTo("login") { inclusive = true } // 로그인 화면 제거
//                    }
//                }
//            )
//        }
//        composable("main") { MainScreen() }  // ✅ 메인 네비게이션 화면
//    }
//}