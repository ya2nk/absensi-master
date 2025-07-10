package com.waroengweb.absensi;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public abstract class Notifier {
    private final NotificationManager notificationManager;

    public Notifier(NotificationManager notificationManager) {
        this.notificationManager = notificationManager;
    }

    public abstract String getNotificationChannelId();
    public abstract String getNotificationChannelName();
    public abstract int getNotificationId();

    public void showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = createNotificationChannel();
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification = buildNotification();
        notificationManager.notify(
                getNotificationId(),
                notification
        );
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.O)
    public NotificationChannel createNotificationChannel(int importance) {
        return new NotificationChannel(
                getNotificationChannelId(),
                getNotificationChannelName(),
                importance
        );
    }

    public NotificationChannel createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return createNotificationChannel(NotificationManager.IMPORTANCE_DEFAULT);
        }
        return null;
    }

    public abstract Notification buildNotification();

    protected abstract String getNotificationTitle();

    protected abstract String getNotificationMessage();
}