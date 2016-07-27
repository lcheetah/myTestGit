package com.lcheetah.activity;

import java.util.Timer;
import java.util.TimerTask;

import com.lcheetah.doppler.R;
import com.lcheetah.main.MainActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;



/**
 * @author lc
 *
 */
public class WelcomeActivity extends Activity{
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcome);
      
        	timeTochange();

		
 
    	
	}
	/**
	 * 设置2秒后跳转
	 */
	 private void timeTochange() {
		 	new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						startActivity(new Intent(WelcomeActivity.this,MainActivity.class));
						finish();
					}
				}, 2000);
	}


	
	      
	    
}
