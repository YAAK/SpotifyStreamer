package io.yaak.android.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;


public class PlayerActivity extends ActionBarActivity {
    private final String LOG_TAG = PlayerActivityFragment.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            DialogFragment playerActivityFragment = new PlayerActivityFragment();


            if(MainActivity.mTwoPane) {
                playerActivityFragment.show(fragmentManager,"playerActivityFragment");
            } else {
                Intent intent = getIntent();

                if (intent != null) {
                    Bundle args = new Bundle();

                    String artistName = intent.getStringExtra(this.getClass().getPackage().toString() + ".ArtistName");
                    final Bundle mTrackBundle = intent.getBundleExtra(this.getClass().getPackage().toString() + ".TrackBundle");
                    int position = intent.getExtras().getInt(this.getClass().getPackage().toString() + ".Position");
                    // Which track?
                    List topTracksList = mTrackBundle.getParcelableArrayList("topTracksList");
                    ParcelableTrack selectedTrack = (ParcelableTrack) topTracksList.get(position);

                    Bundle tracksBundle = new Bundle();
                    tracksBundle.putParcelableArrayList("topTracksList",(ArrayList)topTracksList);
                    args.putParcelable(this.getClass().getPackage().toString() + ".Track", selectedTrack);
                    args.putString(this.getClass().getPackage().toString() + ".ArtistName", artistName);
                    args.putInt(this.getClass().getPackage().toString() + ".Position", position);
                    args.putBundle(this.getClass().getPackage().toString() + ".TrackBundle", tracksBundle);

                    playerActivityFragment.setArguments(args);
                }

                setContentView(R.layout.activity_player);
                fragmentManager.beginTransaction()
                        .add(R.id.player_fragment_container, playerActivityFragment)
                        .commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_player, menu);
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
}
