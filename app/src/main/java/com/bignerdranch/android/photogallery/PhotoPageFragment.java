package com.bignerdranch.android.photogallery;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.net.URI;
import java.util.Date;
import java.util.List;

import javax.crypto.interfaces.PBEKey;

/**
 * Created by Administrator on 2016/8/18 0018.
 */

public class PhotoPageFragment extends VisibleFragment {
    private static final String ARG_URI="photo_page_url";
    private static final String TAG="PPFa";
    private ProgressBar mProgressBar;
    public static PhotoPageFragment newInstance(Uri uri){
        Bundle args=new Bundle();
        args.putParcelable(ARG_URI,uri);
        PhotoPageFragment fragment=new PhotoPageFragment();
        fragment.setArguments(args);
        return fragment;
    }
    private Uri mUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUri=(Uri) getArguments().getParcelable(ARG_URI);

    }

    private WebView mWebview;
    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_photo_page,container,false);
        mWebview=(WebView)v.findViewById(R.id.fragment_photo_page_web_view);
        mWebview.getSettings().setJavaScriptEnabled(true);
    mWebview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.indexOf("market")>=0){
                    Uri uri=Uri.parse(url);

                    Intent intent=new Intent(Intent.ACTION_VIEW,uri);

                    if (!startApp("com.yahoo.mobile.client.android.flickr")){
                        startActivity(intent);
                    }


                    return true;
                }
                return false;
            }
        });

        mProgressBar=(ProgressBar)v.findViewById(R.id.fragment_photo_progress_bar);
        mProgressBar.setMax(100);

        mWebview.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                 if (newProgress==100){
                     mProgressBar.setVisibility(View.GONE);
                 } else {
                     mProgressBar.setVisibility(View.VISIBLE);
                     mProgressBar.setProgress(newProgress);
                 }

            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                AppCompatActivity activity=(AppCompatActivity)getActivity();
                activity.getSupportActionBar().setSubtitle(title);
            }
        });
        mWebview.loadUrl(mUri.toString());
//        mWebview.addJavascriptInterface(new Object(){
//            @JavascriptInterface
//            @Override
//            public String toString() {
//                return "fufk";
//            }
//        },"lir");
//        mWebview.loadData("", "text/html", null);
//        mWebview.loadUrl("javascript:alert(injectedObject.toString())");
        return v;

    }
    public boolean onBackPress() {
        if (mWebview.canGoBack()) {
            mWebview.goBack();
            return true;
        } else {
            return false;
        }
    }
    private boolean startApp(String packageName){
        PackageInfo packageInfo=null;
        try{
            packageInfo=getActivity().getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException n){
            Log.i(TAG,"No packageInfo");
        }
        Intent i=new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        i.setPackage(packageInfo.packageName);
        Date d=new Date(packageInfo.firstInstallTime);
        ActivityInfo[]  activityInfo=packageInfo.activities;
        for (ActivityInfo in:activityInfo){
            Log.i(TAG,in.toString());
        }
        Log.i(TAG,d.toString());
        Log.i(TAG,i.toString());
        Log.i(TAG,"1");
        ResolveInfo info=getActivity().getPackageManager().resolveActivity(i,PackageManager.MATCH_DEFAULT_ONLY);
        if (info!=null){
            Intent intent=new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            String packageN=info.activityInfo.packageName;
            String classN=info.activityInfo.name;
            ComponentName cn=new ComponentName(packageN,classN);
            Log.i(TAG,"2");
            intent.setComponent(cn);
            startActivity(intent);
        } else {
            Log.i(TAG,"3");
            return false;
        }
        return true;
    }
}
