package com.blin.btrack.GPS;

import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.GeofenceClient;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.blin.btrack.R;
public class BaiduLoc extends ActionBarActivity implements OnGetGeoCoderResultListener {
    private TextView LocationResult,ModeInfor;
    private Button startLocation;
    private RadioGroup selectMode,selectCoordinates;
    private EditText frequence;
    private LocationClientOption.LocationMode tempMode = LocationClientOption.LocationMode.Hight_Accuracy;
    private String tempcoor="bd09ll";
    private CheckBox checkGeoLocation;
    private LatLng CurPOI;
    private GeoCoder mSearch;
    public LocationClient mLocationClient;
    public GeofenceClient mGeofenceClient;
    public MyLocationListener mMyLocationListener;


    public TextView mLocationResult,logMsg;
    public TextView trigger,exit;
    public Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baidu_loc);
        LocationResult = (TextView)findViewById(R.id.textView1);
        mLocationClient = new LocationClient(this.getApplicationContext());
        mMyLocationListener = new MyLocationListener();
//        mLocationClient.registerLocationListener(mMyLocationListener);
        mGeofenceClient = new GeofenceClient(getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(tempMode);
        option.setOpenGps(true);// 打?gps
        option.setCoorType("bd09ll"); // 置坐??型
        option.setScanSpan(1000);
        option.setProdName("BennyLoc");
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
        mLocationClient.registerLocationListener(mMyLocationListener);
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
    }
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        mLocationClient.stop();
        mLocationClient=null;
        mMyLocationListener=null;
        super.onStop();
    }

    private void InitLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(tempMode);//扢离隅弇耀宒
        option.setCoorType(tempcoor);//殿隙腔隅弇賦彆岆啃僅冪帠僅ㄛ蘇硉gcj02
        int span=1000;
        try {
            span = Integer.valueOf(frequence.getText().toString());
        } catch (Exception e) {
            // TODO: handle exception
        }
        option.setScanSpan(span);//扢离楷隅弇腔潔路奀潔峈5000ms
        option.setIsNeedAddress(checkGeoLocation.isChecked());
        mLocationClient.setLocOption(option);
    }


    /**
     * 妗珋妗弇隙覃潼泭
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation){
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\ndirection : ");
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append(location.getDirection());
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //堍茠妀陓洘
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
            }
            logMsg(sb.toString());
            CurPOI=new LatLng(location.getLatitude(),location.getLongitude()) ;
            mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                    .location( CurPOI));
            LocationResult.setText(sb.toString());
            mLocationClient.stop();
//            Log.i("BaiduLocationApiDem", sb.toString());
        }


    }


    /**
     * 珆尨趼睫揹
     * @param str
     */
    public void logMsg(String str) {
        try {
            if (mLocationResult != null)
                mLocationResult.setText(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult result) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(this, "onGetGeoCodeResult抱歉", Toast.LENGTH_LONG)
                    .show();
            return;
        }

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
        LocationResult.append("\n"+result.getAddress());
        Toast.makeText(this, result.getAddress(),
                Toast.LENGTH_LONG).show();

    }
    /**
     * 詢儕僅華燴峓戲隙覃
     * @author jpren
     *
     */
}
