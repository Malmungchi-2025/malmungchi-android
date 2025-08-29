package com.malmungchi.feature.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    autoAdvanceMillis: Long = 1500L,
) {
    val images = remember {
        listOf(
            R.drawable.img_onboard1,
            R.drawable.img_onboard2,
            R.drawable.img_onboard3,
            R.drawable.img_onboard4,
            R.drawable.img_onboard5,
            R.drawable.img_onboard6,
            R.drawable.img_onboard7,
            R.drawable.img_onboard8,
            R.drawable.img_onboard9,
        )
    }

    var index by remember { mutableStateOf(0) }

    LaunchedEffect(index) {
        if (index < images.lastIndex) {
            delay(autoAdvanceMillis)
            index += 1
        } else {
            delay(autoAdvanceMillis)
            onFinish()
        }
    }

    val onNext: () -> Unit = {
        if (index < images.lastIndex) index += 1 else onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // ripple 제거 (원치 않으면 빼도 됨)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onNext() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = images[index]),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            // ✅ 화면을 빈틈 없이 꽉 채움 (비율 유지, 가장자리 약간 크롭)
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreen(onFinish = {})
}