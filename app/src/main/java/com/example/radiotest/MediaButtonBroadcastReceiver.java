package com.example.radiotest;

import static com.example.radiotest.ExampleService.mediaSession;
import static com.example.radiotest.ExampleService.pressedCancel;
import static com.example.radiotest.MainActivity.currentButton;
import static com.example.radiotest.MainActivity.mService;
import static com.example.radiotest.MainActivity.stopPressed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import com.google.android.material.button.MaterialButton;

import org.xml.sax.Parser;

public class MediaButtonBroadcastReceiver extends BroadcastReceiver {

    public MediaButtonBroadcastReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);

            Log.e(String.valueOf(pressedCancel),"stopPressed");

        mService.setButton(currentButton);
        stopPressed = pressedCancel;
     //   mService.pressedCancel = false;
        if (mService.isPlaying())
                currentButton.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_pause));
            else currentButton.setIcon(ContextCompat.getDrawable(context, R.drawable.ic_play));

    }
}
