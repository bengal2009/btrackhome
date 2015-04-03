package com.blin.btrack;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class SetShare extends ActionBarActivity {
    private SharedPreferences settings;
    private static final String data = "DATA";
    private static final String UserID = "ID";
    private static final String FirstTag = "FirstTag";
    private static final String SecondTag = "SecondTag";
    private TextView myid,firsttag,secondtag;
    PushApplication app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_share);
        myid = (TextView) findViewById(R.id.MYID);
        firsttag = (TextView) findViewById(R.id.TagTxt);
        secondtag = (TextView) findViewById(R.id.SecondTag);
        app=PushApplication.getInstance();
        if(app!=null){
        myid.setText(app.getUserId());
        }
        ReadPref();
    }

//    Safe Preference
    public void SavePref(View V)
{

    settings = getSharedPreferences(data,0);
    settings.edit()
            .putString(UserID, myid.getText().toString())
            .putString(FirstTag, firsttag.getText().toString())
            .putString(SecondTag, secondtag.getText().toString())
            .commit();
    finish();
}
    public void ReadPref(View V)
    {
        settings = getSharedPreferences(data,0);
        if(settings.getString(FirstTag, "")!=null)
            firsttag.setText(settings.getString(FirstTag, ""));
        if(settings.getString(SecondTag, "")!=null)
            secondtag.setText(settings.getString(SecondTag, ""));
//        Log.i("Setshare",settings.getString(UserID, "") );
    }
    public void ReadPref()
    {
        settings = getSharedPreferences(data,0);
        if(settings.getString(FirstTag, "")!=null)
            firsttag.setText(settings.getString(FirstTag, ""));
        if(settings.getString(SecondTag, "")!=null)
            secondtag.setText(settings.getString(SecondTag, ""));
//        Log.i("Setshare",settings.getString(UserID, "") );
    }
   /* public void readData(){
        settings = getSharedPreferences(data,0);
        name.setText(settings.getString(nameField, ""));
        phone.setText(settings.getString(phoneField, ""));
        sex.setText(settings.getString(sexField, ""));
    }
    public void saveData(){
        settings = getSharedPreferences(data,0);
        settings.edit()
                .putString(nameField, name.getText().toString())
                .putString(phoneField, phone.getText().toString())
                .putString(sexField, sex.getText().toString())
                .commit();
    }*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_share, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
