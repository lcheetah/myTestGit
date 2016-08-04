package com.lcheetah.activity;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.lcheetah.doppler.R;
import com.lcheetah.view.DepthPageTransformer;
import com.lcheetah.view.FixedSpeedScroller;
import com.lcheetah.view.Mytoast;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateInterpolator;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;


public class MainActivity extends Activity {	
	private ImageButton Im_shebei;
	private ImageButton Im_record;
	private ImageButton Im_more;
	private ViewPager Im_viewPager;
	private BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();
	private static final int REQUEST_ENABLE_BT = 1;
	private BluetoothDevice mdevice;
//	private ImageView iView;
	int[] imgs = new int[] { R.drawable.img_1, R.drawable.img_2, R.drawable.img_3,
			R.drawable.img_4};
	private List<ImageView> mImageViews = new ArrayList<ImageView>();  
	FixedSpeedScroller mScroller = null;

	/** 该值指示搜索蓝牙状态<p>=true，正在搜索<p>=false，搜索完成或搜索停止  */
	private boolean searchingFlag = true;
	// private static final long SCAN_PERIOD = 10000;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		regesiterBroadcastReceiver();
		initView();
		//CreateDB();
		setViewPager();
		initBluetooth();
	}
	private void initBluetooth() {
		if (mBtAdapter!=null) {
			if (mBtAdapter.isEnabled()) {
				_searchStart.start();
				Log.i("MainActivity","startSearch");
			}
		}else{
			Toast.makeText(MainActivity.this,"该设备不支持蓝牙",Toast.LENGTH_SHORT).show();
		}

	}
	@Override
	protected void onResume() {
		super.onResume();

		if (!mBtAdapter.isEnabled()) {

			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 3
				&& resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

/*	private void CreateDB() {

		SQLiteDatabase db = openOrCreateDatabase("data.db",
				ResultActivity.MODE_PRIVATE, null);
		db.execSQL("create table if not exists datatb(_id integer primary key autoincrement,date text not null,express_img BLOB,getdata text not null,totaltime text not null,timer_total text not null)");

		db.close();
	}
*/
	private void initView() {
		
		
		Im_viewPager =(ViewPager) findViewById(R.id.guidePages);
		
		Im_shebei = (ImageButton) findViewById(R.id.IM_shebei);
		Im_record = (ImageButton) findViewById(R.id.IM_Record);
		Im_more = (ImageButton) findViewById(R.id.IM_more);
		
		
		
		Im_shebei.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				Log.i("info", "onClick");
				if (mdevice!=null) {
					
					final Intent intent = new Intent(MainActivity.this, DeviceControlActivity.class);
					intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, mdevice.getName());
					intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, mdevice.getAddress());
					intent.putExtra(BluetoothDevice.EXTRA_DEVICE, mdevice);
					Log.i("info", "device=="+mdevice.getName());
					if (searchingFlag) {
						searchingFlag = false;
					}
					startActivity(intent);
					overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
				}else{
					if (mBtAdapter.isDiscovering()) {
						mBtAdapter.cancelDiscovery();
					}
					mBtAdapter.startDiscovery();
					showToast(MainActivity.this,getString(R.string.open_Device),Toast.LENGTH_SHORT);
					//Toast.makeText(MainActivity.this,R.string.open_Device,Toast.LENGTH_SHORT).show();
				}
				/*Intent intent = new Intent();
				intent.setClass(MainActivity.this,
						com.example.bluetooth.le.DeviceScanActivity.class);
				startActivity(intent);*/
			}
		});
		Im_record.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this, RecordActivity.class);
				startActivity(intent);
			}
		});
		Im_more.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				//Intent intent = new Intent();
				//intent.setClass(MainActivity.this, MoreActivity.class);
				//startActivity(intent);
				showToast(MainActivity.this,"More", Toast.LENGTH_SHORT);
			}
		});
	}
	
	
	//设置主页图片轮转
	private void setViewPager() {
		
		controlViewPagerSpeed();
		Im_viewPager.setPageTransformer(true, new DepthPageTransformer());
		isrunning =true;
		handler.sendEmptyMessageDelayed(0, 5000);
		for (int imgId : imgs)  
        {  
            ImageView imageView = new ImageView(getApplicationContext());  
            imageView.setScaleType(ScaleType.CENTER_CROP);  
            imageView.setImageResource(imgId);  
            mImageViews.add(imageView);  
        } 
	//	Im_viewPager.startAutoScroll();
		Im_viewPager.setAdapter(new PagerAdapter() {
			
			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				container.addView(mImageViews.get(position%mImageViews.size()));
				return mImageViews.get(position%mImageViews.size());
			}
			@Override
			public boolean isViewFromObject(View view, Object object) {
				return view == (View)object;
			}
			
			@Override
			public int getCount() {
				return mImageViews.size()*100;
			}
			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				container.removeView((View)object);
				object =null;
			}
		});
	}

	private static Toast mToast = null;
	public static void showToast(Context context, String text, int lengthShort) {
		if (mToast==null) {
			mToast =Toast.makeText(context,text,lengthShort);
		}else{
			mToast.setText(text);
			mToast.setDuration(lengthShort);
		}

		mToast.show();
	}
	@Override
	protected void onPause() {
		super.onPause();
	}
	/*
	 */
	private static boolean isExit = false;
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			isExit = false;
		}

	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return false;
		}
		return super.onKeyDown(keyCode, event);

	}
	//双击退出；

	private void exit() {
		if (!isExit) {
			isExit = true;
			Toast.makeText(getApplicationContext(), R.string.toast3,
					Toast.LENGTH_SHORT).show();
			mHandler.sendEmptyMessageDelayed(0, 5000);
		} else {
			finish();
			System.exit(0);
		}
	}

	/**
	 * 搜索蓝牙监听器，主要是监听 BluetoothDevice.ACTION_FOUND
	 */
	private BroadcastReceiver _foundReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			//String mDevice;
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

			String mdeviceName = device.getName();
			//Log.i("info", mdeviceName);

			String mdeviceAddress = device.getAddress();
			//mdevice = mdeviceAddress + "\n" + mdeviceName;
			if(mdeviceName.contains("iFM10B")){
				mdevice = device;
				mBtAdapter.cancelDiscovery();

			}
		}

	};

	/**
	 * 完成搜索监听器，监听 BluetoothAdapter.ACTION_DISCOVERY_FINISHED
	 */
	private BroadcastReceiver _finishedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(mdevice==null){
				if(!mBtAdapter.isDiscovering()){
					Mytoast.showToast(MainActivity.this, "未搜索到胎心设备，请开启设备重新搜索", Toast.LENGTH_SHORT);
					//_searchStart.start();
				}
			}else{
				searchingFlag = false;
				unregisterBroadcastReceiver();
			}
		}

	};

	/**
	 * 启动搜索线程
	 */
	Thread _searchStart = new Thread() {

		@Override
		public void run() {
			mBtAdapter.startDiscovery();
		}

	};


	/**
	 * 注册广播接收器
	 */
	public void regesiterBroadcastReceiver() {
		IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND); 
		registerReceiver(_foundReceiver, foundFilter); 
		IntentFilter finishedFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); 
		registerReceiver(_finishedReceiver, finishedFilter); 
	}
	/**
	 * 注销广播接收器
	 */
	public void unregisterBroadcastReceiver() {
		unregisterReceiver(_foundReceiver);
		unregisterReceiver(_finishedReceiver);
	}
	
	//图片轮播速度控制
    private void controlViewPagerSpeed() {  
		try {  
		    Field mField;  
		    mField = ViewPager.class.getDeclaredField("mScroller");  
		    mField.setAccessible(true);  
		  
		    mScroller = new FixedSpeedScroller(  
		        Im_viewPager.getContext(),  
		        new AccelerateInterpolator());  
		    mScroller.setmDuration(500); // 2000ms  
		    mField.set(Im_viewPager, mScroller);  
		} catch (Exception e) {  
		    e.printStackTrace();  
		}  
		   }  
    
	private boolean isrunning = false;
    /**
     * 自动滑动的Handler
     * 利用 Handler.sendEmptyMessageDelayed方法，实现定时滚动
     *
     */
    private Handler handler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		//让ViewPager滑到下一页
    		Im_viewPager.setCurrentItem(Im_viewPager.getCurrentItem()+1);
    		//延时，循环调用handler
    		if(isrunning){
    			handler.sendEmptyMessageDelayed(0, 5000);
    		}
    	};
    };
    
    /**
     * Activity生命周期结束时终止定时，否则可能一直进行下去
     */
    protected void onDestroy() {
		super.onDestroy();
    	isrunning = false;
    };


}
