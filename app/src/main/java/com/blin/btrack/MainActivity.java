package com.blin.btrack;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.android.pushservice.PushManager;
import com.blin.btrack.GPS.CurLoc;
import com.blin.btrack.GPS.LocationInfo;
import com.google.gson.Gson;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends Activity implements SendMsgAsyncTask.OnSendScuessListener,
        SendTagMsgAsyncTask.OnSendTagScuessListener,
        CurLoc.OnCurSendScuessListener {

    PushApplication app;
    Gson mGson;
    String curMsg;
    CurLoc CurLocShow=new CurLoc(this);

    LocationInfo LocInfo=new LocationInfo();
    BroadcastReceiver commReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("onBind")) {
                Bundle bindData = intent.getBundleExtra("onBind");
                int errorCode = bindData.getInt("errorCode");
                String bindString;
                if (errorCode == 0) {
                    bindString = " 用户Id: " + bindData.getString("userId") + "\n 通道Id: " + bindData.getString("channelId");
                } else {
                    bindString = "推送服务失败：" + errorCode;
                    Toast.makeText(getApplicationContext(), "推送服务失败：" + errorCode,
                            Toast.LENGTH_LONG).show();
                }
                ((TextView) findViewById(R.id.View1)).append(bindString);

            } else if (intent.hasExtra("onMessage")) {
                String msgLine = "";
                try {
                    Message msg = (Message) intent.getSerializableExtra("onMessage");
                    String userNumber = "(No." + msg.getUser_id().substring(msg.getUser_id().length() - 4) + ")";
                    Timestamp tt = new Timestamp(msg.getTime_samp());
                    msgLine = "收到消息" + tt.getHours() + ":" + tt.getMinutes()
                            + "：" + userNumber + msg.getMessage() + "\n";
                    Log.i("onmessage", "Onmessage start!");
//                    ((TextView)findViewById(R.id.shmsg)).append(msgLine);
                    Log.i("onReceive", msgLine);
                } catch (Exception e) {
                    msgLine = "收到消息" + intent.getStringExtra("onMessage") + "\n";

                } finally {
                    ((TextView) findViewById(R.id.textView2)).append(msgLine);
                }

            } else if (intent.hasExtra("onSetTags")) {
                String info = intent.getStringExtra("onSetTags");
                Log.i("onReceive", info);
                ((TextView) findViewById(R.id.textView2)).append(info);
            } else if (intent.hasExtra("onListTags")) {
               /* String info = intent.getStringExtra("onListTags");
                ((TextView)findViewById(R.id.textView2)).append(info);*/
                Bundle bindData = intent.getBundleExtra("onListTags");
                ArrayList<String> tagstr = bindData.getStringArrayList("tags");
                app.setListTags(tagstr);
                for (String s1 : tagstr) {
                    ((TextView) findViewById(R.id.textView2)).append(s1 + "\n");

                }


            } else if (intent.hasExtra("onDelTags")) {
                String info = intent.getStringExtra("onDelTags");
                ((TextView) findViewById(R.id.textView2)).append(info);
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BackgoundService.luanch(getApplicationContext());
        registerMessageCommReceiver();
        setContentView(R.layout.activity_main);
        ((TextView) findViewById(R.id.View1)).setText("推送准备。。。\n");
        app = PushApplication.getInstance();
        mGson = app.getGson();
        CurLocShow.setOnCurSendScuessListener(this);
    }

    private void registerMessageCommReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MessageReceiver.ACTION_COMMUNICATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(commReceiver, intentFilter);
    }

    public void send(View v) {
        String userId = app.getUserId();
        String channelId = app.getChannelId();
        EditText etMessage = ((EditText) findViewById(R.id.etMsg));
        curMsg = etMessage.getText().toString();
        Message message = new Message(userId, channelId, System.currentTimeMillis(), curMsg, "");
        SendMsgAsyncTask task = new SendMsgAsyncTask(mGson.toJson(message), userId);
        task.setOnSendScuessListener(this);
        task.send();
        etMessage.setText("");
        InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputmanger.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
    }
    //Current Location
    public void CurLocation(View v){
        Toast.makeText(getApplicationContext(), "Click! ",
                Toast.LENGTH_SHORT).show();
       CurLoc a1=new CurLoc(this);
        a1.InitLoc();
       /* LocInfo=a1.ReturnLocInfo();
        Toast.makeText(getApplicationContext(), Double.toString(LocInfo.getLatitude()),
                Toast.LENGTH_SHORT).show();*/
    }
    //SendTag
    public void SendTag(View v) {
        final String userId = app.getUserId();
        String channelId = app.getChannelId();
        String TagStr;
        final EditText etMessage = ((EditText) findViewById(R.id.etMsg));
        curMsg = etMessage.getText().toString();
        final Message message = new Message(userId, channelId, System.currentTimeMillis(), curMsg, "");
        Log.i("SendTag", "Sendtag Start!");

        //Create Alert Dialogue
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);


        final EditText textviewGid = new EditText(this);
        textviewGid.setHint("以英文逗号隔开：");
        layout.addView(textviewGid);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                this);
        builder.setView(layout);
        builder.setPositiveButton("照TAG发送",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

//                        task.setOnSendTagScuessListener(this);
                       /* String userId = app.getUserId();
                        SetTagTask task = new SetTagTask(textviewGid.getText().toString(), userId);
                        task.setTags();*/

                        SendTageCall(userId,message,textviewGid.getText().toString());
                        etMessage.setText("");
                        InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputmanger.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
                    }

                });
        builder.show();
        //Alert Over

    }
    public void SendTageCall(String userId,Message message,String Tagname)
    {
        Log.i("SendTag", "Sendtag Call");
        SendTagMsgAsyncTask task = new SendTagMsgAsyncTask(mGson.toJson(message), userId, Tagname);
        task.setOnSendTagScuessListener(this);
        task.send();
    }
    //Original Settag
    /*public void setTag(View v) {
		String userId = app.getUserId();
		SetTagTask task = new SetTagTask("TAG_GROUP", userId);
		task.setTags();
	}*/
    public void setTag(View v) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText textviewGid = new EditText(this);
        textviewGid.setHint("以英文逗号隔开：");
        layout.addView(textviewGid);

        AlertDialog.Builder builder = new AlertDialog.Builder(
                this);
        builder.setView(layout);
        builder.setPositiveButton("设置TAG",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // Push: ?置tag?用方式
                      /*  List<String> tags = Utils.getTagsList(textviewGid
                                .getText().toString());
                        PushManager.setTags(getApplicationContext(), tags);*/
                        String userId = app.getUserId();
                        try
                        {
                            SetTagTask task = new SetTagTask(textviewGid.getText().toString(), userId);
                            task.setTags();
                        }
                        catch(Exception e)
                        {
                            Log.i("Main",e.toString());
                        }

                    }

                });
        builder.show();
    }

    public void ListTag(View v) {
        try
        {
        PushManager.listTags(this);}
        catch(Exception e)
        {
            Log.i("Main",e.toString());
        }
    }

    public void DelTag(View v) {
        try
        {
        PushManager.listTags(this);
        PushManager.delTags(this, app.getListTags());}
        catch(Exception e)
        {
            Log.i("Main",e.toString());
        }
    }
    //Check Location
    public void CurLocationClick(View V)
    {
       if(CurLocShow.GetLocaFlag==false)
        {
        CurLocShow.InitLoc();
    }
        else{
           Log.i("MainActivity","Location Start!");
        CurLocShow.mLocationClient.start();
    }

    }

    //Display Location String
    @Override
    public void CurLocsendScuess(LocationInfo CurrentLoc)
    {
//        Log.i("getLatitude", Double.toString(CurrentLoc.getLatitude()));
        StringBuilder sb=new StringBuilder();;
        sb.append("RQLOC,");
        sb.append(app.getUserId()+",");
        sb.append(Double.toString(CurrentLoc.getLatitude())+",");
        sb.append(Double.toString(CurrentLoc.getLongitude()));



        final EditText etMessage = ((EditText) findViewById(R.id.etMsg));
        etMessage.setText(sb.toString());
    }
    @Override
    public void sendScuess(String msg) {
        Calendar calendar = Calendar.getInstance();
        String time = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
        ((TextView) findViewById(R.id.textView2)).append("已送出" + time + "：" + curMsg + "\n");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(commReceiver);
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle("是否退出");
        builder.setPositiveButton("完全退出",
                new DialogInterface.OnClickListener() {
                    @SuppressWarnings("deprecation")
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                        ActivityManager activityMgr = (ActivityManager) MainActivity.this.getSystemService(Context.ACTIVITY_SERVICE);
                        activityMgr.restartPackage(MainActivity.this.getPackageName());
                        activityMgr.killBackgroundProcesses(MainActivity.this.getPackageName());
                        System.exit(0);
                    }
                });
        builder.setNegativeButton("退居后台",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MainActivity.super.onBackPressed();
                    }
                });
        builder.show();

    }


}

