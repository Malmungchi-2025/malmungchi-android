package com.malmungchi.feature.mypage.remind

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.malmungchi.feature.mypage.R

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val h24 = intent.getIntExtra("h24", 8)
        val m = intent.getIntExtra("minute", 0)

        // 1) 채널 보장
        ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 2) 알림 탭 시 앱 열기 — 런처 인텐트(패키지명/모듈 변경에 안전)
        val tapIntent = context.packageManager
            .getLaunchIntentForPackage(context.packageName)
            ?.apply {
                // 이미 실행 중이면 최상단으로, 없으면 새 태스크로
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val contentPi: PendingIntent? = tapIntent?.let {
            PendingIntent.getActivity(
                context,
                /* requestCode = */ 0,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // 3) 알림 빌드
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            // 꼭 24×24dp 단색 아이콘이어야 함(미리 R.drawable.ic_pencil 유효 확인!)
            .setSmallIcon(R.drawable.ic_pencil)
            .setContentTitle("오늘의 학습 리마인드")
            .setContentText("지금 잠깐, 오늘 공부 한 번 하고 갈까요?")
            .setAutoCancel(true)
            .apply { if (contentPi != null) setContentIntent(contentPi) }
            .build()

        // id를 HHMM으로 고유화
        nm.notify(h24 * 100 + m, notification)

        // 4) 다음날로 자동 재예약(매일 반복 효과)
        ReminderScheduler.scheduleOne(context, h24, m)
    }

    companion object {
        private const val CHANNEL_ID = "remind_daily"
        private const val CHANNEL_NAME = "학습 리마인드"

        private fun ensureChannel(context: Context) {
            // Android O(API 26)+ 에서만 채널 생성
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val ch = android.app.NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "매일 최대 두 번 도착하는 학습 리마인드 알림"
                    enableLights(true)
                    lightColor = Color.BLUE
                    enableVibration(true)
                }
                nm.createNotificationChannel(ch)
            }
            // API 25 이하는 채널 개념이 없으므로 아무 것도 안 해도 됨
        }
    }
}
