package io.yaak.android.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

public class PlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private final String LOG_TAG = PlayerService.class.getSimpleName();

    // Action names that describe tasks that this IntentService can perform
    public static final String ACTION_PLAY = "io.yaak.android.spotifystreamer.action.PLAY";
    public static final String ACTION_PAUSE = "io.yaak.android.spotifystreamer.action.PAUSE";
    public static final String ACTION_NEXT = "io.yaak.android.spotifystreamer.action.NEXT";
    public static final String ACTION_PREV = "io.yaak.android.spotifystreamer.action.PREV";

    private static final String EXTRA_TRACK = "io.yaak.android.spotifystreamer.Track";

    public static final int STATE_IDLE = 0;
    public static final int STATE_INITIALIZED = 1;
    public static final int STATE_PREPARING = 2;
    public static final int STATE_PREPARED = 3;
    public static final int STATE_STARTED = 4;
    public static final int STATE_PAUSED = 5;
    public static final int STATE_STOPPED = 6;
    public static final int STATE_PLAYBACK_COMPLETE = 7;
    public static final int STATE_ERROR = 8;
    public int CURRENT_STATE = 0;

    MediaPlayer mMediaPlayer = null;
    ParcelableTrack mTrack = null;
    private final IBinder mBinder = new LocalBinder();

    public PlayerService() {
        super();
    }


    private void handleActionPlay(ParcelableTrack track) {
        Log.i(LOG_TAG, " in handleActionPlay");

        try {
            if (CURRENT_STATE == STATE_PAUSED && mTrack!= null && mTrack.id.equals(track.id)) {
                mMediaPlayer.start();
                CURRENT_STATE = STATE_STARTED;
            } else if (CURRENT_STATE == STATE_STARTED && mTrack!= null && mTrack.id.equals(track.id)) {

            } else {
                    mTrack = new ParcelableTrack(track);
                    mMediaPlayer.reset();
                    mMediaPlayer.setDataSource(track.preview_url); // initialize it here
                    CURRENT_STATE = STATE_INITIALIZED;
                    mMediaPlayer.setOnPreparedListener(this);
                    mMediaPlayer.prepareAsync();
                    CURRENT_STATE = STATE_PREPARING;
            }
        } catch(IOException ex) {
            Log.e(LOG_TAG, ex.getMessage());
        }
    }

    private void handleActionPause(ParcelableTrack track) {
        Log.i(LOG_TAG, " in handleActionPause");
        switch (CURRENT_STATE) {
            case STATE_STARTED:
                mMediaPlayer.pause();
                CURRENT_STATE = STATE_PAUSED;
                break;
            case STATE_PAUSED:
                // Do nothing
                break;
            default:
                // Do nothing
                break;
        }
    }

    @Override
    public void onCreate() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                CURRENT_STATE = STATE_PLAYBACK_COMPLETE;
            }
        });
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        CURRENT_STATE = STATE_IDLE;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            String extraBaseStr = this.getClass().getPackage().toString();
            Log.i(LOG_TAG, "STATE: " + CURRENT_STATE);

            final String action = intent.getAction();
            if (ACTION_PLAY.equals(action)) {
                final ParcelableTrack track = intent.getExtras().getParcelable(extraBaseStr + ".Track");
                handleActionPlay(track);
            } else if (ACTION_PAUSE.equals(action)) {
                final ParcelableTrack track = intent.getExtras().getParcelable(extraBaseStr + ".Track");
                handleActionPause(track);
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        CURRENT_STATE = STATE_PREPARED;
        mMediaPlayer.start();
        CURRENT_STATE = STATE_STARTED;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        CURRENT_STATE = STATE_ERROR;
        return false;
    }

    public class LocalBinder extends Binder {
        PlayerService getService() {
            return PlayerService.this;
        }
    }

    public int getDuration() {
        if (CURRENT_STATE == STATE_PREPARED
                || CURRENT_STATE == STATE_STARTED
                || CURRENT_STATE == STATE_PAUSED
                || CURRENT_STATE == STATE_STOPPED
                || CURRENT_STATE == STATE_PLAYBACK_COMPLETE
                ) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    public int getCurrentPosition() {
        if (CURRENT_STATE != STATE_IDLE) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void setPosition(int position) {
        if (CURRENT_STATE != STATE_IDLE) {
            mMediaPlayer.seekTo(position);
        }
    }

    public int getState() {
        return CURRENT_STATE;
    }

}
