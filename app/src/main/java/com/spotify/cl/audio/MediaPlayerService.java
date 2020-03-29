package com.spotify.cl.audio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.spotify.cl.R;

import java.util.ArrayList;

public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer mediaPlayer;
    private String mediaFile;

    private int resumePosition;

    private final IBinder iBinder = new LocalBinder();

    private AudioManager audioManager;

    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    private ArrayList<Audio> audioList;
    private int audioIndex = -1;
    private Audio activeAudio; //an object of the currently playing audio

    public static final String CHANNEL_ID = "channel1";

    public static final String ACTION_PLAY = "com.spotify.cl.audio.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.spotify.cl.audio.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.spotify.cl.audio.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.spotify.cl.audio.ACTION_NEXT";
    public static final String ACTION_STOP = "com.spotify.cl.audio.ACTION_STOP";


    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    private static final int NOTIFICATION_ID = 101;

    AudioInteractor audioInteractor;

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        callStateListener();
        registerBecomingNoisyReceiver();
        register_playNewAudio();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try{

            StorageUtil storage = new StorageUtil(getApplicationContext());
            audioList = storage.loadAudio();
            audioIndex = storage.loadAudioIndex();

            Log.d("mediadebug","ontstartcommand index : "+audioIndex);

            if (audioIndex != -1 && audioIndex < audioList.size()) {
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }

        }catch (NullPointerException e){
            stopSelf();
        }

        if (!requestAudioFocus()){
            stopSelf();
        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                initMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }

        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void initMediaPlayer(){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);

        mediaPlayer.reset();

        try{
            mediaPlayer.setDataSource(activeAudio.getData());
            mediaPlayer.prepareAsync();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void playMedia(){
        if (!mediaPlayer.isPlaying()){
            mediaPlayer.start();
            if (audioInteractor!=null)
                audioInteractor.onPlayed();
            buildNotification(PlaybackStatus.PLAYING);
        }
    }

    public void stopMedia(){
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            if (audioInteractor!=null)
                audioInteractor.onStopped();
        }
    }

    public void pauseMedia(){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            buildNotification(PlaybackStatus.PAUSED);
            resumePosition = mediaPlayer.getCurrentPosition();
            if (audioInteractor!=null)
                audioInteractor.onPaused();
        }
    }

    public void seekToMedia(int i){
        if (mediaPlayer!=null)
            mediaPlayer.seekTo(i);
    }

    public int getMediaTotalDuration(){
        return mediaPlayer.getDuration();
    }

    public boolean isMediaCompleted(){
        return mediaPlayer.getCurrentPosition()>=mediaPlayer.getDuration();
    }

    public void resumeMedia(){
        if (!mediaPlayer.isPlaying()){
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
            buildNotification(PlaybackStatus.PLAYING);
        }
    }

    public boolean getMediaIsPlaying(){
        if (mediaPlayer != null)
            return mediaPlayer.isPlaying();
        return false;
    }

    public int getMediaCurrentPosition(){
        if (mediaPlayer!=null)
            return mediaPlayer.getCurrentPosition();
        else
            return 0;
    }


    private boolean requestAudioFocus(){
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = 0;
        if (audioManager != null) {
            result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
        if (result==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            return true;
        }

        return false;
    }

    private boolean removeAudioFocus(){
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.abandonAudioFocus(this);
    }


    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
     }

    @Override
    public void onCompletion(MediaPlayer mp) {
        skipToNext();
        if (audioInteractor!=null)
            audioInteractor.onSkipToNext();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {

        switch (what){
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error","MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error","MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }

        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {

        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playMedia();
        buildNotification(PlaybackStatus.PLAYING);

        Log.d("mediadebug","onprepared called");
        Log.d("mediadebug","audiointeractor"+audioInteractor);
        if (audioInteractor!=null){
            audioInteractor.onMediaPrepared(mp);
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {

        switch (focusChange){
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mediaPlayer==null) initMediaPlayer();
                else if (!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    buildNotification(PlaybackStatus.PLAYING);
                }
                mediaPlayer.setVolume(1.0f,1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    buildNotification(PlaybackStatus.PAUSED);
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f,0.1f);
                break;
        }

    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaFile!=null){
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();


        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);

        new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
    }


    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    private void callStateListener() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            buildNotification(PlaybackStatus.PAUSED);
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }


    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();

            if (audioIndex != -1 && audioIndex < audioList.size()) {
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }

            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };

    private void register_playNewAudio() {
        IntentFilter filter = new IntentFilter(AudioActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }


    private void initMediaSession() throws RemoteException{
        if (mediaSessionManager!=null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        updateMetaData();

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
                if (audioInteractor!=null){
                    audioInteractor.onPlayed();
                    audioInteractor.onPlayBackStatus(PlaybackStatus.PLAYING);
                }

            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);
                if (audioInteractor!=null) {
                    audioInteractor.onPaused();
                    audioInteractor.onPlayBackStatus(PlaybackStatus.PAUSED);
                }
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
                if (audioInteractor!=null){
                    audioInteractor.onSkipToNext();
                    audioInteractor.onPlayBackStatus(PlaybackStatus.PLAYING);
                }
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
                if (audioInteractor!=null){
                    audioInteractor.onSkipToPrevious();
                    audioInteractor.onPlayBackStatus(PlaybackStatus.PLAYING);
                }
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                if (audioInteractor!=null){
                    audioInteractor.onStopped();
                    audioInteractor.onPlayBackStatus(PlaybackStatus.PAUSED);
                }

                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });

    }

    private void updateMetaData() {
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_action_song); //replace with medias albumArt
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getTitle())
                .build());
    }

    public void skipToNext() {

        if (audioIndex == audioList.size() - 1) {
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        } else {
            activeAudio = audioList.get(++audioIndex);
        }

        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();

        mediaPlayer.reset();
        initMediaPlayer();
    }

    public void skipToPrevious() {

        if (audioIndex == 0) {
            audioIndex = audioList.size() - 1;
            activeAudio = audioList.get(audioIndex);
        } else {
            activeAudio = audioList.get(--audioIndex);
        }

        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();

        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void buildNotification(PlaybackStatus playbackStatus) {

        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.spotify); //replace with your own image

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setShowWhen(false)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                .setOngoing(true)
                .setContentText(activeAudio.getTitle())
                .setContentTitle(activeAudio.getAlbum())
                .setContentInfo(activeAudio.getArtist())
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

        if (playbackStatus == PlaybackStatus.PAUSED)
            builder.setOngoing(false);

        Notification notification = builder.build();

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);
    }


    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }

    public void setAudioInteractor(AudioInteractor audioInteractor){
        this.audioInteractor =audioInteractor;
    }


    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }


}
