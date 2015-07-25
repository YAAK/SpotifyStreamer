package io.yaak.android.spotifystreamer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


public class PlayerActivityFragment extends Fragment {


    private final String LOG_TAG = TopTracksActivityFragment.class.getSimpleName();

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
        updateView();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.v(LOG_TAG, " in onCreateView");

        View rootView =  inflater.inflate(R.layout.fragment_player, container, false);

        return rootView;
    }

}
