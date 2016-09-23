package com.bignerdranch.android.photogallery;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Context;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dlw on 2016/8/6.
 */
public class PhotoGalleryFragment extends VisibleFragment{
    private boolean isSetLayoutManager;
    private RecyclerView mPhotoRecycleView;
    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }
    private int page;
    boolean loading;
    PhotoAdapter adapter;

    private ThumbnailDownloader<PhotoHolder> thumbnailDownloader;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        Intent i=PollService.newIntent(getContext());
        getContext().startService(i);
        isSetLayoutManager=false;
        page=0;
//        fetchItemsTask=new FetchItemsTask(null);
//        fetchItemsTask.execute(new Integer(page+1));
        updateItems();
        loading=false;
        Handler responseHandler=new Handler();
//        thumbnailDownloader=new ThumbnailDownloader<>(responseHandler);//T??
//        thumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
//            @Override
//            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
//                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
//                target.bindDrawable(drawable);
//            }
//        });
//        thumbnailDownloader.start();
//        thumbnailDownloader.getLooper();


        Log.i(TAG, "Background thread started");

        GalleryItem a=new GalleryItem();
        a.setmCaption("a");
        GalleryItem b=new GalleryItem();
        b.setmCaption("b");
        test(a, b);
        Log.i(TAG, a.getmCaption());
        Log.i(TAG, b.getmCaption());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);
        final MenuItem searchItem=menu.findItem(R.id.menu_item_search);
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW|MenuItem.SHOW_AS_ACTION_ALWAYS);
        searchItem.setIcon(android.R.drawable.ic_menu_search);
        final SearchView searchView=(SearchView)searchItem.getActionView();
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String query = QueryPreferences.getStoredQuery(getContext());
                searchView.setQuery(query, false);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                InputMethodManager im = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(searchView.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                searchItem.collapseActionView();

                Log.d(TAG, "QueryTextSubmit: " + query);
                QueryPreferences.setStoredQuery(getContext(), query);
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "QueryTextChange: " + newText);
                return false;
            }
        });

        MenuItem toggleItem=menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getContext())){
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }
    private void updateItems(){
        String query=QueryPreferences.getStoredQuery(getContext());
        new FetchItemsTask(query).execute(1);
    }

    private void test(GalleryItem a,GalleryItem b){
        GalleryItem t;
        t=a;
        a=b;
        b=t;
        Log.i(TAG,a.getmCaption());
        Log.i(TAG,b.getmCaption());
    }

    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView photo;
        private GalleryItem item;
        public PhotoHolder(View itemView) {
            super(itemView);
            photo=(ImageView)itemView.findViewById(R.id.fragment_photo_gallery_image_view);
            photo.setOnClickListener(this);
        }
//        public void bindGalleryItem(GalleryItem item){
//            mTitleTextView.setText(item.getmCaption());
//        }
        public void bindDrawable(Drawable drawable){
            photo.setImageDrawable(drawable);
        }
        public void bindGalleryItem(GalleryItem item){
            this.item=item;
            Picasso.with(getContext())
                    .load(item.getmUrl())
                    .placeholder(R.drawable.ai)
                    .into(photo);
        }

        @Override
        public void onClick(View v) {
            Intent intent=new Intent(Intent.ACTION_VIEW,item.getPhotoPageUri());
            Intent intent1=PhotoPageActivity.newIntent(getActivity(),item.getPhotoPageUri());
            startActivity(intent1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getContext(), null);
                updateItems();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm=!PollService.isServiceAlarmOn(getContext());
                PollService.setServiceAlarm(getContext(),shouldStartAlarm);
                getActivity().supportInvalidateOptionsMenu();
                return true;
            default: return super.onOptionsItemSelected(item);
        }

    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{
        private List<GalleryItem> items;

        public PhotoAdapter(List<GalleryItem> items){
            this.items=items;

        }
        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view=LayoutInflater.from(getContext()).inflate(R.layout.gallery_item,parent,false);
            return new PhotoHolder(view);
        }
        public void preLoad(int num){
//            int l=(page-1)*100;
//            int r=l+num;
//            Log.i(TAG,"preload num  "+l);
//            Log.i(TAG,"preload num  "+r);
//            for (int i=l;i<r;i++){
//                String url=items.get(i).getmUrl();
//                thumbnailDownloader.preLoad(url);
//            }
        }
        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
