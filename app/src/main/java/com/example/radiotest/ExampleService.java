package com.example.radiotest;

import android.animation.Animator;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import com.google.android.material.button.MaterialButton;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

import cz.msebera.android.httpclient.Header;

public class ExampleService extends Service {
    private final IBinder binder = new LocalBinder();

    MaterialButton button;
    String artist;
    String title;
    MediaPlayer player = new MediaPlayer();
    boolean playing;

    private NotificationCompat.Builder notificationBuilder;
    public static MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder stateBuilder;
    private NotificationManager notificationManager;


    @Override
    public IBinder onBind(Intent intent) {
        String link = intent.getStringExtra("link");


        CreateAndPlayMediaPlayer(link);

        CreateMediaSession();
        showNotification(true);
        //  player.start();
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        player.stop();
    }


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
                synchronized (artist) {
                    cauta();
                }
                ;
                showNotification(false);
            }

            @Override
            public void onPause() {
                super.onPause();
                playPauseRadio();
                synchronized (artist) {
                    cauta();
                }
                ;
                showNotification(false);

            }

            @Override
            public void onStop() {
                super.onStop();
                player.stop();

                stopForeground(Service.STOP_FOREGROUND_REMOVE);

                //  notificationManager.cancelAll();
                Log.e("test", "caaaaanceeeeeeeeeel");

            }
        });
        mediaSession.setActive(true);

    }


    /**
     * method for clients
     */
    public boolean isPlaying() {
        return !player.isPlaying();
    }

    public MaterialButton getButton() {

        return button;
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
                        showNotification(false);

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
        cauta();

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
        NotificationCompat.Action stopAction = new NotificationCompat.Action(R.drawable.ic_stop, "stop",
                MediaButtonReceiver.buildMediaButtonPendingIntent(this, PlaybackStateCompat.ACTION_STOP));

        Intent gg = new Intent(this, MainActivity.class);

        gg.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this,
                2, gg, PendingIntent.FLAG_IMMUTABLE);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.mipmap.munte);
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

                        .setShowActionsInCompactView(0));

        notificationManager = (NotificationManager) getSystemService((NOTIFICATION_SERVICE));
        Notification notif = notificationBuilder.build();
        notificationManager.notify(158, notif);
        startForeground(158, notif);
        Log.e("notificare", "notificareeeeeeeeeeeeee");

    }


    public void playPauseRadio() {
        if (player.isPlaying()) {
            player.pause();
        } else {
            try {
               player.start();
            } catch ( Exception e) {
                try {
                    player.prepare();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }


    }


    void CreateAndPlayMediaPlayer(String link) {
      //  player.stop();
        player.release();
        player = MediaPlayer.create(this, Uri.parse(link));
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (player.getDuration() != -1) {
                    Log.e("notttt", "loading");
                } else {
                    Log.e("notttt", "complete");
                    player.start();
                    showNotification(false);
                }
            }
        });


    }

    public class LocalBinder extends Binder {
        ExampleService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ExampleService.this;
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }
}
