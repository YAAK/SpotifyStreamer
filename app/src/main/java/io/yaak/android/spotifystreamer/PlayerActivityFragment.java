package io.yaak.android.spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


public class PlayerActivityFragment extends Fragment {

    private final String LOG_TAG = PlayerActivityFragment.class.getSimpleName();
    private String mArtistName;
    private ParcelableTrack mTrack;
    private List<ParcelableTrack> mTopTracksList = new ArrayList<>();
    private int mPosition = 0;
    PlayerService mService;
    boolean mBound = false;
    SeekBar mSeekBar = null;

    public PlayerActivityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, " in onCreate");
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {

        }

        //(new Thread(new seekBarUpdater())).start();
        updateView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(LOG_TAG, " in onCreateView");
        final String extraBaseStr = this.getClass().getPackage().toString();

        // Read the intent and extras
        Intent intent = getActivity().getIntent();

        mArtistName = intent.getStringExtra(extraBaseStr + ".ArtistName");
        final Bundle mTrackBundle = intent.getBundleExtra(extraBaseStr + ".TrackBundle");
        mPosition = intent.getExtras().getInt(extraBaseStr + ".Position");

        // Which track?
        mTopTracksList = mTrackBundle.getParcelableArrayList("topTracksList");
        mTrack = mTopTracksList.get(mPosition);

        // Bind to PlayerService
        Intent bindIntent = new Intent(getActivity(), PlayerService.class);
        getActivity().bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);

        // Finding views
        View rootView =  inflater.inflate(R.layout.fragment_player, container, false);
        final TextView artistName = (TextView) rootView.findViewById(R.id.player_artist_name);
        final TextView albumName = (TextView) rootView.findViewById(R.id.player_album_name);
        final TextView trackName = (TextView) rootView.findViewById(R.id.player_track_name);
        final ImageView albumImage = (ImageView) rootView.findViewById(R.id.player_album_image);
        final ImageButton playBtn = (ImageButton) rootView.findViewById(R.id.btn_play);
        final ImageButton nextBtn = (ImageButton) rootView.findViewById(R.id.btn_next);
        final ImageButton prevBtn = (ImageButton) rootView.findViewById(R.id.btn_prev);

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
        super.onDestroyView();
        // TODO Stop PlayerService
        //

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

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            // We've bound to LocalService, cast the IBinder and get LocalService instance
            PlayerService.LocalBinder binder = (PlayerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            final Timer seekBarProgressTimer = new Timer("seekBarProgressTimer");
            seekBarProgressTimer.schedule(new seekBarProgressUpdater(), 0, 300);

            Log.v(LOG_TAG, "Service Connected");

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private class seekBarProgressUpdater extends TimerTask {
        private final String LOG_TAG = "seekBarProgressUpdater";
        @Override
        public void run() {
            if (mSeekBar != null && mBound) {
                int dur = mService.getDuration();
                mSeekBar.setMax(dur);
                int prog = mService.getCurrentPosition();
                mSeekBar.setProgress(prog);
                //Log.v(LOG_TAG, "Updating SeekBar Progress " + prog);
            }

        }
    }
}
