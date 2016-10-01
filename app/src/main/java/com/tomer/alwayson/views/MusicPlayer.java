package com.tomer.alwayson.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.v7.widget.AppCompatImageView;
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
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String artist = intent.getStringExtra("artist");
            String album = intent.getStringExtra("album");
            String track = intent.getStringExtra("track");
            Utils.logInfo("Music", artist + ":" + album + ":" + track);
            if (layout.findViewById(R.id.song_name_tv) != null)
                ((TextView) layout.findViewById(R.id.song_name_tv)).setText(track);
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
        iF.addAction("com.htc.music.metachanged");
        iF.addAction("fm.last.android.metachanged");
        iF.addAction("com.sec.android.app.music.metachanged");
        iF.addAction("com.nullsoft.winamp.metachanged");
        iF.addAction("com.amazon.mp3.metachanged");
        iF.addAction("com.miui.player.metachanged");
        iF.addAction("com.real.IMP.metachanged");
        iF.addAction("com.sonyericsson.music.metachanged");
        iF.addAction("com.rdio.android.metachanged");
        iF.addAction("com.samsung.sec.android.MusicPlayer.metachanged");
        iF.addAction("com.andrew.apollo.metachanged");

        context.registerReceiver(mReceiver, iF);
        try {
            AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (!manager.isMusicActive())
                removeView(layout);
            else
                updatePlayPauseButton(true);
        } catch (UnsupportedOperationException e) {
            Utils.logInfo(MusicPlayer.class.getSimpleName(), "Can't connect to music service");
            removeView(layout);
        }
    }

    private void play() {
        sendButton(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
    }

    private void skipNext() {
        sendButton(KeyEvent.KEYCODE_MEDIA_NEXT);
    }

    private void skipPrevious() {
        sendButton(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
    }

    private void sendButton(int keycode) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        KeyEvent downEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keycode);
        am.dispatchMediaKeyEvent(downEvent);
        KeyEvent upEvent = new KeyEvent(KeyEvent.ACTION_UP, keycode);
        am.dispatchMediaKeyEvent(upEvent);
    }

    private void updatePlayPauseButton(boolean reverse) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (!am.isMusicActive())
            ((AppCompatImageView) findViewById(R.id.play)).setImageResource(reverse ? R.drawable.ic_play : R.drawable.ic_pause);
        else
            ((AppCompatImageView) findViewById(R.id.play)).setImageResource(reverse ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    @Override
    public boolean isShown() {
        return findViewById(R.id.song_name_tv) != null && findViewById(R.id.play) != null && findViewById(R.id.skip_next) != null && findViewById(R.id.skip_prev) != null;
    }

    @Override
    public void onClick(View view) {
        Utils.logDebug(MusicPlayer.class.getSimpleName(), "Clicked " + view.getId());
        switch (view.getId()) {
            case R.id.skip_prev:
                skipPrevious();
                break;
            case R.id.play:
                updatePlayPauseButton(false);
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