package com.lcheetah.activity;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.lcheetah.doppler.R;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


/**
 * @author xy
 * 
 */
public class ResultActivity extends Activity {

	private SurfaceView result_SFV;
	// private TextView result_TV;
	private Button record_BT;
	private SurfaceHolder mSurfaceHolder;
	private Paint paint, paint1, paint2, paint3, paint4,paint5;
	private Canvas canvas;
	private float ScreenW, ScreenH;
	private String totaltime;
	private TextView recordTime;
	private List<Float> datalist = new ArrayList<Float>();
	private List<Float> countlist = new ArrayList<Float>();
	private String str;
	private Bitmap bitmap;
	private boolean ischeck;
	private String datastr = "";
	private String timer_total;
	private String path;
	private long currentTime;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_result);
		initData();
		Intent intent = getIntent();
		datalist = (List<Float>) intent.getSerializableExtra("data");
		totaltime = intent.getStringExtra("totaltime");
		recordTime.setText(totaltime);
		timer_total = intent.getStringExtra("timer_total");
		Log.v("=========++++++++++++========", timer_total);
		//		for (int z = 0; z < (sfvlist.size() - 4); z++) {
		//			// ÿ�����ȡƽ�������г�һ���µ�float�����飬�Ӳ������������ͼ����Դ��������µ�����
		//			float x = (sfvlist.get(z) + sfvlist.get(z + 1) + sfvlist.get(z + 2)
		//					+ sfvlist.get(z + 3) + sfvlist.get(z + 4)) / 5;
		//			countlist.add(x);
		//		}

		final int size = datalist.size();
		Float[] arr = datalist.toArray(new Float[size]);
		for (int i = 0; i < arr.length; i++) {
			// Toast.makeText(ResultActivity.this, "=="+arr[i],
			// Toast.LENGTH_SHORT).show();
			// Log.d("float",
			// "============="+arr[i]+"=========================");
			if (i == 0) {
				datastr = datastr + arr[i];
			} else {
				datastr = datastr + "," + arr[i];
			}

		}
		// Toast.makeText(ResultActivity.this, str, Toast.LENGTH_SHORT).show();

		Log.w("String", datastr);
		// String[] strString = str.split(",");
		// for (int i = 0; i < strString.length; i++) {
		// Log.w("===String===", strString[i]);
		// }

	}

	private void CreateDB() {

		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);

		path = Environment.getExternalStorageDirectory()+"/bestmanpic/";
		if(!isFileExist(path)){
			File tempf =createSDDir(path);
		}

		File file = new File(path,currentTime+".png");

		Log.i("info", "picPath="+file);
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(file));
			bitmap.compress(Bitmap.CompressFormat.PNG,100, bos);
			bos.flush();
			bos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SQLiteDatabase db = openOrCreateDatabase("data.db",
				ResultActivity.MODE_PRIVATE, null);
		db.execSQL("create table if not exists datatb(_id integer primary key autoincrement,date text not null,filepath text not null,totaltime text not null,timer_total text not null)");
		
		ContentValues values = new ContentValues();
		
		values.put("date", str);
		values.put("filepath",path+currentTime+".png");
		values.put("totaltime", totaltime);
		values.put("timer_total", timer_total);

		Log.w("===string=========", datastr);
		//values.put("getdata", datastr);
		db.insert("datatb", null, values);
		db.close();
	}

	private void initData() {

		/*SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy��MM��dd��    HH:mm:ss     ");*/
		//SimpleDateFormat formatter = new SimpleDateFormat(getString(R.string.time_set));
		//SimpleDateFormat eformatter = new SimpleDateFormat("dd MMMM yyyy    HH:mm:ss     ");
		//Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
		//str = formatter.format(curDate);
		//estr =eformatter.format(curDate);
		currentTime =System.currentTimeMillis();
		str =String.valueOf(currentTime);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		ScreenW = dm.widthPixels;
		ScreenH = (62 * (dm.heightPixels)) / 100;
		recordTime = (TextView) findViewById(R.id.totaltime);
		ischeck = false;
		result_SFV = (SurfaceView) findViewById(R.id.result_sfv);
		// result_TV = (TextView) findViewById(R.id.result_tv);
		record_BT = (Button) findViewById(R.id.result_record);
		// result_TV.setText("���������");

		record_BT.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (ischeck == false) {
					CreateDB();
					Toast.makeText(ResultActivity.this, R.string.sava,
							Toast.LENGTH_SHORT).show();
					ischeck = true;
				} else {
					Toast.makeText(ResultActivity.this, R.string.toast4,
							Toast.LENGTH_SHORT).show();
				}

			}
		});
		mSurfaceHolder = result_SFV.getHolder();
		mSurfaceHolder.addCallback(surfaceCallback);

	}

	public void mydraw() {
		try {

			canvas = mSurfaceHolder.lockCanvas();
			if (canvas != null) {
				canvas.drawColor(Color.BLACK);

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

				if (datalist.size() > 720) {
					for (int g = 1; g < datalist.size(); g++) {
						if (datalist.get(g) < 50 || datalist.get(g) > 210) {

						} else if (datalist.get(g - 1) < 50
								|| datalist.get(g - 1) > 210) {
							canvas.drawLine(
									(9 * ScreenW / (10 * datalist.size()))
									* (g + 1) + (ScreenW / 10),
									((1 * ScreenH / 170) * (210 - datalist
											.get(g))) + (ScreenH / 17),
									(9 * ScreenW / (10 * datalist.size()))
									* (g + 1) + (ScreenW / 10),
									((1 * ScreenH / 170) * (210 - datalist
											.get(g))) + (ScreenH / 17), paint3);
						} else {
							canvas.drawLine(
									(9 * ScreenW / (10 * datalist.size())) * g
									+ (ScreenW / 10),
									((1 * ScreenH / 170) * (210 - datalist
											.get(g - 1))) + (ScreenH / 17),
									(9 * ScreenW / (10 * datalist.size()))
									* (g + 1) + (ScreenW / 10),
									((1 * ScreenH / 170) * (210 - datalist
											.get(g))) + (ScreenH / 17), paint3);
						}
					}
				} else {
					for (int g = 1; g < datalist.size(); g++) {
						if (datalist.get(g) < 50 || datalist.get(g) > 210) {

						} else if (datalist.get(g - 1) < 50
								|| datalist.get(g - 1) > 210) {
							canvas.drawPoint((ScreenW / 600) * (g + 1) + (ScreenW / 10),
									((1 * ScreenH / 170) * (210 - datalist.get(g)))
									+ (ScreenH / 17), paint3);

							//						 canvas.drawLine(
							//						 (ScreenW / 600) * (g + 1) + (ScreenW / 10),
							//						 ((1 * ScreenH / 200) * (240 - datalist.get(g)))
							//						 + (ScreenH / 20),
							//						 (ScreenW / 600) * (g + 1) + (ScreenW / 10),
							//						 ((1 * ScreenH / 200) * (240 - datalist.get(g)))
							//						 + (ScreenH / 20), paint3);

						} else {
							canvas.drawLine(
									(ScreenW *g/ 600)  + (ScreenW / 10),
									((1 * ScreenH / 170) * (210 - datalist
											.get(g - 1))) + (ScreenH / 17),
									(ScreenW * (g + 1))/600 + (ScreenW / 10),
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
			doDraw();

			mydraw();

		}

		private void doDraw() {
			int mWidth = 0;  //图像宽  
			int	mHeight =0;  //图像高
			int mYsize = 0;	 //Y轴方向线段条数

			if (datalist.size()>720) {
				mWidth =(int) (ScreenW/600*datalist.size()+ScreenW/10);
				mYsize =datalist.size()/60+2;

			}else {
				mWidth = result_SFV.getWidth();
				mYsize = 11;
			}
			mHeight =result_SFV.getHeight();
			bitmap = Bitmap.createBitmap(mWidth,
					mHeight, Bitmap.Config.ARGB_8888);
			Canvas bitCanvas = new Canvas(bitmap);

			if (bitCanvas != null) {

				bitCanvas.drawColor(Color.BLACK);

				bitCanvas.drawText("FHR", ScreenW / 20 - 10, ScreenH / 20 - 10,
						paint);
				bitCanvas.drawRect((ScreenW / 10), 6 * (ScreenH / 17), mWidth,
						11* (ScreenH / 17), paint4);

				for (int i = 0; i < 17; i++) {
					bitCanvas.drawLine(ScreenW / 10, (i + 1) * (ScreenH /17),
							mWidth, (i + 1) * (ScreenH / 17), paint);
					int o = 210- 10 * i;
					bitCanvas.drawText(o + "", ScreenW / 20 - 10, (i + 1)
							* (ScreenH / 17) + 5, paint3);
				}

				for (int j = 1; j < mYsize; j++) {
					if (j == 1) {
						bitCanvas.drawLine(j * (ScreenW / 10), (ScreenH / 17),
								j * (ScreenW / 10), ScreenH, paint);
					} else {
						bitCanvas.drawLine(j * (ScreenW / 10), (ScreenH / 17),
								j * (ScreenW / 10), ScreenH, paint1);
					}
				}

				for (int g = 1; g < datalist.size(); g++) {
					if (datalist.get(g) < 50 || datalist.get(g) > 210) {

					} else if (datalist.get(g - 1) < 50
							|| datalist.get(g - 1) > 210) {
						bitCanvas.drawPoint((ScreenW / 600) * (g + 1) + (ScreenW / 10),
								((1 * ScreenH / 170) * (210 - datalist.get(g)))
								+ (ScreenH / 17), paint3);


					} else {
						bitCanvas.drawLine(
								(ScreenW *g/ 600)  + (ScreenW / 10),
								((1 * ScreenH / 170) * (210 - datalist
										.get(g - 1))) + (ScreenH / 17),
								(ScreenW * (g + 1))/600 + (ScreenW / 10),
								((1 * ScreenH / 170) * (210 - datalist.get(g)))
								+ (ScreenH / 17), paint3);
					}
				}
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
				int arg3) {

		}

		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {

		}

	};
	private boolean isFileExist(String path2) {
		File file = new File(path2);
		file.isFile();
		return file.exists();
	}

	private File createSDDir(String path2) {
		File dir = new File(path2);
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {

			System.out.println("createSDDir:" + dir.getAbsolutePath());
			System.out.println("createSDDir:" + dir.mkdir());
		}
		return dir;
	}
}
