package com.lcheetah.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.lcheetah.doppler.R;
import com.lcheetah.view.SweetAlertDialog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;
import android.widget.TextView;

/**
 * @author xy
 * 
 */
public class CheckActivity extends Activity {
	private TextView checkdate, getdata;
	private String date;
	private String time;
	private ImageView Ck_back;
	private Button delete_button;
	private List<Float> datalist = new ArrayList<Float>();
	private List<Float> reallist = new ArrayList<Float>();
	private String timer_total;
	
	private String totaltime;
	private TextView time_tv;
	private int daojishi;
	private String filepath;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.checkitem);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); 
		Log.i("info","CheckActivity====onCreate");
		try {
			initData();
			initDB();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void initDB() {

		SQLiteDatabase db = openOrCreateDatabase("data.db",
				ResultActivity.MODE_PRIVATE, null);
		Cursor c = db.query("datatb", null, "date=?", new String[] { time },
				null, null, "date");
		if (c != null) {
			while (c.moveToNext()) {
				// result = c.getString(c.getColumnIndex("result"));
				totaltime = c.getString(c.getColumnIndex("totaltime"));
				timer_total = c.getString(c.getColumnIndex("timer_total"));
				daojishi = Integer.parseInt(timer_total);
				time_tv.setText(totaltime);
			}
			c.close();
		}
		db.close();
	}

	private void initData() {
		// ��ȡ���ؼ����е�TextView
		checkdate = (TextView) findViewById(R.id.check_date);
		time_tv = (TextView) findViewById(R.id.resulttime);
		PhotoView photoView = (PhotoView) findViewById(R.id.iv_photo);
		 final PhotoViewAttacher attacher = new PhotoViewAttacher(photoView);
		Intent intent = getIntent();
		date = intent.getStringExtra("checkdate");
		time =intent.getStringExtra("checktime");
		filepath =intent.getStringExtra("filepath");
		Log.i("info", "filepath==========="+filepath);
		File file = new File(filepath);
		Picasso.with(this).load(file).into(photoView, new Callback() {
            @Override
            public void onSuccess() {
                attacher.update();
            }

            @Override
            public void onError() {
            }
        });		
		
		
		checkdate.setText(date);
		delete_button = (Button) findViewById(R.id.delete_button);
		// ɾ����ť�ĵ��ʱ��
		delete_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
		/*		AlertDialog.Builder builder = new Builder(CheckActivity.this);
				builder.setMessage(R.string.Confirm_to_delete);
				builder.setTitle(R.string.note);
				builder.setPositiveButton(R.string.confirm,
						new android.content.DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								SQLiteDatabase db = openOrCreateDatabase(
										"data.db", MODE_PRIVATE, null);
								db.delete("datatb", "date=?",
										new String[] { time });
								db.close();
								finish();
							}
						});
				builder.setNegativeButton(R.string.cancel,
						new android.content.DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int arg1) {
								dialog.dismiss();
							}
						});
				builder.create().show();*/
				
				  new SweetAlertDialog(CheckActivity.this, SweetAlertDialog.WARNING_TYPE)
                  .setTitleText("Are you sure?")
                  .setContentText("Won't be able to recover this file!")
                  .setCancelText("No,cancel plx!")
                  .setConfirmText("Yes,delete it!")
                  .showCancelButton(true)
                  .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                      @Override
                      public void onClick(SweetAlertDialog sDialog) {
                          // reuse previous dialog instance, keep widget user state, reset them if you need
                          sDialog.setTitleText("Cancelled!")
                                  .setContentText("Your imaginary file is safe :)")
                                  .setConfirmText("OK")
                                  .showCancelButton(false)
                                  .setCancelClickListener(null)
                                  .setConfirmClickListener(null)
                                  .changeAlertType(SweetAlertDialog.ERROR_TYPE);

                          // or you can new a SweetAlertDialog to show
                         /* sDialog.dismiss();
                          new SweetAlertDialog(SampleActivity.this, SweetAlertDialog.ERROR_TYPE)
                                  .setTitleText("Cancelled!")
                                  .setContentText("Your imaginary file is safe :)")
                                  .setConfirmText("OK")
                                  .show();*/
                      }
                  })
                  .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                      @Override
                      public void onClick(SweetAlertDialog sDialog) {
                    	  SQLiteDatabase db = openOrCreateDatabase(
                    			  "data.db", MODE_PRIVATE, null);
                    	  db.delete("datatb", "date=?",
                    			  new String[] { time });
                    	  db.close();
                    	  finish();
                          sDialog.setTitleText("Deleted!")
                                  .setContentText("Your imaginary file has been deleted!")
                                  .setConfirmText("OK")
                                  .showCancelButton(false)
                                  .setCancelClickListener(null)
                                  .setConfirmClickListener(null)
                                  .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                      }
                  })
                  .show();
			}
		});

		Ck_back = (ImageView) findViewById(R.id.Ck_back);
		Ck_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();

			}
		});
	}


	@Override
	protected void onStart() {
		super.onStart();
		Log.w("==========", "onStart");

	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.w("==========", "onRestart" + reallist.size());

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.w("==========", "onResume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.w("==========", "onPause");

	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i("info", "CheckActivity destroy");
		Log.w("==========", "onDestroy");
		
	}



}
