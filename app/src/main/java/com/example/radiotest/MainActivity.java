package com.example.radiotest;

import static com.example.radiotest.ExampleService.mediaSession;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.KeyEventDispatcher;
import androidx.media.session.MediaButtonReceiver;


import android.os.IBinder;
import android.util.Log;
import android.view.View;


import com.example.radiotest.databinding.ActivityMainBinding;
import com.google.android.material.button.MaterialButton;
import com.loopj.android.http.JsonHttpResponseHandler;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {

    static ExampleService mService;
    boolean mBound = false;
    private ActivityMainBinding binding;
    public static MaterialButton prevButton;
    public static MaterialButton currentButton;
    Boolean nou = false;
    public static boolean stopPressed = false;
    BroadcastReceiver broadcastReceiverPlayerPrepared = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Boolean stop = intent.getBooleanExtra("stop",false);
            Log.e("tttttttttttttttttttt", String.valueOf(stop));
            if (stop)
                 nou = true;

            currentButton.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_pause));
        }
    };

    public void startService(View v) {
        SetButtonIconPlay();
        prevButton = currentButton;
        currentButton = (MaterialButton) v;
        if (stopPressed)
        Log.e("YES", "start stopPressed");
        else
            Log.e("NO", "start stopPressed");

        //click pe aceelasi post
        if (  !stopPressed &&  prevButton == currentButton) {
            Log.e("ACELASI radioul", currentButton.getText().toString());

            mService.playPauseRadio();

            if (mService.isPlaying())
                currentButton.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_play));
            else
                currentButton.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_pause));
        }

        //if (stopPressed || prevButton == null || (prevButton != null && prevButton != currentButton)) {
        else{
            nou = false;
            Log.e("PRIMA DATA sau DIFERIT", currentButton.getText().toString());
            currentButton.setIcon(ContextCompat.getDrawable(this,R.drawable.ic_loading_foreground));
            mService.setButton(currentButton);
            //mService.CreateAndPlayMediaPlayer(currentButton.getTag().toString());
            mService.Apasare(currentButton.getTag().toString());
        }

        Log.e("final metoda (buton apasat)", String.valueOf(stopPressed));


    }

    private void SetButtonIconPlay() {
        MaterialButton altFMButton = findViewById(R.id.RadioAltFm);
        altFMButton.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_play));
        MaterialButton voceaCrestinilorButton = findViewById(R.id.RadioVoceaCrestinilor);
        voceaCrestinilorButton.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_play));
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

    public void stopService(View v) {
        Intent serviceIntent = new Intent(this, ExampleService.class);
        //serviceIntent.putExtra("inputExtra", "stop");
        stopService(serviceIntent);
        v = findViewById(R.id.RadioAltFm);

        v.setBackgroundResource(R.drawable.ic_play);
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
        // unregisterReceiver(airplaneModeChangeReceiver);
        unregisterReceiver(broadcastReceiverPlayerPrepared);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent serviceIntent = new Intent(getApplicationContext(), ExampleService.class);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        //Context context = getApplicationContext();
        // Intent serviceIntent = new Intent(context, ExampleService.class);


        //  serviceIntent.putExtra("playing", "1");
        //  bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
        //Log.e("bu", button.getText().toString());
        // context.startForegroundService(serviceIntent);
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