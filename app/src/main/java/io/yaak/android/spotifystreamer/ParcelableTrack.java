package io.yaak.android.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.AlbumSimple;
import kaaes.spotify.webapi.android.models.Track;

public class ParcelableTrack implements Parcelable {

    public String id;
    public String name;
    public Album album;

    public ParcelableTrack(Track track) {
        this.id = track.id;
        this.name = track.name;
        this.album = new Album(track.album);
    }

    public ParcelableTrack(Parcel source) {
        this.id = source.readString();
        this.name = source.readString();
        this.album = source.readParcelable(null);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeParcelable(album, 0);

    }

    static final Creator<ParcelableTrack> CREATOR = new Creator<ParcelableTrack>() {
        @Override
        public ParcelableTrack createFromParcel(Parcel source) {
            return new ParcelableTrack(source);
        }

        @Override
        public ParcelableTrack[] newArray(int size) {
            return new ParcelableTrack[size];
        }
    };

    static class Album implements Parcelable {

        public String id;
        public String name;
        public String image_url;

        public Album(AlbumSimple albumSimple) {
            this.id = albumSimple.id;
            this.name = albumSimple.name;
            if (albumSimple.images.size() > 0) {
                this.image_url = albumSimple.images.get(0).url;
            }
            else {
                this.image_url = null;
            }
        }

        public Album(Parcel source) {
            this.id = source.readString();
            this.name = source.readString();
            this.image_url = source.readString();
        }

        static final Creator<Album> CREATOR = new Creator<Album>() {
            @Override
            public Album createFromParcel(Parcel source) {
                return new Album(source);
            }

            @Override
            public Album[] newArray(int size) {
                return new Album[size];
            }
        };

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
    }
}
