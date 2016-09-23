package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.media.Image;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by dlw on 2016/8/11.
 */
public class PollService extends IntentService {
    private static final String TAG="PollService";
    private static final int POLL_INTERVAL=1000*10;
    public static final String ACTION_SHOW_NOTIFICATION="com.bignerdranch.android.photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE="com.bignerdranch.android.photogallery.PRIVATE";
    public static final String REQUEST_CODE="REQUEST_CODE";
    public static final String NOTIFICATION="NOTIFICATION";
    public static Intent newIntent(Context context){
        return new Intent(context,PollService.class);
    }
    public PollService(){
        super(TAG);
    }
    protected void onHandleIntent(Intent intent){
        Log.i(TAG, "Receive an intent: " + intent);
      if (!isNetworkAvailableAndConnected()){
          return;
      }
        String query=QueryPreferences.getStoredQuery(this);
        String lastResultIt=QueryPreferences.getPrefLastResultId(this);
        List<GalleryItem> items;
        if (query==null){
            items=new FlickrFetch().fetchRecentPhotos(1);

        } else {
            items=new FlickrFetch().searchPhotos(query);
        }
        if (items.size()==0) {
            return;
        }
        String resultId=items.get(0).getmId();
        if (resultId.equals(lastResultIt)){
            Log.i(TAG,"Got an old result: "+resultId);
            Resources resources=getResources();
            Intent i=PhotoGalleryActivity.newIntent(this);
            PendingIntent pi=PendingIntent.getActivity(this, 0, i, 0);
            Notification notification=new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_picture_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();
            NotificationManagerCompat notificationManagerCompat=NotificationManagerCompat.from(this);
            notificationManagerCompat.notify(0, notification);

        } else {
            Log.i(TAG,"Got a new result: "+resultId);

            Resources resources=getResources();
            Intent i=PhotoGalleryActivity.newIntent(this);
            PendingIntent pi=PendingIntent.getActivity(this, 0, i, 0);
            Notification notification=new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_picture_text))
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();
//            NotificationManagerCompat notificationManagerCompat=NotificationManagerCompat.from(this);
//            notificationManagerCompat.notify(0,notification);
            showBackgroundNotification(0,notification);
        }
        QueryPreferences.setPrefLastResultId(this, resultId);
    }
    private void showBackgroundNotification(int requestCode,Notification notification){
        Intent intent=new Intent(ACTION_SHOW_NOTIFICATION);
        intent.putExtra(REQUEST_CODE,requestCode);
        intent.putExtra(NOTIFICATION,notification);
        sendOrderedBroadcast(intent,PERM_PRIVATE,null,null, Activity.RESULT_OK,null,null);
    }
    private boolean isNetworkAvailableAndConnected(){
        ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable=cm.getActiveNetworkInfo()!=null;
        boolean isNetworkConnected=isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }
    public static void setServiceAlarm(Context context,boolean isOn){
        Intent i=PollService.newIntent(context);
        PendingIntent pi=PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (isOn){
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),POLL_INTERVAL,pi);

        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
        QueryPreferences.setAlarOn(context,isOn);
    }
    public static boolean isServiceAlarmOn(Context context){
        Intent intent=PollService.newIntent(context);
        PendingIntent pi=PendingIntent.getService(context,0,intent,PendingIntent.FLAG_NO_CREATE);
        return pi!=null;
    }
}
