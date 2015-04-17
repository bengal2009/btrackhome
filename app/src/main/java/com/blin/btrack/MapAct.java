package com.blin.btrack;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.blin.btrack.GPS.CurLoc;
import com.blin.btrack.GPS.LocationInfo;
import com.blin.btrack.GPS.OfflineDemo;
import com.blin.btrack.UPDATE.ChkUpdate;
import com.google.gson.Gson;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

public class MapAct extends ActionBarActivity implements
        OnGetGeoCoderResultListener,
        SendMsgAsyncTask.OnSendScuessListener,
        SendTagMsgAsyncTask.OnSendTagScuessListener,
        CurLoc.OnCurSendScuessListener {
    private static final LatLng GEO_BEIJING = new LatLng(39.945, 116.404);
    public MyLocationListenner myListener = new MyLocationListenner();
    String TAGSTR = "LocationDemo";
    //Push Over
    BroadcastReceiver commMapReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("onBind")) {
                Bundle bindData = intent.getBundleExtra("onBind");
                int errorCode = bindData.getInt("errorCode");
                String bindString;
                if (errorCode == 0) {
                    bindString = " 用户Id: " + bindData.getString("userId") + "\n 通道Id: " + bindData.getString("channelId");
                    Log.i(TAGSTR, bindString);
                } else {
                    bindString = "推送服务失败：" + errorCode;
                    Toast.makeText(getApplicationContext(), "推送服务失败：" + errorCode,
                            Toast.LENGTH_LONG).show();
                }

            } else if (intent.hasExtra("onMessage")) {
                String msgLine = "";
                try {
                    Message msg = (Message) intent.getSerializableExtra("onMessage");
                    String userNumber = "(No." + msg.getUser_id().substring(msg.getUser_id().length() - 4) + ")";
                    Timestamp tt = new Timestamp(msg.getTime_samp());
                    msgLine = "收到消息" + tt.getHours() + ":" + tt.getMinutes()
                            + "：" + userNumber + msg.getMessage() + "\n";
                } catch (Exception e) {
                    msgLine = "收到消息" + intent.getStringExtra("onMessage") + "\n";

                } finally {
//                    ((TextView) findViewById(R.id.textView2)).append(msgLine);
                    Log.i(TAGSTR, msgLine);
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
    // 定位相
    LocationClient mLocClient;
    BitmapDescriptor mCurrentMarker;
    GeoCoder mSearch = null; // 搜索模?，也可去掉地?模??立使用
    BaiduMap mBaiduMap = null;
    MapView mMapView = null;
    boolean isFirstLoc = true;// 是否首次定位
    boolean isBundle = false;// 是否首次定位
    /*
         * Push Relate
         * #Push Relate
         * @return int
           */
    PushApplication app;
    Gson mGson;
    String curMsg, CurTag;
    CurLoc CurLocShow = new CurLoc(this);
    LocationInfo LocInfo = new LocationInfo();
    private LocationClientOption.LocationMode mCurrentMode;
    private Context mcontext;
    private LatLng CurPOI, ClientPOI;
    private Marker mMarkerA;
    //Preference Declare
    private SharedPreferences settings;
    private String data = "DATA";
    private String UserID = "ID";
    private String FirstTag = "FirstTag";
    private String SecondTag = "SecondTag";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mcontext = getApplicationContext();
        app = PushApplication.getInstance();
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            isBundle = true;
        }
        InitMap();
        InitPush();
        BitmapDescriptor bdA = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_marka);
        if (isBundle) {
            String cmd = bundle.getString("CMD");
            if (cmd.equals("CHKUPD")) {
                ChkUpdate A1=new ChkUpdate();
                A1.StartCheck(this);
            } else if (cmd.equals("CULOC")) {
                Double latitude = Double.parseDouble(bundle.getString("latitude"));
                Double longtidude = Double.parseDouble(bundle.getString("longtidude"));
                CurTag = bundle.getString("sendertag");
         /* notif(this,"目前位置","Latitude:"+bundle.getString("latitude")+
                  ",Longtitude"+bundle.getString("longtidude"));*/

//            Log.i(TAGSTR, "Latitude:" + bundle.getString("latitude"));
                ClientPOI = new LatLng(latitude, longtidude);
            /*MapStatusUpdate u4 = MapStatusUpdateFactory
                  .newLatLng(ClientPOI);
          MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.0f);

            mBaiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                    MyLocationConfiguration.LocationMode.NORMAL, true,bdA ));
                    mBaiduMap.setMapStatus(u4);
          mBaiduMap.setMapStatus(msu);
          mLocClient.stop();*/
                mBaiduMap.clear();
                mBaiduMap.addOverlay(new MarkerOptions().position(ClientPOI)
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.icon_marka)));
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(ClientPOI));
                MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(17.0f);
                mBaiduMap.setMapStatus(msu);
                mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                        .location(ClientPOI));
            }
        }

    }

    public void InitPush() {
        MapBackgoundService.luanch(getApplicationContext());
//        registerMessageCommReceiver();
        app = PushApplication.getInstance();
        mGson = app.getGson();
        ReadPref();
        CurLocShow.setOnCurSendScuessListener(this);
    }

    public void InitMap() {


        // 地?初始化
        mMapView = (MapView) findViewById(R.id.bmapView);

        mBaiduMap = mMapView.getMap();
        // ??定位??
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打?gps
        option.setCoorType("bd09ll"); // ?置坐??型
        option.setScanSpan(1000);
        option.setProdName("Benny");
        mLocClient.setLocOption(option);
        if (!isBundle)
            mLocClient.start();
        // 初始化搜索模?，注?事件?听
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
    }

    private void registerMessageCommReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MessageReceiver.ACTION_COMMUNICATION);
        LocalBroadcastManager.getInstance(this).registerReceiver(commMapReceiver, intentFilter);
    }

    private void ReadPref() {
        settings = getSharedPreferences(data, 0);
        if (settings.getString(FirstTag, "") != null) {
            FirstTag = settings.getString(FirstTag, "");
            CurTag = FirstTag;
        }
        if (settings.getString(SecondTag, "") != null)
            SecondTag = settings.getString(SecondTag, "");
    }

    /*
         * CurLocsendScuess(LocationInfo CurrentLoc)
         * #
         * @return int
           */
    //Display Location String
    @Override
    public void CurLocsendScuess(LocationInfo CurrentLoc) {
//        Log.i("getLatitude", Double.toString(CurrentLoc.getLatitude()));
        StringBuilder sb = new StringBuilder();
        ;
        sb.append("RQLOC,");
        sb.append(app.getUserId() + ",");
        sb.append(Double.toString(CurrentLoc.getLatitude()) + ",");
        sb.append(Double.toString(CurrentLoc.getLongitude()));


    }

    @Override
    public void sendScuess(String msg) {
        Calendar calendar = Calendar.getInstance();
        String time = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.back_map) {
            BackMap();
        } else if (id == R.id.check_map) {
            checkmap();
        } else if (id == R.id.offlinemap) {
            Intent intent = new Intent(this, OfflineDemo.class);
            startActivity(intent);
        }else if (id == R.id.checkupdate) {
            ChkUpdate A1=new ChkUpdate();
            A1.StartCheck(this);
        }


        return super.onOptionsItemSelected(item);
    }

    private void checkmap() {
        //Create Alert Dialogue
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);


        final EditText textviewGid = new EditText(this);
        textviewGid.setHint("以英文逗号隔开：");
        layout.addView(textviewGid);
        textviewGid.setText(SecondTag.toString());
        AlertDialog.Builder builder = new AlertDialog.Builder(
                this);
        builder.setView(layout);
        builder.setPositiveButton("发送",
                new DialogInterface.OnClickListener() {


                    public void onClick(DialogInterface dialog, int which) {
//                    String cuMsg="RQLOCT,"+FirstTag+","+textviewGid.getText().toString();
//                    Log.i(TAGSTR,textviewGid.getText().toString());
                        try {
                            if (textviewGid.getText().toString() == "") return;
                            String Tempstr = textviewGid.getText().toString();

                            String Msg1 = "RQLOCT," + FirstTag + "," + Tempstr;
                            Log.i(TAGSTR, Msg1);
                            String UserID, CNLID = null;
                            UserID = app.getUserId();
                            CNLID = app.getChannelId();
                            CurTag = textviewGid.getText().toString();
                            Message message1 = new Message(UserID, CNLID, System.currentTimeMillis(), Msg1, Tempstr);
                            MapSendTageCall(UserID, message1, Tempstr);
                        } catch (Exception E) {
                            Log.i(TAGSTR, "Stop:" + textviewGid.getText().toString());
                        }
                    }

                });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void MapSendTageCall(String userId, Message message, String Tagname) {
        SendTagMsgAsyncTask task = new SendTagMsgAsyncTask(mGson.toJson(message), userId, Tagname);
        task.setOnSendTagScuessListener(this);
        task.send();
    }

    private void BackMap() {
    /*final LatLng GEO_BEIJING = new LatLng(39.945, 116.404);
    ClientPOI=new LatLng(31,121);
    BitmapDescriptor bdA = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_marka);

   mBaiduMap.clear();
    mBaiduMap.addOverlay(new MarkerOptions().position(ClientPOI)
            .icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.icon_marka)));
    mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(ClientPOI));*/
        mLocClient.start();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        // 退出???定位
        mLocClient.stop();
        // ??定位??
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(this, "onGetGeoCodeResult抱歉", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        mBaiduMap.clear();
        mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_marka)));
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
                .getLocation()));
        String strInfo = String.format("?度：%f ?度：%f",
                result.getLocation().latitude, result.getLocation().longitude);
        Toast.makeText(this, strInfo, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(this, "Sorry! Not found!", Toast.LENGTH_LONG)
                    .show();

            return;
        }
        /*
        mBaiduMap.clear();
        mBaiduMap.addOverlay(new MarkerOptions().position(result.getLocation())
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.icon_marka)));
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(result
                .getLocation()));*/
        Log.i(TAGSTR, "onGetReverseGeoCodeResult");
        Toast.makeText(this, result.getAddress(),
                Toast.LENGTH_LONG).show();
        setTitle(FirstTag + ":" + result.getAddress());

    }

    private void notif(Context context, String title, String msg) {
       /* NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        mBuilder.setTicker("您有新的消息哦！");
        mBuilder.setSmallIcon(R.drawable.ic_launcher);
        mBuilder.setContentTitle("消息");

            mBuilder.setContentText(msg);
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(1, mBuilder.build());
        //Toast.makeText(arg0, msg.getMessage(), Toast.LENGTH_SHORT).show();*/
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(msg);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }

    /**
     * 定位SDK?听函?
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view ??后不在?理新接收的位置
            if (location == null || mMapView == null)
                return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                            // 此??置??者?取到的方向信息，???0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            /*Log.i(TAG,"Latitude:"+Double.toString(location.getLatitude()));
            Log.i(TAG,"Longitude:"+Double.toString(location.getLongitude()));*/
            CurPOI = new LatLng(location.getLatitude(), location.getLongitude());

            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
               /* LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());

                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
                mLocClient.stop();
                Log.i(TAGSTR,"mloclient Stop!");*/
            }
            LatLng ll = new LatLng(location.getLatitude(),
                    location.getLongitude());

            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
            mBaiduMap.animateMapStatus(u);
            mLocClient.stop();
            mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                    .location(CurPOI));
            Log.i(TAGSTR, "mloclient Stop!");
        }
    }
}