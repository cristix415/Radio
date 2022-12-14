package com.example.radiotest;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.RatingCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.radiotest.databinding.ActivityMainBinding;
import com.google.android.material.button.MaterialButton;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;
import org.w3c.dom.Attr;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {

    static ExampleService mService;
    boolean mBound = false;
    public static MaterialButton button;
    // Array of strings...
    ListView simpleList;
    String radioList[] = {"Radio ALT FM", "Radio Vocea Crestinilor"};
    String links[] = {"http://asculta.radiocnm.ro:8002/live", "https://listen.radioking.com/radio/494884/stream/551902"};

    BroadcastReceiver broadcastReceiverPlayerPrepared = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            button.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_pause));

        }
    };


    public void startServiciu(View v) {
        // SetButtonIconPlay();
        if ((button != null))
            button.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_play));
        //apasare pause
        if (button == v && mService.player != null) {
            Log.e("apasare pause SERVICE", button.getText().toString());
            button.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_play));
            mService.player.stop();
            mService.player.release();
            mService.player = null;
            mService.showNotification(R.drawable.ic_play);
        }
        // apasare play
        else {
        //    Log.e("apasare play SERVICE", button.getText().toString());
            button = (MaterialButton) v;
            button.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_loading_foreground));
            mService.setButton(button);

            if (mService.player != null) {
                mService.player.stop();
                mService.player.release();
                mService.player = null;
            }
            mService.PlayRadio(button.getTag().toString());
        }
    }


    private void cauta() {
        HttpUtils.get("current", null, new JsonHttpResponseHandler() {
            @Override
            public synchronized void onSuccess(int statusCode, Header[] headers, JSONObject responseString) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                //super.onFailure(statusCode, headers, responseString, throwable);
                Log.e("aaaa", responseString);
            }
        });


    }

    /*private void SetButtonIconPlay() {
        MaterialButton altFMButton = findViewById(R.id.RadioAltFm);
        altFMButton.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_play));
        MaterialButton voceaCrestinilorButton = findViewById(R.id.RadioVoceaCrestinilor);
        voceaCrestinilorButton.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_play));
    }*/
    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    public ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ExampleService.LocalBinder binder = (ExampleService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.radiotest");
        registerReceiver(broadcastReceiverPlayerPrepared, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiverPlayerPrepared);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent serviceIntent = new Intent(getApplicationContext(), ExampleService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        //   simpleList = (ListView)findViewById(R.id.simpleListView);
        //CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), radioList, flags);
        // simpleList.setAdapter(customAdapter);

        for (int i = 0; i < 2; i++) {
            button = new MaterialButton(this, null);
            button.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_play));
            button.setText(radioList[i]);
            button.setTag(links[i]);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startServiciu(v);
                }
            });

            binding.getRoot().addView(button);
        }
        button = null;


        // startForegroundService(serviceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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