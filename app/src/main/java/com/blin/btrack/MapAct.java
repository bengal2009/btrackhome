package com.blin.btrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.gson.Gson;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

public class MapAct extends ActionBarActivity implements
        OnGetGeoCoderResultListener,
        SendMsgAsyncTask.OnSendScuessListener,
        SendTagMsgAsyncTask.OnSendTagScuessListener,
        CurLoc.OnCurSendScuessListener{
    private static final LatLng GEO_BEIJING = new LatLng(39.945, 116.404);
    public MyLocationListenner myListener = new MyLocationListenner();
    String TAGSTR = "LocationDemo";
    // 定位相
    LocationClient mLocClient;
    BitmapDescriptor mCurrentMarker;
    GeoCoder mSearch = null; // 搜索模?，也可去掉地?模??立使用
    BaiduMap mBaiduMap = null;
    MapView mMapView = null;
    boolean isFirstLoc = true;// 是否首次定位
    boolean isBundle = false;// 是否首次定位
    private LocationClientOption.LocationMode mCurrentMode;
    private Context mcontext;
    private LatLng CurPOI,ClientPOI;
    private Marker mMarkerA;
    /*
         * Push Relate
         * #Push Relate
         * @return int
           */
    PushApplication app;
    Gson mGson;
    String curMsg;
    CurLoc CurLocShow=new CurLoc(this);
    LocationInfo LocInfo=new LocationInfo();
    //Preference Declare
    private SharedPreferences settings;
    private  String data = "DATA";
    private  String UserID = "ID";
    private  String FirstTag = "FirstTag";
    private  String SecondTag = "SecondTag";
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
                    Log.i(TAGSTR,bindString);
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
                    Log.i(TAGSTR,msgLine);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mcontext = getApplicationContext();
      Bundle bundle =this.getIntent().getExtras();
        if(bundle!=null) {
            isBundle=!isBundle;}
        InitMap();
        InitPush();
        BitmapDescriptor bdA = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_marka);
      if(isBundle){

        Double latitude = Double.parseDouble(bundle.getString("latitude"));
        Double longtidude = Double.parseDouble(bundle.getString("longtidude"));
        Log.i(TAGSTR,"Latitude:"+bundle.getString("latitude"));
          ClientPOI=new LatLng(latitude,longtidude);
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
          mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                  .location(ClientPOI));
        }


    }

    public void InitPush()
    {
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
        if(!isBundle)
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
        if (settings.getString(FirstTag, "") != null)
            FirstTag = settings.getString(FirstTag, "");
        if (settings.getString(SecondTag, "") != null)
            SecondTag = settings.getString(SecondTag, "");
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
        } else if( id == R.id.back_map) {
            BackMap();
        }

        return super.onOptionsItemSelected(item);
    }
private void BackMap()
{
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
        Toast.makeText(this, result.getAddress(),
                Toast.LENGTH_LONG).show();

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
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                /*Log.i(TAG,"Latitude:"+Double.toString(location.getLatitude()));
                Log.i(TAG,"Longitude:"+Double.toString(location.getLongitude()));*/


                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
                mBaiduMap.animateMapStatus(u);
                mLocClient.stop();
                Log.i(TAGSTR,"mloclient Stop!");
            }
        }

        public void onReceivePoi(BDLocation poiLocation) {
        }
    }
}