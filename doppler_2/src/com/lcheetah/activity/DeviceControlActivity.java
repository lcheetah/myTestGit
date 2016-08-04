package com.lcheetah.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.lcheetah.doppler.R;
import com.lcheetah.service.BluetoothService;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author xy
 * 
 */
@SuppressLint("NewApi")
public class DeviceControlActivity extends Activity implements Runnable {
	private final static String TAG = DeviceControlActivity.class
			.getSimpleName();
	private BluetoothDevice mBtDevice;

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	private ImageView xinlv_image;
	private Button searchButton;
	private TextView mConnectionState;
	private TextView mDataField;
	private TextView record_time;
	private String mDeviceName = "";
	private int xinlv = 0;
	private int recLen = 0;
	private float b ;
	private boolean mConnected = false;
	private boolean flag = false;
	private boolean isConnect = true;
	private List<Float> datalist = new ArrayList<Float>();
	private List<Float> datalist2 = new ArrayList<Float>();
	private BluetoothService mBluetoothService;
	private SurfaceView mSurfaceView = null;
	private SurfaceHolder mSurfaceHolder = null;
	final Timer timer = new Timer(true);
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch(msg.what) {
			case TIMER:
				recLen++;
				record_time.setText(DatatoTime(recLen));
				break;
			case MSG_ERR:
				String str = msg.getData().getString("err");
				//	dispString(str);
				Log.i("DeviceControlActivity","err==="+ str);
				break;

			case MSG_SERVICE_INFOR:
				if(isPainting){

					String infor = msg.getData().getString("infor");
					infor=infor.trim();
					//showServiceInfor(infor);
					// int data= Integer.decode(infor);
					int data=Integer.parseInt(infor,10);
					displayData(data);
					//Log.i("info","info===="+infor);


				}
				break;

			case MSG_SERVICE_STATUS:
				String status = msg.getData().getString("status");
				//	showServiceStatus(status);
				Log.i("DeviceControlActivity","status==="+ status);
				break;

			}

		}

	};


	// 服务绑定标志
	public boolean serveiceBindFlag = false;
	/**
	 * 绑定服务
	 */
	private void bindFhrService() {
		Intent bindIntent = new Intent(this, BluetoothService.class);
		this.bindService(bindIntent , mSCon, Context.BIND_AUTO_CREATE);
		serveiceBindFlag = true;

	}
	/**
	 * 解除服务绑定
	 */
	private void unbindFhrDervice() {
		if(mBluetoothService.getRecordStatus()) {
			mBluetoothService.recordFinished();
		}
		this.unbindService(mSCon);
		mBluetoothService = null;
		serveiceBindFlag = false;
	}

	/**
	 * 服务绑定连接类，在这里要设置服务的蓝牙设备，设置回调接口，
	 * 并启动服务的相关线程
	 */
	private ServiceConnection mSCon = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBluetoothService = ((BluetoothService.BluetoothBinder) service).getService();
			mBluetoothService.setBluetoothDevice(mBtDevice);
			mBluetoothService.setCallback(mCallback);
			mBluetoothService.start();

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBluetoothService = null;
		}

	};

	protected boolean isPainting =false;

	private final OnClickListener buttonClickListner = new OnClickListener() {

		@Override
		public void onClick(View arg0) {

			if (searchButton.getText().equals(getString(R.string.start_testing))) {
				if (mBluetoothService.mSocket==null) {
					Toast.makeText(DeviceControlActivity.this, "请开启胎心仪",Toast.LENGTH_SHORT).show();
				}
				
				try {
					isPainting  =true;
					//onChildClick(0, 0);
				} catch (Exception e) {
				}
				if (datalist.size() != 0) {
					datalist.clear();
				}
				timer.schedule(task, 0, 1000);
				datalist.clear();
				searchButton.setText(R.string.Stop_testing);
			} else {
				//					if(datalist.size()<5){
				//					Toast.makeText(DeviceControlActivity.this, R.string.toast5, Toast.LENGTH_SHORT).show();
				//					}else{
				Log.i("info", "datalistsize====="+datalist2.size());
				isPainting=false;
				timer.cancel();
				finish();
				Intent intent = new Intent(DeviceControlActivity.this,
						ResultActivity.class);
				intent.putExtra("data", (Serializable) datalist2);
				//DeviceScanActivity.instance.finish();
				Log.w("==========", DatatoTime(recLen));
				intent.putExtra("totaltime", DatatoTime(recLen));
				intent.putExtra("timer_total", recLen+"");
				startActivity(intent);
				overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
			}

		}

	};




	private void clearUI() {
		mDataField.setText(R.string.no_data);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.gatt_services_characteristics);
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		
		final Intent intent = getIntent();
		mBtDevice =intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		bindFhrService();
		searchButton = (Button) findViewById(R.id.search_button);
		searchButton.setOnClickListener(buttonClickListner);
	//	mConnectionState = (TextView) findViewById(R.id.connection_state);
		mDataField = (TextView) findViewById(R.id.data_value);
		record_time = (TextView) findViewById(R.id.record_time);

		xinlv_image = (ImageView) findViewById(R.id.xinlv_image);
		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceview);
		mSurfaceHolder = mSurfaceView.getHolder();
		mSurfaceHolder.addCallback(surfaceCallback);
		//getActionBar().setTitle(mDeviceName);
		clearUI();

	}

	@Override
	protected void onResume() {
		super.onResume();
		/*	if (mBluetoothService != null) {
			mBluetoothService.start();
		}*/
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unbindFhrDervice();
		mBluetoothService = null;
	}

	/*@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.gatt_services, menu);

		if (isConnect == false) {
			menu.findItem(R.id.menu_connect).setVisible(false);

			menu.findItem(R.id.menu_refresh_gatt_services).setActionView(
					R.layout.actionbar_indeterminate_progress);

		} else {
			menu.findItem(R.id.menu_refresh_gatt_services).setActionView(null);
		}

		if (mConnected) {
			menu.findItem(R.id.menu_connect).setVisible(false);
			menu.findItem(R.id.menu_disconnect).setVisible(true);
			menu.findItem(R.id.menu_refresh_gatt_services).setActionView(null);
		} else {
			menu.findItem(R.id.menu_connect).setVisible(true);
			menu.findItem(R.id.menu_disconnect).setVisible(false);
		}
		return true;
	}*/

