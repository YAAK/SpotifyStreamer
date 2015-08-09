package io.yaak.android.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class PlayerActivityFragment extends Fragment {

    private final String LOG_TAG = PlayerActivityFragment.class.getSimpleName();
    private String mArtistName;
    private ParcelableTrack mTrack;
    private Bundle mTrackBundle = new Bundle();
    private List<ParcelableTrack> mTopTracksList = new ArrayList<>();
    private int mPosition = 0;

    public PlayerActivityFragment() {
        // Required empty public constructor
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, " in onCreate");
        super.onCreate(savedInstanceState);
        Intent intent = getActivity().getIntent();
        String extraBaseStr = this.getClass().getPackage().toString();
        //mTrack = intent.getExtras().getParcelable(extraBaseStr + ".Track");
        mArtistName = intent.getStringExtra(extraBaseStr + ".ArtistName");
        mTrackBundle = intent.getBundleExtra(extraBaseStr + ".TrackBundle");
        mTopTracksList = mTrackBundle.getParcelableArrayList("topTracksList");
        mPosition = intent.getExtras().getInt(extraBaseStr + ".Position");
        mTrack = mTopTracksList.get(mPosition);
        updateView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(LOG_TAG, " in onCreateView");

        View rootView =  inflater.inflate(R.layout.fragment_player, container, false);
        TextView artistName = (TextView) rootView.findViewById(R.id.player_artist_name);
        TextView albumName = (TextView) rootView.findViewById(R.id.player_album_name);
        TextView trackName = (TextView) rootView.findViewById(R.id.player_track_name);
        ImageView albumImage = (ImageView) rootView.findViewById(R.id.player_album_image);

        artistName.setText(mArtistName);
        albumName.setText(mTrack.album.name);
        trackName.setText(mTrack.name);
        Picasso.with(getActivity())
                .load(mTrack.album.image_url)
                .placeholder(R.drawable.artist_placeholder)
                .resize(300, 300).centerCrop().into(albumImage);

        final ImageButton playBtn = (ImageButton) rootView.findViewById(R.id.btn_play);
        final ImageButton nextBtn = (ImageButton) rootView.findViewById(R.id.btn_next);
        final ImageButton prevBtn = (ImageButton) rootView.findViewById(R.id.btn_prev);

        final String extraBaseStr = this.getClass().getPackage().toString();

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
                mPosition += 1;
                mTrack = mTopTracksList.get(mPosition);
                Intent playIntent = new Intent(getActivity(), PlayerService.class)
                        .putExtra(extraBaseStr + ".Track", mTrack);
                playIntent.setAction(PlayerService.ACTION_PLAY);
                playBtn.setTag("Pause");
                playBtn.setImageResource(android.R.drawable.ic_media_pause);
                getActivity().startService(playIntent);
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPosition -= 1;
                mTrack = mTopTracksList.get(mPosition);
                Intent playIntent = new Intent(getActivity(), PlayerService.class)
                        .putExtra(extraBaseStr + ".Track", mTrack);
                playIntent.setAction(PlayerService.ACTION_PLAY);
                playBtn.setTag("Pause");
                playBtn.setImageResource(android.R.drawable.ic_media_pause);
                getActivity().startService(playIntent);
            }
        });

        return rootView;
    }



}
