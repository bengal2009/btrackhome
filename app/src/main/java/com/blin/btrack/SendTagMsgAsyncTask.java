package com.blin.btrack;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;


public class SendTagMsgAsyncTask {
	private BaiduPush mBaiduPush;
	private String mMessage;
    private String mTagStr;
	private Handler mHandler;
	private MyAsyncTask mTask;
	private OnSendTagScuessListener mListener;

	public interface OnSendTagScuessListener {
		void sendScuess(String msg);
	}

	public void setOnSendTagScuessListener(OnSendTagScuessListener listener) {
		this.mListener = listener;
	}

	Runnable reSend = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.i("reSend","resend msg...");
			send();//重?
		}
	};

	public SendTagMsgAsyncTask(String jsonMsg,String useId,String TagStr) {
		// TODO Auto-generated constructor stub
		mBaiduPush = PushApplication.getInstance().getBaiduPush();
		mMessage = jsonMsg;
		mHandler = new Handler();
        mTagStr=TagStr;
	}

	// ?送
	public void send() {
		//TODO 需判?有?有网?
		mTask = new MyAsyncTask();
		mTask.execute();
	}

	// 停止
	public void stop() {
		if (mTask != null)
			mTask.cancel(true);
	}

	class MyAsyncTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... message) {
			String result = "";
//				result = mBaiduPush.PushMessage(mMessage);
				result = mBaiduPush.PushTagMessage(mMessage,mTagStr);

			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			Log.i("SendMsgAsyncTask","send msg result:"+result);
			if (result.contains(BaiduPush.SEND_MSG_ERROR)) {// 如果消息发送失败，100ms后重送
				mHandler.postDelayed(reSend, 100);
			} else {
				if (mListener != null)
					mListener.sendScuess(mMessage);
			}
		}
	}
}
