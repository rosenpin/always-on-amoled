package com.tomer.alwayson.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tomer.alwayson.R;
import com.tomer.alwayson.helpers.Utils;

public class MusicPlayer extends LinearLayout implements View.OnClickListener {
    private Context context;
    private Intent mediaButtonsIntent;
    private View layout;
    private AudioManager manager;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!manager.isMusicActive())
                removeView(layout);
            else if (!layout.isAttachedToWindow())
                addView(layout);
            String artist = intent.getStringExtra("artist");
            String album = intent.getStringExtra("album");
            String track = intent.getStringExtra("track");
            Utils.logInfo("Music", artist + ":" + album + ":" + track);
            if (findViewById(R.id.song_name_tv) != null && (artist != null || album != null || track != null)) {
                ((TextView) findViewById(R.id.song_name_tv)).setText(artist + "-" + track);
            }
        }
    };

    public MusicPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        mediaButtonsIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layout = inflater.inflate(R.layout.music_widget, null);
        addView(layout);
        findViewById(R.id.skip_prev).setOnClickListener(this);
        findViewById(R.id.play).setOnClickListener(this);
        findViewById(R.id.skip_next).setOnClickListener(this);

        IntentFilter iF = new IntentFilter();
        iF.addAction("com.android.music.metachanged");
        iF.addAction("com.android.music.playstatechanged");
        iF.addAction("com.android.music.playbackcomplete");
        iF.addAction("com.android.music.queuechanged");

        context.registerReceiver(mReceiver, iF);
        manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (!manager.isMusicActive())
            removeView(layout);
    }

    public void play() {
        sendButton(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
    }

    public void skipNext() {
        sendButton(KeyEvent.KEYCODE_MEDIA_NEXT);
    }

    public void skipPrevious() {
        sendButton(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
    }

    public void sendButton(int keycode) {
        synchronized (this) {
            mediaButtonsIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keycode));
            context.sendOrderedBroadcast(mediaButtonsIntent, null);
            mediaButtonsIntent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, keycode));
            context.sendOrderedBroadcast(mediaButtonsIntent, null);
        }
    }

    @Override
    public void onClick(View view) {
        Utils.logDebug(MusicPlayer.class.getSimpleName(), "Clicked " + view.getId());
        switch (view.getId()) {
            case R.id.skip_prev:
                skipPrevious();
                break;
            case R.id.play:
                play();
                break;
            case R.id.skip_next:
                skipNext();
                break;
        }
    }

    public void destroy() {
        try {
            context.unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException ignored) {
        }
    }
}