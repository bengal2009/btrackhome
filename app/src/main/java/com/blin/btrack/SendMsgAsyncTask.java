package com.blin.btrack;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import org.json.JSONObject;


public class SendMsgAsyncTask {
	private BaiduPush mBaiduPush;
	private String mMessage;
	private Handler mHandler;
	private MyAsyncTask mTask;
	private OnSendScuessListener mListener;

	public interface OnSendScuessListener {
		void sendScuess(String msg);
	}

	public void setOnSendScuessListener(OnSendScuessListener listener) {
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

	public SendMsgAsyncTask(String jsonMsg,String useId) {
		// TODO Auto-generated constructor stub
		mBaiduPush = PushApplication.getInstance().getBaiduPush();
		mMessage = jsonMsg;
		mHandler = new Handler();
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
            String[] S1=null;
            //Sample RQLOCT,benny,eva
            try {
                JSONObject obj = new JSONObject(mMessage);
                S1= obj.getString("message").split(",");
//                Log.i("SendMSGAsyncBenny", S1[0]);
//                Log.i("SendMSGAsyncBenny", mMessage);
            }
            catch (Exception E)
            {
                Log.i("SendMSGAsyncBenny",E.toString());
            }
            if(S1[0].equals("RQLOCT"))
            {
                result = mBaiduPush.PushTagMessage(mMessage,S1[2]);
                Log.i("SendMSGAsyncBenny", S1[2]);
            }else {
                result = mBaiduPush.PushMessage(mMessage);
            }

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
