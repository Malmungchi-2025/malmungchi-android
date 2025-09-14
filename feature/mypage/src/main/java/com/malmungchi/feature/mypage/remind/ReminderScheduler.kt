package com.malmungchi.feature.mypage.remind

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

enum class Ampm(val label: String) { AM("오전"), PM("오후") }

data class RemindTime(
    val ampm: Ampm,      // AM/PM
    val hour: String,    // "01".."12"
    val minute: String   // "00","10",...,"50"
)

object ReminderScheduler {
    private const val TAG = "REMIND"
    private const val PREF = "remind_prefs"
    private const val KEY_JSON = "remind_times_json"

    /** 저장 + 전체 재스케줄 (★ 기존 알람을 먼저 취소) */
    fun saveAndSchedule(context: Context, list: List<RemindTime>) {
        // 0) 이전에 저장되어 있던 알람 목록 확보
        val oldList = load(context)

        // 1) 이전 알람들 모두 취소
        cancelList(context, oldList)

        // 2) 새 목록 저장
        save(context, list)

        // 3) 새 알람 스케줄
        list.forEach { t ->
            val h24 = to24h(t.ampm, t.hour)
            val m = t.minute.toInt()
            scheduleOne(context, h24, m)
        }
        Log.d(TAG, "scheduled: ${list.map { "${it.ampm} ${it.hour}:${it.minute}" }}")
    }

    /** 주어진 목록의 알람만 취소(내부용) */
    private fun cancelList(context: Context, list: List<RemindTime>) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        list.forEach { t ->
            val h24 = to24h(t.ampm, t.hour)
            val m = t.minute.toInt()
            val pi = pendingIntent(context, h24, m)
            am.cancel(pi)
        }
    }

    /** 기존 저장된 목록 기준으로 전체 취소(외부 필요 시) */
    fun cancelAll(context: Context) {
        cancelList(context, load(context))
    }

    /** requestCode를 고유하게: HHMM(24h)로 사용 */
    private fun reqCodeOf(h24: Int, m: Int) = (h24 * 100) + m

    /** AM/PM + "01".."12" -> 24시간제 */
    private fun to24h(ampm: Ampm, hour: String): Int {
        val h12 = hour.toIntOrNull()?.coerceIn(1, 12) ?: 12
        return when (ampm) {
            Ampm.AM -> if (h12 == 12) 0 else h12
            Ampm.PM -> if (h12 == 12) 12 else h12 + 12
        }
    }

    /** 다음 트리거 millis 계산(오늘 시각이 지났으면 내일) */
    private fun nextTriggerMillis(h24: Int, m: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, h24)
            set(Calendar.MINUTE, m)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }


    /** 단일 예약(오늘/내일 한 번) — 수신 시 다음날로 다시 예약 */
    fun scheduleOne(context: Context, h24: Int, m: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerAt = nextTriggerMillis(h24, m)
        val pi = pendingIntent(context, h24, m)

        // 정확도 최우선
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)

        Log.d(TAG, "Alarm set: %02d:%02d -> %tF %tR".format(h24, m, triggerAt, triggerAt))
    }

    private fun pendingIntent(context: Context, h24: Int, m: Int): PendingIntent {
        val req = reqCodeOf(h24, m)
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.example.malmungchi.REMIND"
            putExtra("h24", h24)
            putExtra("minute", m)
        }
        return PendingIntent.getBroadcast(
            context,
            req,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** 부팅 후 복원용 */
    fun rescheduleAllFromPrefs(context: Context) {
        load(context).forEach { t ->
            scheduleOne(context, to24h(t.ampm, t.hour), t.minute.toInt())
        }
    }

    // ───── 간단한 SharedPreferences 직렬화(로컬만) ─────

    private fun save(context: Context, list: List<RemindTime>) {
        val json = buildString {
            append("[")
            append(list.joinToString(",") {
                """{"ampm":"${it.ampm.name}","hour":"${it.hour}","minute":"${it.minute}"}"""
            })
            append("]")
        }
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY_JSON, json).apply()
    }

    fun load(context: Context): List<RemindTime> {
        val s = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_JSON, "[]") ?: "[]"
        return runCatching {
            val arr = org.json.JSONArray(s)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                RemindTime(
                    Ampm.valueOf(o.getString("ampm")),
                    o.getString("hour"),
                    o.getString("minute")
                )
            }
        }.getOrDefault(emptyList())
    }
}