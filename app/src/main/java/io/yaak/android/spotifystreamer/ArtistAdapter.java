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

public class ArtistAdapter extends ArrayAdapter<ParcelableArtist>{

    private final String LOG_TAG = ArtistAdapter.class.getSimpleName();

    public ArtistAdapter(Context context, List<ParcelableArtist> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ParcelableArtist artist = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.artist, parent, false);
        }

        // Lookup view for data population
        TextView nameView = (TextView) convertView.findViewById(R.id.artist_name);
        ImageView photoView = (ImageView) convertView.findViewById(R.id.artist_photo);
        // Populate the data into the template view using the data object
        nameView.setText(artist.name);

        Picasso.with(getContext())
                .load(artist.image_url)
                .placeholder(R.drawable.artist_placeholder)
                .resize(50, 50).centerCrop().into(photoView);

        // Return the completed view to render on screen
        return convertView;
    }
}
