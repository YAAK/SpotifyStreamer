package io.yaak.android.spotifystreamer;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class PlayerActivityFragment extends DialogFragment {

    private final String LOG_TAG = PlayerActivityFragment.class.getSimpleName();
    private String mArtistName;
    private ParcelableTrack mTrack;
    private List<ParcelableTrack> mTopTracksList = new ArrayList<>();
    private int mPosition = 0;
    PlayerService mService;
    boolean mBound = false;
    SeekBar mSeekBar = null;
    ImageButton playBtn = null;
    TextView mTimeRemaining = null;
    TextView mTimePassed = null;
    String extraBaseStr;

    public PlayerActivityFragment() {
        // Required empty public constructor
        extraBaseStr = this.getClass().getPackage().toString();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, " in onCreate");
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mArtistName = savedInstanceState.getString("mArtistName");
            mPosition = savedInstanceState.getInt("mPosition");
            mTrack = savedInstanceState.getParcelable("mTrack");
            mTopTracksList = savedInstanceState.getParcelableArrayList("mTopTracksList");


        } else {
            Bundle args = getArguments();

            mArtistName = args.getString(this.getClass().getPackage().toString() + ".ArtistName");
            final Bundle mTrackBundle = args.getBundle(this.getClass().getPackage().toString() + ".TrackBundle");
            mPosition = args.getInt(this.getClass().getPackage().toString() + ".Position");

            Log.v(LOG_TAG, "Artist Name" + mArtistName);

            // Which track?
            mTopTracksList = mTrackBundle.getParcelableArrayList("topTracksList");
            mTrack = mTopTracksList.get(mPosition);
        }

        updateView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(LOG_TAG, " in onCreateView");

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        // Bind to PlayerService
        Intent bindIntent = new Intent(getActivity(), PlayerService.class);
        getActivity().bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);

        // Finding views
        View rootView =  inflater.inflate(R.layout.fragment_player, container, false);
        final TextView artistName = (TextView) rootView.findViewById(R.id.player_artist_name);
        final TextView albumName = (TextView) rootView.findViewById(R.id.player_album_name);
        final TextView trackName = (TextView) rootView.findViewById(R.id.player_track_name);
        final ImageView albumImage = (ImageView) rootView.findViewById(R.id.player_album_image);
        playBtn = (ImageButton) rootView.findViewById(R.id.btn_play);
        final ImageButton nextBtn = (ImageButton) rootView.findViewById(R.id.btn_next);
        final ImageButton prevBtn = (ImageButton) rootView.findViewById(R.id.btn_prev);
        mTimePassed = (TextView) rootView.findViewById(R.id.time_passed);
        mTimeRemaining = (TextView) rootView.findViewById(R.id.time_remaining);

        // Filling in the view
        artistName.setText(mArtistName);
        albumName.setText(mTrack.album.name);
        trackName.setText(mTrack.name);
        Picasso.with(getActivity())
                .load(mTrack.album.image_url)
                .placeholder(R.drawable.artist_placeholder)
                .resize(300, 300).centerCrop().into(albumImage);

        // Start Playing the track
        Intent playIntent = new Intent(getActivity(), PlayerService.class)
                .putExtra(extraBaseStr + ".Track", mTrack);

        if (playBtn.getTag() == null || playBtn.getTag().toString().equals("play")) {
            playIntent.setAction(PlayerService.ACTION_PLAY);
            playBtn.setTag("Pause");
            ((ImageView) playBtn).setImageResource(android.R.drawable.ic_media_pause);
        } else if(playBtn.getTag().toString().equals("Pause")) {
            playIntent.setAction(PlayerService.ACTION_PAUSE);
            playBtn.setTag("Play");
            ((ImageView) playBtn).setImageResource(android.R.drawable.ic_media_play);
        }
        getActivity().startService(playIntent);

        // Define view buttons onClick
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent playIntent = new Intent(getActivity(), PlayerService.class)
                        .putExtra(extraBaseStr + ".Track", mTrack);
                if(v.getTag() == null || v.getTag().toString().equals("Play")) {
                    playIntent.setAction(PlayerService.ACTION_PLAY);
                    v.setTag("Pause");
                    ((ImageView) v).setImageResource(android.R.drawable.ic_media_pause);

                } else if(v.getTag().toString().equals("Pause")) {
                    playIntent.setAction(PlayerService.ACTION_PAUSE);
                    v.setTag("Play");
                    ((ImageView) v).setImageResource(android.R.drawable.ic_media_play);
                }
                getActivity().startService(playIntent);
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPosition < mTopTracksList.size() - 1) {
                    mPosition += 1;
                    mTrack = mTopTracksList.get(mPosition);

                    albumName.setText(mTrack.album.name);
                    trackName.setText(mTrack.name);
                    Picasso.with(getActivity())
                            .load(mTrack.album.image_url)
                            .placeholder(R.drawable.artist_placeholder)
                            .resize(300, 300).centerCrop().into(albumImage);

                    Intent playIntent = new Intent(getActivity(), PlayerService.class)
                            .putExtra(extraBaseStr + ".Track", mTrack);
                    playIntent.setAction(PlayerService.ACTION_PLAY);
                    playBtn.setTag("Pause");
                    playBtn.setImageResource(android.R.drawable.ic_media_pause);
                    getActivity().startService(playIntent);
                } else {
                    Toast toast = Toast.makeText(getActivity(), R.string.no_next_track, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPosition > 0) {
                    mPosition -= 1;
                    mTrack = mTopTracksList.get(mPosition);

                    albumName.setText(mTrack.album.name);
                    trackName.setText(mTrack.name);
                    Picasso.with(getActivity())
                            .load(mTrack.album.image_url)
                            .placeholder(R.drawable.artist_placeholder)
                            .resize(300, 300).centerCrop().into(albumImage);

                    Intent playIntent = new Intent(getActivity(), PlayerService.class)
                            .putExtra(extraBaseStr + ".Track", mTrack);
                    playIntent.setAction(PlayerService.ACTION_PLAY);
                    playBtn.setTag("Pause");
                    playBtn.setImageResource(android.R.drawable.ic_media_pause);
                    getActivity().startService(playIntent);
                } else {
                    Toast toast = Toast.makeText(getActivity(), R.string.no_prev_track, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });

        mSeekBar = (SeekBar) rootView.findViewById(R.id.player_seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mService != null && mBound && fromUser) {
                    mService.setPosition(progress);
                    seekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return rootView;
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            // From : http://stackoverflow.com/a/9848730/1204714
            getDialog().setOnDismissListener(null);
        }
        if (mBound) {
            getActivity().unbindService(mConnection);
        }
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i(LOG_TAG, " in onSaveInstanceState");
        outState.putParcelableArrayList("topTrackList", new ArrayList(mTopTracksList));
        outState.putParcelable("track", mTrack);
        outState.putInt("position", mPosition);
        outState.putString("artist", mArtistName);
        super.onSaveInstanceState(outState);
    }


    public void updateView() {
        Context context = getActivity();
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {

        } else {
            Toast toast = Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        private final String LOG_TAG = "ServiceConnection";
        private Timer seekBarProgressTimer = new Timer("seekBarProgressTimer");

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            seekBarProgressTimer.schedule(new seekBarProgressUpdater(), 0, 300);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            seekBarProgressTimer.cancel();
            seekBarProgressTimer = null;
        }
    };

    private class seekBarProgressUpdater extends TimerTask {
        private final String LOG_TAG = "seekBarProgressUpdater";
        @Override
        public void run() {
            Activity activity = getActivity();
            if (mSeekBar != null && mBound && mService.getState() == mService.STATE_STARTED) {
                final int dur = mService.getDuration();
                final int prog = mService.getCurrentPosition();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSeekBar.setMax(dur);
                            mSeekBar.setProgress(prog);
                            int remain = (int) Math.round((dur - prog) / 1000.0);
                            int passed = (int) Math.round(prog / 1000.0);
                            String remainTime = String.format("%02d:%02d", remain / 60, remain % 60);
                            String passedTime = String.format("%02d:%02d", passed / 60, passed % 60);
                            if (remain < 1000 && remain != 0) {
                                mTimeRemaining.setText(remainTime);
                                mTimePassed.setText(passedTime);
                            } else {
                                mTimeRemaining.setText("");
                                mTimePassed.setText("");
                            }
                        }
                    });
                }
            }
            if (mSeekBar != null && mBound && mService.getState() == mService.STATE_PLAYBACK_COMPLETE) {
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final int dur = mService.getDuration();
                            mSeekBar.setProgress(0);
                            mTimePassed.setText("00:00");
                            int duration = (int) Math.round(dur / 1000);
                            mTimeRemaining.setText(String.format("%02d:%02d", duration / 60, duration % 60));
                            playBtn.setTag("Play");
                            ((ImageView) playBtn).setImageResource(android.R.drawable.ic_media_play);
                        }
                    });
                }

            }

        }
    }
}
