package com.spotify.playit.audio;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.spotify.playit.R;

import java.util.ArrayList;

public class AudioActivity extends AppCompatActivity implements AudioRecyclerAdapter.SongInteractor, AudioInteractor{

    private static final int PERMISSION_REQUEST = 1;
    public static final String Broadcast_PLAY_NEW_AUDIO = "com.spotify.playit.PlayNewAudio";

    private MediaPlayerService player;
    boolean serviceBound = false;

    ArrayList<Audio> audioList;

    ProgressBar progressBar;

    RecyclerView recyclerView;

    SeekBar seekBar;
    FloatingActionButton fab;

    TextView permission_not_granted_tv, peek_song_name_tv, songNameTv, noSongsFoundTv;

    LinearLayout main_layout, bottomBar;

    ConstraintLayout llBottomSheet;

    RelativeLayout topLevelRel;

    BottomSheetBehavior bottomSheetBehavior;

    ImageButton peekPlayBtn, prevBtn, nextBtn;

    ImageView slideDownArrow;

    Handler mSeekbarUpdateHandler = new Handler();
    Runnable mUpdateSeekbar;

    Button playBtn;

    private int CURRENT_SONG_INDEX = 1;

    StorageUtil storage;

    NotificationManager notificationManager;

    AudioRecyclerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        storage = new StorageUtil(getApplicationContext());

        initViews();

        initSongNameTv();

        initPeekSongNameTv();

        initBottomSheet();

        initStartingUI();

        checkPermission();

        setupChannelForAndroidO();

        initPeekPlayBtn();

        initFab();

        initNextBtn();

        initPrevButton();

