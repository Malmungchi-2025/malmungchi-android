package com.example.malmungchi


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AppSplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF195FCF)),
        contentAlignment = Alignment.TopCenter   // ⬅️ 위쪽 기준으로 정렬!
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 180.dp)  // ⬅️ 원하는 만큼 내리기
        ) {

            Image(
                painter = painterResource(id = R.drawable.img_splash_icon),
                contentDescription = "말뭉치 로고",
                modifier = Modifier.size(360.dp)
            )

            Spacer(Modifier.height(32.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_splash_malmungchi),
                contentDescription = "말뭉치 텍스트 로고",
                modifier = Modifier.height(40.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewAppSplashScreen() {
    AppSplashScreen()
}