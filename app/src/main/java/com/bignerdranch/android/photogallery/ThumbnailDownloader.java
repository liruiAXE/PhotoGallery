package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by dlw on 2016/8/7.
 */
public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG="ThumbnaiDownloader";
    private static final int MESSAGE_DOWNLOAD=0;
    private Handler mRequestHandler;
    private ConcurrentMap<T,String> mRequestMap=new ConcurrentHashMap<>();
    private LruCache<String,Bitmap> mPhotoCache;

    private Boolean mHasQuit=false;

    private Handler mResponsehander;

    public interface ThumbnailDownloadListener<T>{
        void onThumbnailDownloaded(T target,Bitmap thumbnail);
    }
    private ThumbnailDownloadListener listener;
    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener){
        this.listener=listener;
    }
    public ThumbnailDownloader(Handler mResponsehander){
        super(TAG);
        this.mResponsehander=mResponsehander;

        int maxMemory=(int)Runtime.getRuntime().maxMemory();
        maxMemory=maxMemory/4;
        Log.i(TAG,"maxMemory  "+maxMemory);
        mPhotoCache=new LruCache<String,Bitmap>(maxMemory){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                Log.i(TAG,"BitmapByteCount "+value.getByteCount());
                return value.getByteCount();
            }
        };
    }

    @Override
    public boolean quit() {
        mHasQuit=true;
        return super.quit();
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what==MESSAGE_DOWNLOAD){
                    T target=(T)msg.obj;
                    Log.i(TAG,"Got a request for URL: "+mRequestMap.get(target));
                    handleRequest(target);
                } else if (msg.what==MESSAGE_PRELOAD){
                    String url=(String)msg.obj;
                    handlePreLoad(url);
                }
            }
        };
    }
    private void handlePreLoad(String url){
        try{
            if (url==null){
                return;
            }
            byte[] bitmapBytes=new FlickrFetch().getUrlBytes(url);
            final Bitmap bitmap= BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);
            Log.i(TAG,"Bitmap created");
            mPhotoCache.put(url, bitmap);
        } catch (IOException e){
            Log.e(TAG,"error preloading bitmap");
        }
    }
    private void handleRequest(final T target){
        try{
            final String url=mRequestMap.get(target);
            if (url==null){
                return;
            }
            byte[] bitmapBytes=new FlickrFetch().getUrlBytes(url);
            final Bitmap bitmap= BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);
            Log.i(TAG,"Bitmap created");
            mPhotoCache.put(url, bitmap);

            mResponsehander.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target) != url || mHasQuit) {
                        return;
                    }
                    String url = mRequestMap.get(target);

                    mRequestMap.remove(target);
                    listener.onThumbnailDownloaded(target, bitmap);
                }
            });
        } catch(IOException e){
            Log.e(TAG,"Error douloading image",e);
        }
    }
    public void queueThumbnail(T target,String url){
        Log.i(TAG, "Got a URL: " + url);
        if (url==null){
            mRequestMap.remove(target);
            return;
        }
        //is this UI thread?
        //这个函数实在UI线程中运行的 Thread.currentThread();应该返回的是UI线程
        Bitmap bitmap;
        if ((bitmap=mPhotoCache.get(url))!=null){
            listener.onThumbnailDownloaded(target,bitmap);
            return;
        }
        if (url==null){

        } else {

            mRequestMap.put(target,url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD,target).sendToTarget();
        }
    }
    private static final int MESSAGE_PRELOAD=1;
    public void preLoad(String url){
        if (url==null) return;
        if (mPhotoCache.get(url)!=null){
            return;
        }
        mRequestHandler.obtainMessage(MESSAGE_PRELOAD,url).sendToTarget();

    }

    public void clearQueue(){
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }
}
