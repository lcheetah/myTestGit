package com.lcheetah.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import com.lcheetah.doppler.R;
import com.luckcome.lmtpdecorder.LMTPDecoder;
import com.luckcome.lmtpdecorder.LMTPDecoderListener;
import com.luckcome.lmtpdecorder.data.FhrData;
import com.luckcome.lmtpdecorder.data.FhrDataBuffer;
import com.luckcome.lmtpdecorder.help.Utils;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

@SuppressLint("NewApi")
public class BluetoothService extends Service {
	
	// 服务的回调接口
	Callback mCallback = null;
	
	// 蓝牙适配器 
	BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
	
    public static final UUID MY_UUID = 	UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// 服务当前连接的蓝牙设备
	BluetoothDevice mBtDevice;
	// 蓝牙 socket
	public BluetoothSocket mSocket = null;
	
	// 蓝牙输出流
	OutputStream mOutputStream = null;
	
	// 是否保存标志
	boolean isRecord = false;
	
	
	/** 终端协议解析器 */
	public LMTPDecoder mLMTPDecoder = null;
	/** 解析器回调接口 */
	LMTPDListener mLMTPDListener = null;
	
	
	@Override
	public IBinder onBind(Intent intent) {
		//Log.i("BluetoothService", "bindservice");
		return mBinder;
	}
	
	

	
	@Override
	public void onCreate() {
		mLMTPDecoder = new LMTPDecoder();
		mLMTPDListener = new LMTPDListener();
		mLMTPDecoder.setLMTPDecoderListener(mLMTPDListener);
		mLMTPDecoder.prepare();
	}




	@Override
	public boolean onUnbind(Intent intent) {
		cancel();
		return super.onUnbind(intent);
	}



	@Override
	public void onDestroy() {
		super.onDestroy();
		mLMTPDecoder.release();
		mLMTPDecoder = null;
		mLMTPDListener = null;
	}



	public BluetoothBinder mBinder = new BluetoothBinder();
	public class BluetoothBinder extends Binder {
		public BluetoothService getService() {
			return BluetoothService.this;
		}
	}
	