        initPlayButton();

    }

    private void initPlayButton() {
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player!=null){
                    if (player.getMediaIsPlaying()){
                        player.pauseMedia();
                    }else{
                        player.playMedia();
                        peek_song_name_tv.setText(storage.loadAudio().get(storage.loadAudioIndex()).getTitle());
                        songNameTv.setText(storage.loadAudio().get(storage.loadAudioIndex()).getTitle());
                        adapter.selectedPosition = storage.loadAudioIndex();
                        adapter.notifyDataSetChanged();
                    }
                }else{
                    int audioIndex = storage.loadAudioIndex();

                    if (audioIndex == -1){
                        audioIndex = 0;
                    }

                    playAudio(audioIndex);
                    playBtn.setText("Pause");

                    peek_song_name_tv.setText(storage.loadAudio().get(storage.loadAudioIndex()).getTitle());
                    songNameTv.setText(storage.loadAudio().get(storage.loadAudioIndex()).getTitle());
                    adapter.selectedPosition = storage.loadAudioIndex();
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void initPrevButton() {
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int audioIndex = storage.loadAudioIndex();

                if (audioIndex == 0) {
                    audioIndex = audioList.size() - 1;
                } else {
                    audioIndex -=1;
                }

                storage.storeAudioIndex(audioIndex);

                playAudio(storage.loadAudioIndex());

                peek_song_name_tv.setText(storage.loadAudio().get(storage.loadAudioIndex()).getTitle());
                songNameTv.setText(storage.loadAudio().get(storage.loadAudioIndex()).getTitle());
                adapter.selectedPosition = storage.loadAudioIndex();
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void initNextBtn() {
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNextSong();
            }
        });
    }

    private void playNextSong() {

        int audioIndex = storage.loadAudioIndex();

        if (audioIndex == audioList.size() - 1) {
            audioIndex = 0;
        } else {
            audioIndex +=1;
        }

        storage.storeAudioIndex(audioIndex);
        playAudio(storage.loadAudioIndex());
        peek_song_name_tv.setText(storage.loadAudio().get(storage.loadAudioIndex()).getTitle());
        songNameTv.setText(storage.loadAudio().get(storage.loadAudioIndex()).getTitle());
        adapter.selectedPosition = storage.loadAudioIndex();
        adapter.notifyDataSetChanged();
    }


    private void initFab() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player!=null){
                    if (player.getMediaIsPlaying()){
                        player.pauseMedia();
                    }else{
                        player.playMedia();
                    }
                }else{
                    int audioIndex = storage.loadAudioIndex();

                    if (audioIndex == -1){
                        audioIndex = 0;
                    }

                    playAudio(audioIndex);
                    playBtn.setText("Pause");

                    peek_song_name_tv.setText(storage.loadAudio().get(storage.loadAudioIndex()).getTitle());
                    songNameTv.setText(storage.loadAudio().get(storage.loadAudioIndex()).getTitle());
                    adapter.selectedPosition = storage.loadAudioIndex();
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void initPeekPlayBtn() {
        peekPlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player!=null){
                    if (player.getMediaIsPlaying()){
                        player.pauseMedia();
                    }else{
                        player.playMedia();
                    }
                }else{
                    int audioIndex = storage.loadAudioIndex();

                    if (audioIndex == -1){
                        audioIndex = 0;
                    }

                    playAudio(audioIndex);
                    playBtn.setText("Pause");

                    peek_song_name_tv.setText(storage.loadAudio().get(storage.loadAudioIndex()).getTitle());
                    songNameTv.setText(storage.loadAudio().get(storage.loadAudioIndex()).getTitle());
                    adapter.selectedPosition = storage.loadAudioIndex();
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void setupChannelForAndroidO() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    private void initStartingUI() {
        progressBar.setVisibility(View.VISIBLE);
        main_layout.setVisibility(View.GONE);
    }

    private void initBottomSheet() {
        bottomBar.setVisibility(View.VISIBLE);
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if (bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED){
                    initSongNameTv();
                    bottomBar.setVisibility(View.GONE);
                    topLevelRel.setVisibility(View.GONE);
                }else if (bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_COLLAPSED){
                    initPeekSongNameTv();
                    bottomBar.setVisibility(View.VISIBLE);
                    topLevelRel.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                if (v>=0.7){
                    if (bottomBar.getVisibility()==View.VISIBLE){
                        bottomBar.setVisibility(View.GONE);
                        topLevelRel.setVisibility(View.GONE);
                    }
                }
                if (v<=0.3){
                    if (bottomBar.getVisibility()==View.GONE){
                        bottomBar.setVisibility(View.VISIBLE);
                        topLevelRel.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        slideDownArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

    }

    private void initSongNameTv() {
        songNameTv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        songNameTv.setSingleLine();
        songNameTv.setMarqueeRepeatLimit(10);
        songNameTv.setFocusable(true);
        songNameTv.setHorizontallyScrolling(true);
        songNameTv.setFocusableInTouchMode(true);
        songNameTv.requestFocus();
    }

    private void initPeekSongNameTv(){
        peek_song_name_tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        peek_song_name_tv.setSingleLine();
        peek_song_name_tv.setMarqueeRepeatLimit(10);
        peek_song_name_tv.setFocusable(true);
        peek_song_name_tv.setHorizontallyScrolling(true);
        peek_song_name_tv.setFocusableInTouchMode(true);
        peek_song_name_tv.requestFocus();
    }

    private void initViews() {
        progressBar = findViewById(R.id.main_progress);
        recyclerView = findViewById(R.id.main_rv);
        main_layout = findViewById(R.id.main_layout);
        llBottomSheet = findViewById(R.id.bottom_sheet);
        bottomBar = findViewById(R.id.bottom_bar);
        peek_song_name_tv = findViewById(R.id.peek_song_name_tv);
        permission_not_granted_tv = findViewById(R.id.permission_not_granted_tv);
        noSongsFoundTv = findViewById(R.id.no_songs_found_tv);
        peekPlayBtn = findViewById(R.id.peek_song_play_btn);
        fab = findViewById(R.id.fab_btn);
        seekBar = findViewById(R.id.song_seek_bar);
        songNameTv = findViewById(R.id.song_text_tv);
        playBtn = findViewById(R.id.play_btn_playlist);
        prevBtn = findViewById(R.id.prev_song_btn);
        nextBtn = findViewById(R.id.next_song_btn);
        topLevelRel = findViewById(R.id.top_level_rel);
        slideDownArrow = findViewById(R.id.slideDownArrow);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(MediaPlayerService.CHANNEL_ID,
                    "spotify",NotificationManager.IMPORTANCE_LOW);

            notificationManager = getSystemService(NotificationManager.class);

            if (notificationManager != null){
                notificationManager.createNotificationChannel(channel1);
            }
        }
    }


    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(AudioActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(AudioActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(AudioActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
            }else{
                ActivityCompat.requestPermissions(AudioActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
            }
        }else{
            fetchSongs();
        }
    }

    private void fetchSongs() {
        loadAudio();

        progressBar.setVisibility(View.GONE);

        if (audioList!=null && audioList.size()!=0){
            adapter = new AudioRecyclerAdapter(AudioActivity.this,audioList, this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(AudioActivity.this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(layoutManager);
            main_layout.setVisibility(View.VISIBLE);
        }else{
            Toast.makeText(AudioActivity.this, "Unable to find songs on the device!",Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            llBottomSheet.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
            noSongsFoundTv.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUEST:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(AudioActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(AudioActivity.this,"Permission Granted", Toast.LENGTH_SHORT).show();
                        fetchSongs();
                        llBottomSheet.setVisibility(View.VISIBLE);
                    }
                }else{
                    Toast.makeText(AudioActivity.this,"Permission Not Granted",Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    permission_not_granted_tv.setVisibility(View.VISIBLE);
                    llBottomSheet.setVisibility(View.GONE);
                }
                return;
            }
        }
    }


    @Override
    public void onSongPlayed(Audio song, int position) {
        CURRENT_SONG_INDEX = position;
        playAudio(CURRENT_SONG_INDEX);

        peek_song_name_tv.setText(song.getTitle());

        songNameTv.setText(song.getTitle());

        seekBar.setProgress(0);

    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;

//            Toast.makeText(AudioActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();

            player.setAudioInteractor(AudioActivity.this);
            onPlayed();
            initSeekBar();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    private void initSeekBar() {
        Log.d("mediadebug","seekbar");

        seekBar.setMax(player.getMediaTotalDuration());

        Log.d("mediadebug","totalduration : "+player.getMediaTotalDuration());

        mUpdateSeekbar = new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(player.getMediaCurrentPosition());
                mSeekbarUpdateHandler.postDelayed(this, 50);
            }
        };

        mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int progressValue = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar1, int i, boolean b) {
                progressValue = i;
                if (b){
                    player.seekToMedia(i);
                    seekBar1.setProgress(i);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                player.seekToMedia(progressValue);
                seekBar.setProgress(progressValue);
            }
        });
    }

    private void playAudio(int audioIndex) {
        if (!serviceBound) {
            storage.storeAudio(audioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        } else {
            storage.storeAudioIndex(audioIndex);
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }

    }

    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                audioList.add(new Audio(data, title, album, artist));
            }
        }
        cursor.close();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean("ServiceState", serviceBound);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationManager.cancelAll();
        }

        if (serviceBound) {
            unbindService(serviceConnection);
            player.stopSelf();
        }
    }

    @Override
    public void onMediaPrepared(MediaPlayer mediaPlayer) {
        initSeekBar();
        if (mediaPlayer.isPlaying()){
            peekPlayBtn.setImageResource(R.drawable.ic_action_pause);
            fab.setImageResource(R.drawable.ic_action_pause_black);
            playBtn.setText("Pause");
        }else{
            peekPlayBtn.setImageResource(R.drawable.ic_action_play);
            fab.setImageResource(R.drawable.ic_action_play_black);
            playBtn.setText("Play");
        }
    }

    @Override
    public void onPlayed() {
        peekPlayBtn.setImageResource(R.drawable.ic_action_pause);
        fab.setImageResource(R.drawable.ic_action_pause_black);
        playBtn.setText("Pause");
    }

    @Override
    public void onPaused() {
        peekPlayBtn.setImageResource(R.drawable.ic_action_play);
        fab.setImageResource(R.drawable.ic_action_play_black);
        playBtn.setText("Play");
    }

    @Override
    public void onStopped() {
        peekPlayBtn.setImageResource(R.drawable.ic_action_play);
        fab.setImageResource(R.drawable.ic_action_play_black);
        playBtn.setText("Play");
    }

    @Override
    public void onSkipToNext() {
        adapter.selectedPosition = storage.loadAudioIndex();
        adapter.notifyDataSetChanged();
        songNameTv.setText(storage.loadAudio().get(storage.loadAudioIndex()).getTitle());
        peek_song_name_tv.setText(storage.loadAudio().get(storage.loadAudioIndex()).getTitle());
    }

    @Override
    public void onSkipToPrevious() {
        adapter.selectedPosition = storage.loadAudioIndex();
        adapter.notifyDataSetChanged();
        songNameTv.setText(storage.loadAudio().get(storage.loadAudioIndex()).getTitle());
        peek_song_name_tv.setText(storage.loadAudio().get(storage.loadAudioIndex()).getTitle());
    }

    @Override
    public void onPlayBackStatus(PlaybackStatus playbackStatus) {
        if (playbackStatus == PlaybackStatus.PLAYING){
            peekPlayBtn.setImageResource(R.drawable.ic_action_pause);
            fab.setImageResource(R.drawable.ic_action_pause_black);
            playBtn.setText("Pause");
        }else{
            peekPlayBtn.setImageResource(R.drawable.ic_action_play);
            fab.setImageResource(R.drawable.ic_action_play_black);
            playBtn.setText("Pause");
        }
    }

    @Override
    public void onBackPressed() {
        if (bottomSheetBehavior.getState()== BottomSheetBehavior.STATE_EXPANDED){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }else{
            //super.onBackPressed();
           // onPause();

            Intent setIntent = new Intent(Intent.ACTION_MAIN);
            setIntent.addCategory(Intent.CATEGORY_HOME);
            setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(setIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
       // checkPermission();
    }
}
