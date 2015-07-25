package io.yaak.android.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;

public class TopTracksActivityFragment extends Fragment {

    private final String LOG_TAG = TopTracksActivityFragment.class.getSimpleName();
    protected static TrackAdapter trackAdapter;
    private List<ParcelableTrack> topTracksList = new ArrayList<>();
    private String mArtistId;
    private String mArtistName;

    public TopTracksActivityFragment() {
    }

    public void updateView() {
        Context context = getActivity();
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            trackAdapter.clear();
            if (topTracksList.size() > 0) {
                for (ParcelableTrack track : topTracksList) {
                    trackAdapter.add(track);
                }
            }
            else
            {
                Toast toast = Toast.makeText(context, R.string.no_top_tracks_found, Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(context, R.string.no_internet_connection, Toast.LENGTH_SHORT);
            toast.show();
        }


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, " in onCreate");
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null){
            Intent intent = getActivity().getIntent();
            mArtistId = intent.getStringExtra(this.getClass().getPackage().toString() + "ArtistId");
            mArtistName = intent.getStringExtra(this.getClass().getPackage().toString() + "ArtistName");
            ((ActionBarActivity) getActivity()).getSupportActionBar().setSubtitle(mArtistName);
            if (mArtistId != null) {
                (new TopTracksTask()).execute(mArtistId);
            }

        } else if (!savedInstanceState.containsKey("topTracksList")) {
            // What should I do?
        } else {
            topTracksList = savedInstanceState.getParcelableArrayList("topTracksList");
            mArtistId = savedInstanceState.getString("mArtistId");
            mArtistName = savedInstanceState.getString("mArtistName");
            updateView();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, " in onCreateView");

        View rootView =  inflater.inflate(R.layout.fragment_top_tracks, container, false);

        trackAdapter = new TrackAdapter(this.getActivity(), new ArrayList<>(topTracksList));

        ListView tracksListView = (ListView) rootView.findViewById(R.id.listview_tracks);
        tracksListView.setAdapter(trackAdapter);

        tracksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParcelableTrack selectedTrack = trackAdapter.getItem(position);
                String extraBaseStr = getActivity().getClass().getPackage().toString();
                Intent playerIntent = new Intent(getActivity(), PlayerActivity.class)
                        .putExtra(extraBaseStr + "ArtistName", mArtistName)
                        .putExtra(extraBaseStr + "AlbumName", selectedTrack.album.name)
                        .putExtra(extraBaseStr + "TrackName", selectedTrack.name)
                        .putExtra(extraBaseStr + "AlbumImage", selectedTrack.album.image_url);
                startActivity(playerIntent);
            }
        });

        return rootView;
    }

    public void onSaveInstanceState(Bundle outState) {
        Log.i(LOG_TAG, " in onSaveInstanceState");
        outState.putParcelableArrayList("topTracksList", (ArrayList) topTracksList);
        outState.putString("mArtistId", mArtistId);
        outState.putString("mArtistName", mArtistName);
        super.onSaveInstanceState(outState);
    }

    public class TopTracksTask extends AsyncTask<String, Void, List<ParcelableTrack>> {
        private final String LOG_TAG = TopTracksActivityFragment.class.getSimpleName();

        @Override
        protected List<ParcelableTrack> doInBackground(String... params) {
            if (params.length < 1) {
                return null;
            }
            if (params[0] == null || params[0].length() == 0) {
                return null;
            }

            SpotifyApi api = new SpotifyApi();
            final SpotifyService spotify = api.getService();

            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("country", "CA");
            Tracks result = null;
            try {
                result = spotify.getArtistTopTrack(params[0], queryMap);
            } catch (RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                Log.e(LOG_TAG, error.getMessage());
            }

            ArrayList<ParcelableTrack> out = new ArrayList<>();
            if (result != null)
            {
                for (Track track : result.tracks) {
                    out.add(new ParcelableTrack(track));
                }
            }
            return out;
        }

        @Override
        protected void onPostExecute(List<ParcelableTrack> results) {
            topTracksList = new ArrayList<>(results);
            updateView();
        }
    }
}
