package com.waroengweb.absensi;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import androidx.core.app.NotificationCompat;

public class RunnerNotifier extends Notifier {
    private final String notificationChannelId = "runner_channel_id";
    private final String notificationChannelName = "Running Notification";
    private final int notificationId = 200;

    private final Context context;

    public RunnerNotifier(NotificationManager notificationManager, Context context) {
        super(notificationManager);
        this.context = context;
    }

    @Override
    public String getNotificationChannelId() {
        return notificationChannelId;
    }

    @Override
    public String getNotificationChannelName() {
        return notificationChannelName;
    }

    @Override
    public int getNotificationId() {
        return notificationId;
    }

    @Override
    public Notification buildNotification() {
        return new NotificationCompat.Builder(context, notificationChannelId)
                .setContentTitle(getNotificationTitle())
                .setContentText(getNotificationMessage())
                .setSmallIcon(android.R.drawable.btn_star)
                .build();
    }

    @Override
    public String getNotificationTitle() {
        return "Time to go for a run 🏃‍️";
    }

    @Override
    public String getNotificationMessage() {
        return "You are ready to go for a run?";
    }
}
