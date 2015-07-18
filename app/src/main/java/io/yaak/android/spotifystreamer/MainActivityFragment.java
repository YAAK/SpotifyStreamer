package io.yaak.android.spotifystreamer;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;

public class MainActivityFragment extends Fragment {

    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    static final String STATE_QUERY = "searchQuery";
    protected static ArtistAdapter artistAdapter;
    protected List<ParcelableArtist> artistsList = new ArrayList<>();
    private String searchQuery = null;

    public MainActivityFragment() {
    }

    public void updateView() {
        Context context = getActivity();
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if (isConnected) {
            artistAdapter.clear();
            if (artistsList.size() > 0) {
                for (ParcelableArtist artist : artistsList) {
                    artistAdapter.add(artist);
                }
            }
            else
            {
                Toast toast = Toast.makeText(context, R.string.no_artist_found, Toast.LENGTH_SHORT);
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

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("artistsList")) {
                artistsList = savedInstanceState.getParcelableArrayList("artistsList");
                updateView();
            } else if (savedInstanceState.containsKey("searchQuery")) {
                searchQuery = savedInstanceState.getString("searchQuery");
                (new SearchArtistsTask()).execute(searchQuery);
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(LOG_TAG, " in onCreateView");

        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);
        ListView artistsListView = (ListView) rootView.findViewById(R.id.listview_artists);
        artistAdapter = new ArtistAdapter(this.getActivity(), new ArrayList<>(artistsList));
        artistsListView.setAdapter(artistAdapter);

        artistsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParcelableArtist selectedArtist = artistAdapter.getItem(position);
                Intent topSongsIntent = new Intent(getActivity(), TopTracksActivity.class)
                        .putExtra(getActivity().getClass().getPackage().toString() + "ArtistId", selectedArtist.id)
                        .putExtra(getActivity().getClass().getPackage().toString() + "ArtistName", selectedArtist.name);
                startActivity(topSongsIntent);
            }
        });

        final EditText searchBox = (EditText) rootView.findViewById(R.id.search_query);
        if (searchQuery != null) {
            searchBox.setText(searchQuery);
        }

        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    //sendMessage();
                    searchBox.clearFocus();
                    searchArtist(v.getText().toString());
                    handled = true;
                }
                return handled;
            }
        });

        return rootView;
    }

    public void searchArtist(String q) {
        (new SearchArtistsTask()).execute(q.toString());
    }

    public void onSaveInstanceState(Bundle outState) {
        Log.i(LOG_TAG, " in onSaveInstanceState");
        outState.putString(STATE_QUERY, searchQuery);
        outState.putParcelableArrayList("artistsList", (ArrayList) artistsList);
        super.onSaveInstanceState(outState);
    }

    public class SearchArtistsTask extends AsyncTask<String, Void, List<ParcelableArtist>> {

        private final String LOG_TAG = MainActivityFragment.class.getSimpleName();

        public SearchArtistsTask(){}

        @Override
        protected List<ParcelableArtist> doInBackground(String... params) {

            if (params.length < 1) {
                return null;
            }
            if (params[0].length() == 0) {
                return null;
            }

            SpotifyApi api = new SpotifyApi();
            final SpotifyService spotify = api.getService();

            ArtistsPager artistsPager = null;
            try {
                artistsPager = spotify.searchArtists(params[0]);
            } catch (RetrofitError error) {
                SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                Log.e(LOG_TAG, error.getMessage());
            }

            ArrayList<ParcelableArtist> out = new ArrayList<>();
            if (artistsPager != null) {
                for (Artist artist : artistsPager.artists.items) {
                    out.add(new ParcelableArtist(artist));
                }
            }

            return out;

        }

        @Override
        protected void onPostExecute(List<ParcelableArtist> results) {
            artistsList = new ArrayList<>(results);
            updateView();
        }
    }
}
