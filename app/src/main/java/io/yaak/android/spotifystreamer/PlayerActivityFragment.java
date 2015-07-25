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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;


public class PlayerActivityFragment extends Fragment {

    private final String LOG_TAG = PlayerActivityFragment.class.getSimpleName();
    private String mArtistName;
    private String mAlbumName;
    private String mTrackName;
    private String mAlbumImage;

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
        mArtistName = intent.getStringExtra(extraBaseStr + "ArtistName");
        mAlbumName = intent.getStringExtra(extraBaseStr + "AlbumName");
        mTrackName = intent.getStringExtra(extraBaseStr + "TrackName");
        mAlbumImage = intent.getStringExtra(extraBaseStr + "AlbumImage");
        updateView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(LOG_TAG, " in onCreateView");

        View rootView =  inflater.inflate(R.layout.fragment_player, container, false);
        TextView artisName = (TextView) rootView.findViewById(R.id.player_artist_name);
        TextView albumName = (TextView) rootView.findViewById(R.id.player_album_name);
        TextView trackName = (TextView) rootView.findViewById(R.id.player_track_name);
        ImageView albumImage = (ImageView) rootView.findViewById(R.id.player_album_image);

        artisName.setText(mArtistName);
        albumName.setText(mAlbumName);
        trackName.setText(mTrackName);
        Picasso.with(getActivity())
                .load(mAlbumImage)
                .placeholder(R.drawable.artist_placeholder)
                .resize(300, 300).centerCrop().into(albumImage);



        return rootView;
    }

}
