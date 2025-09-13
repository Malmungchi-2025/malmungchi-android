package com.example.malmungchi
//super.onCreate(savedInstanceState)

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.malmungchi.ui.theme.MalmungchiTheme
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SplashScreen 설치
        installSplashScreen()

        // 권한 확인 후 요청
        if (!isPermissionGranted()) {
            requestPermissions()
        }

        setContent {
            MainApp()  // 메인 앱 실행
        }
    }

    // 권한 체크 함수
    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
    }

    // 권한 요청 함수
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    // 권한 요청 결과 처리 함수
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용됨
                Toast.makeText(this, "권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                // 권한이 거부됨
                Toast.makeText(this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 이미지 저장 함수
    fun saveImageToStorage(bitmap: Bitmap) {
        // 파일을 저장할 경로
        val storageDir = File(getExternalFilesDir(null), "MalmungchiImages")
        if (!storageDir.exists()) {
            storageDir.mkdir()
        }

        val fileToSave = File(storageDir, "nickname_card.png")

        try {
            FileOutputStream(fileToSave).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)  // PNG 형식으로 압축 후 저장
            }

            Toast.makeText(this, "이미지가 저장되었습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "이미지 저장 실패.", Toast.LENGTH_SHORT).show()
        }
    }

    // Bitmap 생성 함수 (화면에 표시된 이미지를 Bitmap으로 변환)
    fun getBitmapFromDrawable(drawableId: Int): Bitmap {
        val drawable = resources.getDrawable(drawableId, theme)
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    // 이미지 저장을 트리거하는 함수
    fun onSaveImageClicked(nickname: String) {
        val imageResId = getNicknameCardImageRes(nickname) // 닉네임에 맞는 이미지 리소스 ID 가져오기
        val bitmap = getBitmapFromDrawable(imageResId) // 이미지 리소스를 Bitmap으로 변환
        saveImageToStorage(bitmap)  // 저장
    }
    // 별명에 맞는 이미지 리소스를 반환하는 함수
    private fun getNicknameCardImageRes(nickname: String): Int {
        return when (nickname) {
            "언어연금술사" -> R.drawable.img_word_magician
            "눈치번역가" -> R.drawable.img_sense
            "감각해석가" -> R.drawable.img_sense2
            "맥락추리자" -> R.drawable.img_context
            "언어균형술사" -> R.drawable.img_language
            "낱말여행자" -> R.drawable.img_word2
            "단어수집가" -> R.drawable.img_word3
            "의미해석가" -> R.drawable.img_context2
            "언어모험가" -> R.drawable.img_language2
            else -> R.drawable.img_word_magician
        }
    }
}