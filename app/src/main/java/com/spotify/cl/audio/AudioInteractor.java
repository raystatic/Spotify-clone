package com.spotify.cl.audio;

public interface AudioInteractor {
    void onMediaPrepared();
    void onPlayed();
    void onPaused();
    void onStopped();
    void onSkipToNext();
    void onSkipToPrevious();
    void onPlayBackStatus(PlaybackStatus playbackStatus);
}