//             Drawable placeholder=getResources().getDrawable(R.drawable.bill_up_close);
//             holder.bindDrawable(placeholder);
//            GalleryItem galleryItem=items.get(position);
//            thumbnailDownloader.queueThumbnail(holder,galleryItem.getmUrl());
//            GridLayoutManager manager=(GridLayoutManager)mPhotoRecycleView.getLayoutManager();
//            int firstp=manager.findFirstVisibleItemPosition();
//            int lastp=manager.findLastVisibleItemPosition();
//            int j1=position-firstp;
//            int j2=lastp-position;
//            int preLoadPos;
//            if (j1<j2){
//               preLoadPos=position-9;
//            } else {
//                preLoadPos=position+9;
//            }
//
//            if (0<=preLoadPos&&preLoadPos<page*100){
//                String url=items.get(preLoadPos).getmUrl();
//                thumbnailDownloader.preLoad(url);
//            }
             holder.bindGalleryItem(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_photo_gallery,container,false);
        mPhotoRecycleView=(RecyclerView)v.findViewById(R.id.fragment_photo_gallery_recycler_view);
//        mPhotoRecycleView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        mPhotoRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
//                GridLayoutManager manager = (GridLayoutManager) recyclerView.getLayoutManager();
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    int lastVisiblePosition = manager.findLastVisibleItemPosition();
//                    if (lastVisiblePosition > manager.getItemCount() -9) {
//                        Log.i(TAG, "Reach the END");
//                        if (!loading && page < 10) {
//                            loading = true;
//                            new FetchItemsTask(null).execute(page+1);
//                        }
//                    }
//                }
            }
        });

        setupAdapter(null);//maybe none
        ViewTreeObserver observer=mPhotoRecycleView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (isSetLayoutManager) return;
                isSetLayoutManager=true;
                float desity=getResources().getDisplayMetrics().density;
                int dp= (int)(mPhotoRecycleView.getWidth()/desity);
                mPhotoRecycleView.setLayoutManager(new GridLayoutManager(getContext(), dp/120));
                Log.i(TAG, "tree " + mPhotoRecycleView.getWidth());
                Log.i(TAG,"tree "+dp);
            }
        });
        return v;
    }
    private List<GalleryItem> mItems=new ArrayList<>();
    private void setupAdapter(List<GalleryItem> list){
        if (isAdded()){
            if (mPhotoRecycleView.getAdapter()==null){
                adapter=new PhotoAdapter(mItems);
                mPhotoRecycleView.setAdapter(adapter);
            } else {
                mItems.addAll(list);
                adapter.notifyDataSetChanged();

            }

        }
    }
    private void setupSearchAdapter(List<GalleryItem> items){
        if (isAdded()){
            if (mPhotoRecycleView.getAdapter()==null){
                mItems.addAll(items);
                adapter=new PhotoAdapter(items);
                mPhotoRecycleView.setAdapter(adapter);
            } else {
                mItems.clear();
                mItems.addAll(items);
                adapter.notifyDataSetChanged();
                mPhotoRecycleView.smoothScrollToPosition(0);

            }
        }
    }
    private static final String TAG="PhotoFrag";
    private class FetchItemsTask extends AsyncTask<Integer,Void,List<GalleryItem>>{
        private String mQuery;
        public FetchItemsTask(String query){
            mQuery=query;
        }
        ProgressDialog dialog;
        @Override
        protected List<GalleryItem> doInBackground(Integer... params) {
              publishProgress();
              if (mQuery==null){
                  Log.d(TAG, "doin mQuery==null");
                  Log.d(TAG,"EX",new Exception());
                  return new FlickrFetch().fetchRecentPhotos(params[0]);
              } else {
                  Log.d(TAG, "doin search");
                  return new FlickrFetch().searchPhotos(mQuery);
              }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
             if (mQuery==null){
                 setupAdapter(galleryItems);
                 loading = false;
                 page=page+1;
                 adapter.preLoad(30);
             } else {
                 setupSearchAdapter(galleryItems);
             }

            dialog.dismiss();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            dialog=new ProgressDialog(getContext());
            dialog.setIndeterminate(true);
            dialog.setTitle("从Flickr下载图片中。。。");
            dialog.show();
        }




    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        thumbnailDownloader.quit();
//        thumbnailDownloader.clearQueue();
        Log.i(TAG,"Background thread destroy");
    }

}
