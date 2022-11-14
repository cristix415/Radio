package com.example.radiotest;

import static com.example.radiotest.ExampleService.mediaSession;
import static com.example.radiotest.MainActivity.button;
import static com.example.radiotest.MainActivity.mService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

public class MediaButtonBroadcastReceiver extends BroadcastReceiver {

    public MediaButtonBroadcastReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);


        mService.setButton(button);
     //   mService.pressedCancel = false;
        if (mService.player == null)
                button.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_pause));
            else button.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_play));

    }
}
