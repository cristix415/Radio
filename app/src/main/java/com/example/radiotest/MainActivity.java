package com.example.radiotest;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.radiotest.databinding.ActivityMainBinding;
import com.google.android.material.button.MaterialButton;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {

    static ExampleService mService;
    boolean mBound = false;
    public static MaterialButton button;

    BroadcastReceiver broadcastReceiverPlayerPrepared = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            button.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_pause));
        }
    };


    public void startService(View v) {
        SetButtonIconPlay();
        //apasare pause
        if (button == v && mService.player != null) {
            button.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_play));
            mService.player.stop();
            mService.player.release();
            mService.player = null;
            mService.showNotification(R.drawable.ic_play);
        }
        // apasare play
        else {
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

    private void SetButtonIconPlay() {
        MaterialButton altFMButton = findViewById(R.id.RadioAltFm);
        altFMButton.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_play));
        MaterialButton voceaCrestinilorButton = findViewById(R.id.RadioVoceaCrestinilor);
        voceaCrestinilorButton.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_play));
    }
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
        com.example.radiotest.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent serviceIntent = new Intent(getApplicationContext(), ExampleService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
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