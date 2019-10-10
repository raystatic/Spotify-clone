package com.spotify.cl;

public class Song {
    long id;
    String songName, artistName;

    public Song(long id, String songName, String artistName) {
        this.id = id;
        this.songName = songName;
        this.artistName = artistName;
    }
}
