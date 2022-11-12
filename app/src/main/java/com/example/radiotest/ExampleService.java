package com.example.radiotest;

import static com.example.radiotest.MainActivity.stopPressed;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.BitmapKt;
import androidx.media.session.MediaButtonReceiver;

import com.google.android.material.button.MaterialButton;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

import cz.msebera.android.httpclient.Header;

public class ExampleService extends Service implements MediaPlayer.OnPreparedListener {
    private final IBinder binder = new LocalBinder();
    private static final String ACTION_PLAY = "com.example.action.PLAY";
    MaterialButton button;
    String artist;
    String title;
    MediaPlayer player = new MediaPlayer();
    WifiManager.WifiLock wifiLock;

    private NotificationCompat.Builder notificationBuilder;
    public static MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private NotificationManager notificationManager;
    public static boolean pressedCancel = false;

    private void CreateMediaSession() {
        mediaSession = new MediaSessionCompat(this, "simpleplayer session");
        Log.e("STOP", "create media session");
        stateBuilder = new PlaybackStateCompat.Builder()
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
                playPauseRadio();
                //    cauta();
                pressedCancel = false;
                ;
                //showNotification(false);
            }

            @Override
            public void onPause() {
                super.onPause();
                playPauseRadio();
                pressedCancel = false;
                synchronized (artist) {
                    //cauta();
                }
                ;
                //showNotification(false);

            }

            @Override
            public void onStop() {
                super.onStop();
                player.stop();
                pressedCancel = true;
                stopForeground(Service.STOP_FOREGROUND_REMOVE);

                //  notificationManager.cancelAll();
                Log.e("canceeeeeeeeel", String.valueOf(pressedCancel));


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
        super.onDestroy();
        Log.e("onDestroy", " ");
        if (player != null)
            player.release();
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        wifiLock.release();
        Log.e("onTaskRemoved", " ");
        stopSelf();
    }




    /**
     * method for clients
     */
    public boolean isPlaying() {
        return !player.isPlaying();
    }
    public void playPauseRadio() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            player.start();
        }
        showNotification(false);
    }
    public void setButton(MaterialButton button) {
        this.button = button;
    }

    private void cauta() {
        if (button != null && button.getId() == R.id.RadioVoceaCrestinilor)
            HttpUtils.get("current", null, new JsonHttpResponseHandler() {
                @Override
                public synchronized void onSuccess(int statusCode, Header[] headers, JSONObject responseString) {
                    //super.onSuccess(statusCode, headers, responseString);
                    try {
                        artist = responseString.getString("artist");
                        title = responseString.getString("title");
                        //   showNotification(false);

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
        if (button != null && button.getId() == R.id.RadioAltFm) {
            artist = "RADIO ALTFM";
            title = "";
        }
        if (button == null) {
            artist = "RADIO ";
            title = "";
        }


    }

    public void showNotification(boolean firstRun) {
        //  cauta();
       // Log.e("notificare", "incepuuuuut notificare");
        notificationBuilder = new NotificationCompat.Builder(this, "exampleServiceChannel");
        int icon;

        if (firstRun)
            icon = R.drawable.ic_pause;
        else {
            if (!isPlaying())
                icon = R.drawable.ic_pause;
            else
                icon = R.drawable.ic_play;
        }

        //MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent);
        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(icon, "playPause",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_PLAY_PAUSE));
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


        notificationManager = (NotificationManager) getSystemService((NOTIFICATION_SERVICE));
        Notification notif = notificationBuilder.build();
        notificationManager.notify(158, notif);
        startForeground(158, notif);
       // Log.e("notificare", "incepuuuuut notificare");

    }


    public void Apasare(String link) {
        player.release();
        player = null;

        Log.e("APASARE METODA", " ");
        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        wifiLock.acquire();

            player = new MediaPlayer();

        // player = MediaPlayer.create(this, Uri.parse(link));
        try {
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
        Log.e("dupa Prepare Async", " ");
        Intent intent1 = new Intent();
        intent1.setAction("com.example.radiotest");
        intent1.putExtra("stop", true);
        sendBroadcast(intent1);
        playPauseRadio();
    }

    public class LocalBinder extends Binder {
        ExampleService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ExampleService.this;
        }
    }


}
