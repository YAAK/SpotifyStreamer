package io.yaak.android.spotifystreamer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
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

    private static final int STATE_IDLE = 0;
    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_PREPARING = 2;
    private static final int STATE_PREPARED = 3;
    private static final int STATE_STARTED = 4;
    private static final int STATE_PAUSED = 5;
    private static final int STATE_STOPPED = 6;
    private static final int STATE_PLAYBACK_COMPLETE = 7;
    private static int CURRENT_STATE = 0;

    // TODO: Rename parameters
    MediaPlayer mMediaPlayer = null;
    ParcelableTrack mTrack = null;

    public PlayerService() {
        super();
    }


    /**
     * Handle action Play in the provided background thread with the provided
     * parameters.
     */
    private void handleActionPlay(ParcelableTrack track) {
        Log.i(LOG_TAG, " in handleActionPlay");

        try {
            if (CURRENT_STATE == STATE_PAUSED && mTrack!= null && mTrack.id.equals(track.id)) {
                mMediaPlayer.start();
                CURRENT_STATE = STATE_STARTED;
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
        } finally {
            //Async(); // prepare async to not block main thread
            //handleActionPlay(param1, param2);
        }
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
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
        return null;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        CURRENT_STATE = STATE_PREPARED;
        mMediaPlayer.start();
        CURRENT_STATE = STATE_STARTED;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }
}
