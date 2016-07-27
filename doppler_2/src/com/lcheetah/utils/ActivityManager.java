package com.lcheetah.utils;
import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;

public class ActivityManager {
	public static ActivityManager activityManager;
	private static List<Activity> list;
	private ActivityManager() {
		list = new ArrayList<Activity>();
	}
	public static ActivityManager getInstance(){
		if (activityManager == null) {
			activityManager = new ActivityManager();
		}
		return activityManager;
	}
	/*public void toMainAcitivity(Activity act){
		Intent intent = new Intent(act,MainActivity.class);
		act.startActivity(intent);
	}
	public  void toHelpAcitivity(Activity act){
		Intent intent = new Intent(act,HelpActivity.class);
		act.startActivity(intent);
	}
	public  void toMoreAcitivity(Activity act){
		Intent intent = new Intent(act,MoreActivity.class);
		act.startActivity(intent);
	}
	public void toResultAcitivity(Activity act){
		Intent intent = new Intent(act,ResultActivity.class);
		act.startActivity(intent);
	}*/
	@SuppressLint("NewApi")
	public void quit(){
		for (Activity activity : list) {
			if (!activity.isDestroyed()) {
				activity.finish();
			}
		}
	}
	public void addActivity(Activity act){
		list.add(act);
	}
}