	/** 消息编号，连接完成 */
	public static final int MSG_CONNECT_FINISHED = 10;
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			
			switch(msg.what) {
			case MSG_CONNECT_FINISHED:
				if(mCallback != null) {
					mCallback.dispServiceStatus(getResources().getString(R.string.read_data_start));
				}
				isReading = true;
				mReadThread = new ReadThread();
				mReadThread.start();
				break;
			
			}
			
			
		}
		
	};
	
	
	
	/**
	 * 设置要连接的蓝牙设备
	 * @param device
	 */
	public void setBluetoothDevice(BluetoothDevice device) {
		mBtDevice = device;
	}
	/**
	 * 启动连接线程
	 */
	public void start() {

		if(mConnectThread == null)
			mConnectThread = new ConnectThread(mBtDevice);
		mConnectThread.start();
		mLMTPDecoder.startWork();
	}
	
	/**
	 * 停止数据读取和解析
	 */
	public void cancel() {
		
		isReading = false;
		if(mSocket != null) {
			try {
				mSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		mConnectThread = null;
		mReadThread = null;
		mLMTPDecoder.stopWork();
	}
	
	/**
	 * 设置回调接口
	 * @param cb
	 */
	public void setCallback(Callback cb) {
		mCallback = cb;
	}
	
	/**
	 * 服务的回调接口定义
	 */
	public interface Callback {
		/**
		 * 主要显示监护数据信息
		 * @param infor
		 */
		public void dispInfor(String infor);
		
		/**
		 * 主要显示记录状态
		 * @param sta
		 */
		public void dispServiceStatus(String sta);
	}
	
	/*
	 * 连接蓝牙
	 */
	ConnectThread mConnectThread = null;
	private class ConnectThread extends Thread {
    	private BluetoothDevice mDevice;
    	BluetoothSocket tmp = null;
    	
    	public ConnectThread(BluetoothDevice device) {
    		mDevice = mBtDevice;
    	}

		@Override
		public void run() {
			
    		try {
				tmp = mDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				if(mCallback != null) {
					mCallback.dispServiceStatus(getResources().getString(R.string.service_get_socket_fail));
				}
			}
    		
    		mSocket = tmp;
    		
			mAdapter.cancelDiscovery();
			
			try {
				mSocket.connect();
				if(mCallback != null) {
					mCallback.dispServiceStatus(getResources().getString(R.string.service_get_socket_ok));
				}
				mHandler.sendEmptyMessage(MSG_CONNECT_FINISHED);
			} catch (IOException e) {
				if(mCallback != null) {
					mCallback.dispServiceStatus(getResources().getString(R.string.service_get_socket_fail));
				}
			}
			
			try {
				mOutputStream = mSocket.getOutputStream();
			} catch (IOException e) {
				mOutputStream = null;
				e.printStackTrace();
			}
		}
    	
	}
	
	
	/*
	 * 读数据线程
	 */
	private boolean isReading = false;
	
	ReadThread mReadThread = null;
	private class ReadThread extends Thread {
		private InputStream mIs = null;
		

		@Override
		public void run() {
			try {
				mIs = mSocket.getInputStream();
				if(mCallback != null) {
					mCallback.dispServiceStatus(getResources().getString(R.string.service_get_socket_ok));
				}
			} catch (IOException e) {
				if(mCallback != null) {
					mCallback.dispServiceStatus(getResources().getString(R.string.service_get_socket_fail));
				}
				isReading = false;
			}
			

			int len;
			byte[] buffer = new byte[2048];
			while(isReading) {
				try {
					len = mIs.read(buffer);
					
					mLMTPDecoder.putData(buffer, 0, len);
					try {
						Thread.sleep(30);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					

				} catch (IOException e) {
					if(mCallback != null) {
						mCallback.dispServiceStatus(getResources().getString(R.string.service_read_data_fail));
					}
					isReading = false;
				} 
			}
			
			if(mSocket != null) {
				try {
					mSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		

		}
		
	}
	
	
	/**
	 * 获取工作状态
	 * @return
	 */
	public boolean getReadingStatus() {
		return isReading;
	}
	
	/**
	 * 启动记录功能
	 */
	public void recordStart() {
		
		File path = Utils.getRecordFilePath();
		
		String fname = "" + System.currentTimeMillis();
		
		mLMTPDecoder.beginRecordWave(path, fname);
		isRecord = true;
		
		if(mCallback != null)
			mCallback.dispServiceStatus(getResources().getString(R.string.recording));
		
	}
	
	/**
	 * 结束记录
	 */
	public void recordFinished() {
		isRecord = false;
		mLMTPDecoder.finishRecordWave();
		
		if(mCallback != null)
			mCallback.dispServiceStatus(getResources().getString(R.string.record_finished));

	}
	
	/**
	 * 获取记录状态
	 * @return
	 */
	public boolean getRecordStatus() {
		return isRecord;
	}
	
	
	/**
	 * 设置宫缩复位
	 * @param value		宫缩复位的值
	 */
	public void setTocoReset(int value) {
		mLMTPDecoder.sendTocoReset(value);
	}
	
	
	/**
	 * 设置一次手动胎动
	 */
	public void setFM() {
		mLMTPDecoder.setFM();
	}
	
	
	/**
	 * 设置胎心音量
	 * @param value		胎心音量大小
	 */
	public void setFhrVolume(int value) {
		mLMTPDecoder.sendFhrVolue(value);
	}
	
	
	/**
	 * 协议解析器回调接口
	 * @author Administrator
	 *
	 */
	class LMTPDListener implements LMTPDecoderListener {

		/**
		 * 有新数据产生的时候回调
		 */
		@Override
		public void fhrDataChanged(FhrData fhrData) {
			
	/*		String infor = String.format(
					"FHR1:%-3d\nTOCO:%-3d\n AFM:%-3d\nSIGN:%-3d\nBATT:%-3d\n"
					+ "isFHR1:%-3d\nisTOCO:%-3d\n isAFM:%-3d\n",
					fhrData.fhr1, fhrData.toco, fhrData.afm, fhrData.fhrSignal, fhrData.devicePower,
					fhrData.isHaveFhr1, fhrData.isHaveToco, fhrData.isHaveAfm
					);*/
			String infor =String.format("%-3d",fhrData.fhr1);
			if(fhrData.fmFlag != 0)
				Log.v("LMTPD", "LMTPD...1...fm");
			
			if(fhrData.tocoFlag != 0)
				Log.v("LMTPD", "LMTPD...2...toco");

			if(mCallback != null)
				mCallback.dispInfor(infor);
		}

		/**
		 * 有命令产生的时候回调
		 */
		@Override
		public void sendCommand(byte[] cmd) {
			// 这里添加从蓝牙发送数据的代码
			if(mOutputStream != null) {
				try {
					mOutputStream.write(cmd);
					mOutputStream.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
}
