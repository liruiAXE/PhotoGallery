package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;
import android.widget.Gallery;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dlw on 2016/8/6.
 */
public class FlickrFetch {

    private static final String TAG="FlickFetch";
    private static final String API_KEY="cc7414eb94c05d2b7fc6aaf66dcac31a";

    private static final String FETCH_RECENTS_METHOD="flickr.photos.getRecent";
    private static final String SEARCH_METHOD="flickr.photos.search";
    private static final Uri ENDPOINT=Uri.parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key",API_KEY)
            .appendQueryParameter("nojsoncallback","1")
            .appendQueryParameter("extras","url_s")
            .appendQueryParameter("format","json")
            .build();
    public byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url=new URL(urlSpec);
        HttpURLConnection connection=(HttpURLConnection)url.openConnection();
        try {
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            InputStream in=connection.getInputStream();
            if (connection.getResponseCode()!=HttpURLConnection.HTTP_OK){
                IOException e= new IOException(connection.getResponseMessage()+" :" +
                        "with " +
                        urlSpec);
                Log.i("PhotoFrag","  fil  ",e);
                throw e;
            }
            int bytesRead=0;
            byte[] buffer=new byte[1024];
            while ((bytesRead=in.read(buffer))>0){
                out.write(buffer,0,bytesRead);
            }
            out.close();
            return out.toByteArray();

        }
        finally {
           connection.disconnect();
        }
    }
    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }
    private String buildUrl(String method,String query){
        Uri.Builder uriBuilder=ENDPOINT.buildUpon()
                .appendQueryParameter("method",method);
        if (method.equals(SEARCH_METHOD)){
            uriBuilder.appendQueryParameter("text",query);
        }
        if (method.equals(FETCH_RECENTS_METHOD)){
            uriBuilder.appendQueryParameter("page",query);
        }
        return uriBuilder.build().toString();
    }
    public List<GalleryItem> fetchRecentPhotos(int page){
        String url=buildUrl(FETCH_RECENTS_METHOD,String.valueOf(page));
        return downloadGalleryItems(url);
    }
    public List<GalleryItem> searchPhotos(String query){
        String url=buildUrl(SEARCH_METHOD,query);
        return downloadGalleryItems(url);
    }
    private List<GalleryItem> downloadGalleryItems(String url){
        List<GalleryItem> items=new ArrayList<>();
        try{
//            String url= Uri.parse("https://api.flickr.com/services/rest/")
//                    .buildUpon()
//                    .appendQueryParameter("method","flickr.photos.getRecent")
//                    .appendQueryParameter("api_key",API_KEY)
//                    .appendQueryParameter("format","json")
//                    .appendQueryParameter("nojsoncallback","1")
//                    .appendQueryParameter("extras","url_s")
//                    .appendQueryParameter("page",String.valueOf(page))
//                    .build().toString();
            String jsonString=getUrlString(url);
            Log.i(TAG,"Received JSON : "+jsonString);
            JSONObject jsonBody=new JSONObject(jsonString);
            parseItem2(items, jsonBody);
        } catch(IOException e){
            Log.e(TAG,"Failed to fetch item ",e);
        } catch (JSONException je){
            Log.e(TAG,"Failed to parse json ",je);
        }
        return items;
    }
    private void parseItem(List<GalleryItem> items,JSONObject jsonBody) throws IOException,JSONException{
        JSONObject photosJsonObject=jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray=photosJsonObject.getJSONArray("photo");
        for (int i=0;i<photoJsonArray.length();i++){
            JSONObject photoJsonObject=photoJsonArray.getJSONObject(i);

            GalleryItem item=new GalleryItem();
            item.setmId(photoJsonObject.getString("id"));
            item.setmCaption(photoJsonObject.getString("title"));
            if (!photoJsonObject.has("url_s")){
                continue;
            }
            item.setmUrl(photoJsonObject.getString("url_s"));
            items.add(item);
        }
    }
    private void parseItem2(List<GalleryItem> items,JSONObject jsonBody) throws IOException,JSONException{
        JSONObject photosJsonObject=jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray=photosJsonObject.getJSONArray("photo");
        Gson gson=new Gson();
        Type type=new TypeToken<ArrayList<GalleryItem>>(){}.getType();

        ArrayList<GalleryItem>item=gson.fromJson(photoJsonArray.toString(),type);
        for (int i=0;i<item.size();i++){
            items.add(item.get(i));
        }
        Log.i(TAG,"num "+items.size());

    }
}
