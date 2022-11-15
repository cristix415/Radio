package com.example.radiotest;
import static android.net.wifi.WifiManager.WIFI_MODE_FULL_HIGH_PERF;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Button;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;
import com.google.android.material.button.MaterialButton;
import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import cz.msebera.android.httpclient.Header;

public class ExampleService extends Service implements MediaPlayer.OnPreparedListener {
    private final IBinder binder = new LocalBinder();
    Button button;
    String artist;
    String title="";
    MediaPlayer player = new MediaPlayer();
    WifiManager.WifiLock wifiLock;

    public static MediaSessionCompat mediaSession;

    private void CreateMediaSession() {
        mediaSession = new MediaSessionCompat(this, "simpleplayer session");
        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_STOP
                );
        mediaSession.setMediaButtonReceiver(null);
        mediaSession.setPlaybackState(stateBuilder.build());
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                artist = button.getText().toString();
                showNotification(R.drawable.ic_loading_foreground);
        //        cauta();

                PlayRadio(button.getTag().toString());
            }

            @Override
            public void onPause() {
                super.onPause();
                player.stop();
                player.release();
                player = null;
                artist = button.getText().toString();
                title = "";
         //       cauta();
                showNotification(R.drawable.ic_play);
            }

            @Override
            public void onStop() {
                super.onStop();
                player.stop();
                stopForeground(Service.STOP_FOREGROUND_REMOVE);
            }
        });
        mediaSession.setActive(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        CreateMediaSession();
        return binder;
    }

    @Override
    public void onCreate() {


        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.e("destroy","destroy");
        super.onDestroy();
        if (player != null)
            player.release();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("removed","task removd");
        super.onTaskRemoved(rootIntent);
        wifiLock.release();
        stopSelf();
    }


    /**
     * method for clients
     */



    public void setButton(Button button) {
        artist = button.getText().toString();
        this.button = button;
    }

    private void cautaa() {
        if (button != null && button.getTag().toString() == "RadioVoceaCrestinilor")
            HttpUtils.get("current", null, new JsonHttpResponseHandler() {
                @Override
                public synchronized void onSuccess(int statusCode, Header[] headers, JSONObject responseString) {
                    //super.onSuccess(statusCode, headers, responseString);
                    try {
                        artist = responseString.getString("artist");
                        title = responseString.getString("title");
                        showNotification(R.drawable.ic_pause);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    //super.onFailure(statusCode, headers, responseString, throwable);
                    Log.e("aaaa", responseString);
                }
            });
        if (button == null) {
            artist = "RADIO ";
            title = "";
        }


    }

    public void showNotification(int icon) {

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "exampleServiceChannel");

        NotificationCompat.Action playPauseAction;
        if (player != null)
            playPauseAction = new NotificationCompat.Action(icon, "playPause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PAUSE));
        else
            playPauseAction = new NotificationCompat.Action(icon, "playPause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY));
        NotificationCompat.Action stopAction = new NotificationCompat.Action(R.drawable.ic_close, "stop",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP));

        Intent gg = new Intent(this, MainActivity.class);
        gg.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this,
                2, gg, PendingIntent.FLAG_IMMUTABLE);

        notificationBuilder
                .setContentTitle(title + " - " + artist)

                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(contentPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(playPauseAction)
                .addAction(stopAction)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()

                        .setMediaSession(mediaSession.getSessionToken())

                        .setShowCancelButton(true)
                        .setShowActionsInCompactView(0))

        //        .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
        ;


        NotificationManager notificationManager = (NotificationManager) getSystemService((NOTIFICATION_SERVICE));
        Notification notif = notificationBuilder.build();
        notificationManager.notify(158, notif);
        startForeground(158, notif);
    }


    public void PlayRadio(String link) {

        Log.e("APASARE METODA", " ");
        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WIFI_MODE_FULL_HIGH_PERF, "mylock");

        wifiLock.acquire();
        player = new MediaPlayer();

        // player = MediaPlayer.create(this, Uri.parse(link));
        try {
            Log.e("aaaa",link);
            player.setDataSource(this, Uri.parse(link));
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setOnPreparedListener(this);
        Log.e("Prepare Async", " ");
        Intent mainIntent = new Intent(this, MainActivity.class);
        // sendBroadcast(mainIntent);
        player.prepareAsync(); // prepare async to not block main thread

    }

    /**
     * Called when MediaPlayer is ready
     */
    public void onPrepared(MediaPlayer player) {
        Intent intent1 = new Intent();
        intent1.setAction("com.example.radiotest");
        sendBroadcast(intent1);
        player.start();
        showNotification(R.drawable.ic_pause);
    }

    public class LocalBinder extends Binder {
        ExampleService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ExampleService.this;
        }
    }


}
