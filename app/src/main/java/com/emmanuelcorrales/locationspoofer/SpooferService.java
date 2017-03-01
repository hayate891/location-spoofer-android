package com.emmanuelcorrales.locationspoofer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.emmanuelcorrales.locationspoofer.activities.MainActivity;
import com.google.android.gms.maps.model.LatLng;


public class SpooferService extends Service {

    public class SpooferBinder extends Binder {
        public SpooferService getService() {
            return SpooferService.this;
        }
    }

    public static final String ACTION_STOP = "stop";

    private static final String TAG = SpooferService.class.getSimpleName();
    private static final int ID = SpooferService.class.hashCode();

    private Binder mBinder = new SpooferBinder();
    private LocationSpoofer mSpoofer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        mSpoofer = new LocationSpoofer(this);
        mSpoofer.initializeGpsSpoofing();
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        if (intent != null) {
            String action = intent.getAction();
            if (action != null && action.equals(ACTION_STOP)) {
                Log.d(TAG, "Stopping SpooferService.");
                stopSelf();
                return START_NOT_STICKY;
            }
        }
        return START_STICKY;
    }

    public LatLng getSpoofedLocation() {
        return mSpoofer.getLatLng();
    }

    public void spoof(LatLng latLng) {
        mSpoofer.mockLocation(latLng);
        startForeground(ID, createNotification());
    }

    private Notification createNotification() {
        NotificationCompat.Style style = new NotificationCompat.InboxStyle()
                .addLine("Latitude: " + mSpoofer.getLatLng().latitude)
                .addLine("Longitude: " + mSpoofer.getLatLng().latitude);

        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setStyle(style)
                .setContentIntent(createAppPendingIntent())
                .addAction(android.R.drawable.ic_lock_power_off,
                        getString(R.string.notification_stop_spoofing),
                        createStopPendingIntent())
                .build();
    }

    private PendingIntent createAppPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private PendingIntent createStopPendingIntent() {
        Intent intent = new Intent(this, SpooferService.class);
        intent.setAction(ACTION_STOP);
        return PendingIntent.getService(this, 0, intent, 0);
    }
}