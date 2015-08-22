package io.yaak.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements MainActivityFragment.Callback {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    public static boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.top_tracks_fragment_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                TopTracksActivityFragment topTracksActivityFragment = new TopTracksActivityFragment();
                MainActivityFragment mainActivityFragment = new MainActivityFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.main_fragment_container, mainActivityFragment)
                        .add(R.id.top_tracks_fragment_container, topTracksActivityFragment)
                        .commit();
            }

        } else {
            mTwoPane = false;
            if (savedInstanceState == null) {
                MainActivityFragment mainActivityFragment = new MainActivityFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.main_fragment_container, mainActivityFragment)
                        .commit();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onArtistSelected(ParcelableArtist artist) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putString(this.getClass().getPackage().toString() + "ArtistId", artist.id);
            args.putString(this.getClass().getPackage().toString() + "ArtistName", artist.name);
            TopTracksActivityFragment fragment = new TopTracksActivityFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_tracks_fragment_container, fragment)
                    .commit();
        } else {
            Intent topSongsIntent = new Intent(this, TopTracksActivity.class)
                    .putExtra(this.getClass().getPackage().toString() + "ArtistId", artist.id)
                    .putExtra(this.getClass().getPackage().toString() + "ArtistName", artist.name);
            startActivity(topSongsIntent);
        }
    }

    @Override
    public void onSongSelected(List topTracksList, int position, String artistName, ParcelableTrack selectedTrack) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            Bundle tracksBundle = new Bundle();

            tracksBundle.putParcelableArrayList("topTracksList", (ArrayList) topTracksList);
            args.putParcelable(this.getClass().getPackage().toString() + ".Track", selectedTrack);
            args.putString(this.getClass().getPackage().toString() + ".ArtistName", artistName);
            args.putInt(this.getClass().getPackage().toString() + ".Position", position);
            args.putBundle(this.getClass().getPackage().toString() + ".TrackBundle", tracksBundle);

            FragmentManager fragmentManager = getSupportFragmentManager();
            PlayerActivityFragment player = new PlayerActivityFragment();
            player.setArguments(args);

            player.show(fragmentManager, "playerActivityFragment");

        } else {
            Bundle tracksBundle = new Bundle();
            tracksBundle.putParcelableArrayList("topTracksList",(ArrayList)topTracksList);
            Intent playerIntent = new Intent(this, PlayerActivity.class)
                    .putExtra(this.getClass().getPackage().toString() + ".Track", selectedTrack)
                    .putExtra(this.getClass().getPackage().toString() + ".ArtistName", artistName)
                    .putExtra(this.getClass().getPackage().toString() + ".Position", position)
                    .putExtra(this.getClass().getPackage().toString() + ".TrackBundle", tracksBundle);
            startActivity(playerIntent);
        }
    }


}