/*	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_connect:
			mBluetoothService.start();
			isConnect = false;

			invalidateOptionsMenu();
			return true;
		case R.id.menu_disconnect:
			mBluetoothService.cancel();
			isConnect = true;
			invalidateOptionsMenu();
			return true;
		case android.R.id.home:
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}*/


	private void displayData(int data) {
		//if (data != 0) {
		if (data< 50 || data > 210) {
			mDataField.setTextColor(Color.BLACK);
			mDataField.setText("--");
		} else if(data>160||data<110){
			mDataField.setTextColor(Color.RED);
			mDataField.setText("" + data);
		}
		else{
			mDataField.setTextColor(Color.BLACK);
			mDataField.setText("" + data);
		}
		//				if(StringtoInt(data) < 50 || StringtoInt(data) > 240){
		//					b=0;
		//				}else{
		//					int a = StringtoInt(data);
		//					b = a;
		//				}

		//					if (datalist.size() > 1) {
		//						// �ж������������ݵĲ�ֵ�Ƿ����20���������20�򽫵�һ�����ݵ�ֵ��ֵ���ڶ�������
		//						if (b - datalist.get(datalist.size() - 1) > 20
		//								|| datalist.get(datalist.size() - 1) - b > 20) {
		//							b = datalist.get(datalist.size() - 1);
		//
		//						}
		//					}
		//int a = data;
		b = (float)data;
		// 如果数据长度大于720 则移除前面的数
		datalist.add(b);
		datalist2.add(b);
		if (datalist.size() > 720) {
			datalist.remove(0);
		}


		if (xinlv % 2 == 0) {
			xinlv_image.setVisibility(View.VISIBLE);

		} else {
			xinlv_image.setVisibility(View.GONE);
		}
		//		}
	}

	TimerTask task = new TimerTask() {
		public void run() {
			Message message = new Message();
			message.what = TIMER;
			xinlv = xinlv +1;
			handler.sendMessage(message);
		}
	};

	private String DatatoTime(int data) {
		String time = null;
		String time_second = "00";
		String time_miu = "00";
		String time_hour = "00";
		int miu = data / 60;
		int sec = data % 60;
		int hour = miu / 60;
		if (sec % 60 < 10) {
			time_second = "0" + sec;
		} else {
			time_second = "" + sec;
		}
		if (miu < 10) {
			time_miu = "0" + miu;
		} else if (miu < 60) {
			time_miu = "" + miu;
		} else if (miu >= 60 && hour < 10 && miu % 60 < 10) {
			time_hour = "0" + hour;
			time_miu = "0" + miu % 60;
		} else if (miu >= 60 && hour < 10 && miu % 60 >= 10) {
			time_hour = "0" + hour;
			time_miu = "" + miu % 60;
		} else if (miu > 60 && hour < 24 && miu % 60 < 10) {
			time_hour = "" + hour;
			time_miu = "0" + miu % 60;
		} else if (miu > 60 && hour < 24 && miu % 60 >= 10) {
			time_hour = "" + hour;
			time_miu = "" + miu % 60;
		}

		time = time_hour + ":" + time_miu + ":" + time_second;
		return time;
	}


	private Paint paint, paint1, paint2, paint3, paint4, paint5;
	private Canvas canvas;
	private float ScreenW, ScreenH, oneWid;

	public void mydraw() {
		try {

			canvas = mSurfaceHolder.lockCanvas();
			if (canvas != null) {
				canvas.drawColor(Color.BLACK);
				DisplayMetrics dm = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(dm);
				ScreenW = dm.widthPixels;
				ScreenH = (dm.heightPixels) / 2;
				oneWid =ScreenW/600;

				canvas.drawText("FHR", ScreenW / 20 - 10, ScreenH / 20 - 10,
						paint);
				
				canvas.drawRect((ScreenW / 10), 6 * (ScreenH / 17), ScreenW,
						11 * (ScreenH / 17), paint4);
				

				for (int i = 0; i < 17; i++) {
					if (5<=i&&i<=10) {
							canvas.drawLine(ScreenW / 10, (i + 1) * (ScreenH / 17),
									ScreenW, (i + 1) * (ScreenH / 17), paint5);
					}else{
					canvas.drawLine(ScreenW / 10, (i + 1) * (ScreenH / 17),
							ScreenW, (i + 1) * (ScreenH / 17), paint);
					}
					int o = 210 - 10 * i;
					canvas.drawText(o + "", ScreenW / 20 - 10, (i + 1)
							* (ScreenH / 17) + 5, paint3);
					}

				for (int j = 1; j < 11; j++) {
					if (j == 1) {
						canvas.drawLine(j * (ScreenW / 10), (ScreenH / 17), j
								* (ScreenW / 10), ScreenH, paint);
					} else {
						canvas.drawLine(j * (ScreenW / 10), (ScreenH / 17), j
								* (ScreenW / 10), ScreenH, paint1);
						canvas.drawLine(j*(ScreenW/10),(ScreenH/17)*6 ,j*(ScreenW/10) ,(ScreenH/17)*11, paint5);
					}
				}
				
				
				//另加--------------
				/*for (int i = 6; i < 11; i++) {
					canvas.drawLine(ScreenW / 10, (i + 1) * (ScreenH / 17),
							ScreenW, (i + 1) * (ScreenH / 17), paint);
				}*/

				if (datalist.size() != 0) {

					canvas.drawLine((oneWid) * datalist.size()
							+ (ScreenW / 10), 0,
							( oneWid) * datalist.size() + (ScreenW / 10),
							ScreenH + 20, paint);
					for (int g = 1; g <datalist.size(); g++) {
						if(datalist.get(g)<50||datalist.get(g)>210){

						}
						else if(datalist.get(g-1)<50||datalist.get(g-1)>210){
							canvas.drawLine(
									(oneWid) * (g + 1) + (ScreenW / 10),
									((1 * ScreenH / 170) * (210 - datalist.get(g)))
									+ (ScreenH / 17),
									(oneWid) * (g + 1) + (ScreenW / 10),
									((1 * ScreenH / 170) * (210 - datalist.get(g)))
									+ (ScreenH / 17), paint3);

						}
						else{


							canvas.drawLine(
									(oneWid *g)  + (ScreenW / 10),
									((1 * ScreenH / 170) * (210 - datalist
											.get(g - 1))) + (ScreenH / 17),
									(oneWid * (g + 1)) + (ScreenW / 10),
									((1 * ScreenH / 170) * (210 - datalist.get(g)))
									+ (ScreenH / 17), paint3);
						}
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (canvas != null) {
				mSurfaceHolder.unlockCanvasAndPost(canvas);
			}
		}

	}

	private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {

		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {

		}

		@Override
		public void surfaceCreated(SurfaceHolder arg0) {
			paint = new Paint();
			paint1 = new Paint();
			paint2 = new Paint();
			paint3 = new Paint();
			paint4 = new Paint();
			paint5 = new Paint();

			paint.setColor(Color.rgb(250, 234, 100));
			paint.setTextSize(20);
			paint.setAntiAlias(true);

			paint1.setColor(Color.rgb(250, 234, 100));
			PathEffect effects = new DashPathEffect(new float[] { 8, 4, 8, 4 },
					1);
			paint1.setPathEffect(effects);
			paint1.setAntiAlias(true);

			paint2.setStrokeWidth(5);
			paint2.setColor(Color.WHITE);
			paint2.setAntiAlias(true);

			paint3.setColor(Color.WHITE);
			paint3.setStrokeWidth(3);
			paint3.setTextSize(20);
			paint3.setAntiAlias(true);

			paint4.setColor(Color.rgb(73, 73, 73));
			
			paint5.setColor(Color.rgb(48,172,206));
			paint5.setStrokeWidth(3);
			paint5.setAntiAlias(true);

			flag = true;
			Thread tr = new Thread(DeviceControlActivity.this);
			tr.start();

		}

		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
				int arg3) {

		}
	};

	@Override
	public void run() {
		while (flag) {
			mydraw();
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	private final int TIMER = 100;
	private final int MSG_ERR = 2;
	private final int MSG_SERVICE_INFOR = 10;
	private final int MSG_SERVICE_STATUS = 11;
	/**
	 * 服务的回到接口的实现
	 */
	BluetoothService.Callback mCallback = new BluetoothService.Callback() {

		@Override
		public void dispInfor(String infor) {
			Log.i("info","infor=="+infor);
			Message msg = Message.obtain();
			Bundle bundle = new Bundle();
			bundle.clear();
			bundle.putString("infor", infor);
			msg.setData(bundle);
			msg.what = MSG_SERVICE_INFOR;
			handler.sendMessage(msg);

		}

		@Override
		public void dispServiceStatus(String sta) {
			Message msg = Message.obtain();
			Bundle bundle = new Bundle();
			bundle.clear();
			bundle.putString("status", sta);
			msg.setData(bundle);
			msg.what = MSG_SERVICE_STATUS;
			handler.sendMessage(msg);
		}
	};

}
