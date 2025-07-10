package com.waroengweb.absensi

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

class BootCompletedReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.BOOT_COMPLETED"){
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar[Calendar.HOUR_OF_DAY] = 13
            calendar[Calendar.MINUTE] = 1
            calendar[Calendar.SECOND] = 0
            calendar[Calendar.MILLISECOND] = 0

            val alarmTimeInMillis = calendar.timeInMillis
            val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    112233,
                    Intent(context, AlarmReceiver::class.java).putExtra(
                        "ALARM_MSG",
                        "JANGAN LUPA ABSEN SIANG!!"
                    ),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTimeInMillis,
                    pendingIntent
                )
            }
        }
    }
}