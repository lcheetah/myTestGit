package com.lcheetah.activity;

import java.io.File;

import com.lcheetah.doppler.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class TestViewActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_view);
		initView();
		
	}

	private void initView() {
		  PhotoView photoView = (PhotoView) findViewById(R.id.iv_photo);
		 

	        final PhotoViewAttacher attacher = new PhotoViewAttacher(photoView);
	        File file = new File(Environment.getExternalStorageDirectory()+"/bestmanpic/","1469026188268.png");

	        Picasso.with(this).load(file).into(photoView, new Callback() {
                @Override
                public void onSuccess() {
                    attacher.update();
                }

                @Override
                public void onError() {
                }
            });		
	        
	}
}
