package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by Administrator on 2016/8/18 0018.
 */

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG="NotificationReveicer";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"received result: "+getResultCode());
        if (getResultCode()!= Activity.RESULT_OK){
            return;
        }
        int resultCode=intent.getIntExtra(PollService.REQUEST_CODE,0);
        Notification notification=(Notification)intent.getParcelableExtra(PollService.NOTIFICATION);
        NotificationManagerCompat notificationManagerCompat=NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(resultCode,notification);
    }
}
