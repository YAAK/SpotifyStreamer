package io.yaak.android.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Artist;

public class ParcelableArtist implements Parcelable {

    public String id;
    public String name;
    public String image_url;

    public ParcelableArtist(Artist artist) {
        this.id = artist.id;
        this.name = artist.name;
        if (artist.images.size() > 0) {
            this.image_url = artist.images.get(2).url;
        }
        else {
            this.image_url = null;
        }
    }

    public ParcelableArtist(Parcel source) {
        id = source.readString();
        name = source.readString();
        image_url = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(image_url);
    }

    static final Creator<ParcelableArtist> CREATOR = new Creator<ParcelableArtist>() {

        @Override
        public ParcelableArtist createFromParcel(Parcel source) {
            return new ParcelableArtist(source);
        }

        @Override
        public ParcelableArtist[] newArray(int size) {
            return new ParcelableArtist[size];
        }
    };
}
