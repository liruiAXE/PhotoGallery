package com.bignerdranch.android.photogallery;

import android.app.VoiceInteractor;
import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by dlw on 2016/8/10.
 */
public class QueryPreferences {
    private static final String PREF_SEARCH_QUERY="searchQuery";
    private static final String PREF_LAST_RESULT_ID="lastResultId";
    private static final String PREF_IS_ALARM_ON="isAlarmOn";
    public static boolean isAlarmOn(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_IS_ALARM_ON,false);
    }
    public static void setAlarOn(Context context,boolean isOn){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_IS_ALARM_ON,isOn)
                .apply();
    }
    public static String getStoredQuery(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY,null);
    }
    public static void setStoredQuery(Context context,String query){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY,query)
                .apply();
    }
    public static String getPrefLastResultId(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_LAST_RESULT_ID,null);
    }
    public static void setPrefLastResultId(Context context,String lastResultId){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_LAST_RESULT_ID,lastResultId)
                .apply();
    }
}
