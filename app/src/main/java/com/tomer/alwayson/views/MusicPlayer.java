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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tomer.alwayson.R;
import com.tomer.alwayson.helpers.Utils;

public class MusicPlayer extends LinearLayout implements View.OnClickListener {
    private Context context;
    private View layout;
    private AudioManager manager;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String artist = intent.getStringExtra("artist");
            String album = intent.getStringExtra("album");
            String track = intent.getStringExtra("track");
            Utils.logInfo("Music", artist + ":" + album + ":" + track);
            if (layout.findViewById(R.id.song_name_tv) != null) {
                try {
                    removeView(layout);
                } catch (IllegalStateException e) {
                    ((TextView) layout.findViewById(R.id.song_name_tv)).setText(artist + "-" + track);
                }
            }
        }
    };

    public MusicPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
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
        iF.addAction("com.android.music.metachanged");
        iF.addAction("com.htc.music.metachanged");
        iF.addAction("fm.last.android.metachanged");
        iF.addAction("com.sec.android.app.music.metachanged");
        iF.addAction("com.nullsoft.winamp.metachanged");
        iF.addAction("com.amazon.mp3.metachanged");
        iF.addAction("com.miui.player.metachanged");
        iF.addAction("com.real.IMP.metachanged");
        iF.addAction("com.sonyericsson.music.metachanged");
        iF.addAction("com.spotify.music.metachanged");
        iF.addAction("com.rdio.android.metachanged");
        iF.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
        iF.addAction("com.andrew.apollo.metachanged");
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
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        KeyEvent downEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keycode);
        am.dispatchMediaKeyEvent(downEvent);
        KeyEvent upEvent = new KeyEvent(KeyEvent.ACTION_UP, keycode);
        am.dispatchMediaKeyEvent(upEvent);
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