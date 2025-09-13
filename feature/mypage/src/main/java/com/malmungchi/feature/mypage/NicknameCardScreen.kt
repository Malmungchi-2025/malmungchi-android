import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.malmungchi.feature.mypage.R as MyPageR
import com.malmungchi.core.designsystem.Pretendard
import com.malmungchi.feature.mypage.MyPageViewModel

@Composable
fun NicknameCardScreen(
    navController: NavHostController,
    userName: String,
    nickname: String,
    onExit: () -> Unit = {},
    onSaveImage: (String) -> Unit = {}
) {
    // ViewModel 초기화 (MyPageViewModel 또는 별명에 관련된 ViewModel을 사용)
    val viewModel: MyPageViewModel = hiltViewModel()

    val ui by viewModel.ui.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp)
                .align(Alignment.Center) // 화면 중앙 정렬
        ) {
            // 헤더
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
            ) {
                IconButton(onClick = onExit) {
                    Icon(
                        painter = painterResource(id = MyPageR.drawable.ic_back),
                        contentDescription = "뒤로가기"
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "별명 카드",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    fontFamily = Pretendard,
                    color = Color.Black,
                    modifier = Modifier
                        .padding(start = 80.dp) // 왼쪽 여백 추가하여 텍스트 위치 조정
                )
            }

            // 별명 이미지 (카드 X, 이미지만 좌우 20dp 여백)
            Box(
                modifier = Modifier
                    .fillMaxWidth() // Box의 가로를 꽉 채움
                    .padding(horizontal = 20.dp) // 좌우 여백 추가
            ) {
                Image(
                    painter = painterResource(id = getNicknameCardImageRes(nickname)),
                    contentDescription = "별명 카드 이미지",
                    contentScale = ContentScale.Crop, // 이미지를 꽉 채움
                    modifier = Modifier
                        .fillMaxWidth() // Box의 가로를 꽉 채움
                        .height(600.dp) // 이미지의 높이를 더 키움
                        .clip(RoundedCornerShape(16.dp)) // 모서리 둥글게 처리
                        .padding(top = 0.dp) // 이미지 위치를 조정하려면 여백 조정
                )
                // 카드 이미지 위에 사용자 텍스트
                Text(
                    text = "$userName 님의 별명은",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Pretendard,
                    color = Color.Black,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp) // 텍스트 위치를 조금 더 아래로 내릴 수도 있음
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            // 버튼 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 48.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onSaveImage(nickname) }, // 닉네임에 맞는 이미지 저장
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF195FCF)
                    ),
                    border = BorderStroke(2.dp, Color(0xFF195FCF)),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text(
                        text = "카드 이미지 저장",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Pretendard,
                        color = Color(0xFF195FCF),
                        textAlign = TextAlign.Center
                    )
                }
                Button(
                    onClick = onExit,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF195FCF),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Text(
                        text = "나가기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = Pretendard,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
// 이미지 매핑 함수 (기존 유지)
private fun getNicknameCardImageRes(nickname: String): Int {
    return when (nickname) {
        "언어연금술사" -> MyPageR.drawable.img_word_magician
        "눈치번역가" -> MyPageR.drawable.img_sense
        "감각해석가" -> MyPageR.drawable.img_sense2
        "맥락추리자" -> MyPageR.drawable.img_context
        "언어균형술사" -> MyPageR.drawable.img_language
        "낱말여행자" -> MyPageR.drawable.img_word2
        "단어수집가" -> MyPageR.drawable.img_word3
        "의미해석가" -> MyPageR.drawable.img_context2
        "언어모험가" -> MyPageR.drawable.img_language2
        else -> MyPageR.drawable.img_word_magician
    }
}

//@Preview(showBackground = true)
//@Composable
//fun NicknameCardScreenPreview() {
//    NicknameCardScreen(
//        userName = "김뭉치",
//        nickname = "언어연금술사",
//        onExit = {},
//        onSaveImage = {}
//    )
//}