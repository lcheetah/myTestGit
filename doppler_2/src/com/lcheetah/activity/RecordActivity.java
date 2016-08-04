package com.lcheetah.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.lcheetah.doppler.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView.PictureListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.Toast;


/**
 * @author xy
 *
 */
@SuppressLint("SimpleDateFormat")
public class RecordActivity extends Activity {
	private ImageView Rc_back;
	private GridView gv;
	private List<String> dateList = new ArrayList<String>();
	private List<String> timeList = new ArrayList<String>();
	private List<String> fileList = new ArrayList<String>();
	private boolean click = true;
	private String filepath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		try {

			setContentView(R.layout.activity_record);
			initData();
			initDB();
			initGridView();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void initData() {
		Rc_back = (ImageView) findViewById(R.id.Rc_back);
		Rc_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}


	@Override
	protected void onRestart() {
		super.onRestart();
		Log.v("shengmingzhouqi", "onrestart");
		dateList.clear();
		initDB();
		initGridView();

	}


	private void initGridView() {
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < dateList.size(); i++) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("imageItem", R.drawable.card_01);// ���ͼ����Դ��ID
			item.put("textItem", dateList.get(i));// ��������ItemText
			items.add(item);
		}

		SimpleAdapter adapter = new SimpleAdapter(this, items,
				R.layout.gridview_item,
				new String[] { "imageItem", "textItem" }, new int[] {
						R.id.image_item, R.id.text_item });

		gv = (GridView) findViewById(R.id.mygridview);
		gv.setAdapter(adapter);
		gv.setOnItemClickListener(new ItemClickListener());
	}

	@SuppressLint("SimpleDateFormat")
	private void initDB() {
		SQLiteDatabase db = openOrCreateDatabase("data.db",
				ResultActivity.MODE_PRIVATE, null);
		Cursor c = db.query("datatb", null, "_id>?", new String[] { "0" },
				null, null, "date DESC");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日    HH:mm:ss     ");
		SimpleDateFormat eformatter = new SimpleDateFormat("dd MMMM yyyy    HH:mm:ss     ");
		if (c != null) {
			while (c.moveToNext()) {
				String data =null;
				Date date =null;
				String time=null;
				try {
					filepath =c.getString(c.getColumnIndex("filepath"));
					time=c.getString(c.getColumnIndex("date"));
					date =new Date(Long.valueOf(time));
					if ((Locale.getDefault().getLanguage()).equals("zh")) {
						data =formatter.format(date);
					}else{
						data =eformatter.format(date);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				fileList.add(filepath);
				timeList.add(time);
				dateList.add(data);
				//dateList.add(c.getString(c.getColumnIndex("date")));

			}
			c.close();
		}
	}
	OnItemLongClickListener clickListener=new OnItemLongClickListener() {        
		@Override        
		public boolean onItemLongClick(AdapterView<?> arg0, View view,              
				int position, long arg3) {

			Toast.makeText(RecordActivity.this, "diandadadad", Toast.LENGTH_SHORT).show();
			click = false;
			return click;   
		}    
	};



	class ItemClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> arg0,

				View arg1, int arg2, long arg3) {


			@SuppressWarnings("unchecked")
			HashMap<String, Object> item = (HashMap<String, Object>) arg0
			.getItemAtPosition(arg2);
			Intent intent = new Intent(RecordActivity.this, CheckActivity.class);
			intent.putExtra("checkdate", (String) item.get("textItem"));
			intent.putExtra("checktime",timeList.get(arg2));
			intent.putExtra("filepath",fileList.get(arg2));
			Log.i("info", "RecordActivity----------filepath"+ filepath);
			startActivity(intent);
		}
	}

}
