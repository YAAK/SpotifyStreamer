package io.yaak.android.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class TrackAdapter extends ArrayAdapter<ParcelableTrack>{

    private final String LOG_TAG = TrackAdapter.class.getSimpleName();

    public TrackAdapter(Context context, List<ParcelableTrack> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ParcelableTrack track = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.track, parent, false);
        }

        // Lookup view for data population
        TextView nameView = (TextView) convertView.findViewById(R.id.track_name);
        TextView albumNameView = (TextView) convertView.findViewById(R.id.track_album_name);
        ImageView photoView = (ImageView) convertView.findViewById(R.id.track_photo);
        // Populate the data into the template view using the data object
        nameView.setText(track.name);
        albumNameView.setText(track.album.name);
        Picasso.with(getContext())
                .load(track.album.image_url)
                .placeholder(R.drawable.artist_placeholder)
                .resize(50, 50).centerCrop().into(photoView);

        // Return the completed view to render on screen
        return convertView;
    }
}
