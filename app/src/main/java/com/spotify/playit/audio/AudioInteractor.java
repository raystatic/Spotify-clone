package com.spotify.playit.audio;

import android.media.MediaPlayer;

public interface AudioInteractor {
    void onMediaPrepared(MediaPlayer mediaPlayer);
    void onPlayed();
    void onPaused();
    void onStopped();
    void onSkipToNext();
    void onSkipToPrevious();
    void onPlayBackStatus(PlaybackStatus playbackStatus);
}
