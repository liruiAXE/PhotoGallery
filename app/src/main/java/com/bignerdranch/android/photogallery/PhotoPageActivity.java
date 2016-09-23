package com.bignerdranch.android.photogallery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.Choreographer;

/**
 * Created by Administrator on 2016/8/18 0018.
 */

public class PhotoPageActivity extends SingleFragmentActivity {
    public static Intent newIntent(Context context,Uri photoPageUri){
        Intent i=new Intent(context,PhotoPageActivity.class);
        i.setData(photoPageUri);
        return i;
    }
    PhotoPageFragment fragment;
    @Override
    protected Fragment createFragment() {
        fragment=PhotoPageFragment.newInstance(getIntent().getData());
        return fragment;
    }

    @Override
    public void onBackPressed() {
        if (!fragment.onBackPress()) {
            super.onBackPressed();
        }
        
    }
}
