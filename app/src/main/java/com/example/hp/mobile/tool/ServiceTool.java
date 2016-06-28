package com.example.hp.mobile.tool;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.hp.mobile.MainActivity;
import com.example.hp.mobile.R;

/**
 * Created by hp on 2016/6/26.
 */
public class ServiceTool extends Service {
    private NotificationManager mNF;

    public static String AUTO_START = "com.example.hp.mobile.AUTO_START";
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        autoStartNotification();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mNF.cancel(R.string.app_name);
    }
    private void autoStartNotification()
    {
        //Notification mNotification=new Notification(R.drawable.app_logo,"365手机秘书",System.currentTimeMillis());
        Intent intent=new Intent(this, MainActivity.class);
        intent.setAction(AUTO_START);
        intent.putExtra("auto_start","boot_completed");
        PendingIntent mPI=PendingIntent.getActivity(this,0,intent,0);
        Notification mNotification= new Notification.Builder(this)
                .setContentTitle("365手机秘书")
                .setContentText("成功开机")
                .setSmallIcon(R.drawable.app_logo)
                .setContentIntent(mPI).setWhen(System.currentTimeMillis())
                .build();
        if(null==mNF)
        {
            mNF=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        }
        mNF.notify(R.string.app_name,mNotification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
