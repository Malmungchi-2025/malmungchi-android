package com.malmungchi.feature.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TermsDetailScreen(
    title: String,
    body: String,
    onBack: () -> Unit
) {
//    Column(Modifier.fillMaxSize()) {
//        TopAppBar(
//            title = { Text(title) },
//            navigationIcon = {
//                IconButton(onClick = onBack) {
//                    Icon(Icons.Outlined.ArrowBack, contentDescription = "뒤로")
//                }
//            }
//        )
        Divider()

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = body, lineHeight = 22.sp)
            Spacer(Modifier.height(24.dp))
        }
    }


val sampleAppTerms = "앱 서비스 이용약관 본문 …"
val samplePrivacyTerms = "개인정보 수집 및 이용 본문 …"
val sampleMarketingTerms = "마케팅 활용 동의 본문 …"