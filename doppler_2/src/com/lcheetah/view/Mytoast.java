package com.lcheetah.view;

import android.content.Context;
import android.widget.Toast;

public class Mytoast {
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
}
