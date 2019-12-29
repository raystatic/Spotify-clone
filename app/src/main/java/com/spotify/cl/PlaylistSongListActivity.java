package com.spotify.cl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import com.spotify.cl.services.OnClearFromRecentService;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class PlaylistSongListActivity extends AppCompatActivity implements RecyclerMainAdpter.SongInteractor, Playable {

    private static final int PERMISSION_REQUEST = 1;

    ArrayList<Song> songArrayList;

    ProgressBar progressBar;

    RecyclerView recyclerView;

    Boolean songIsPlaying = false;
    Boolean songCompleted = false;
    MediaPlayer mediaPlayer;

    int songPosition, totalDuration;

    SeekBar seekBar;
    FloatingActionButton fab;

    TextView permission_not_granted_tv, peek_song_name_tv, songNameTv;

    LinearLayout main_layout, llBottomSheet, bottomBar;

    RelativeLayout topLevelRel;

    BottomSheetBehavior bottomSheetBehavior;

    ImageButton peekPlayBtn, prevBtn, nextBtn;

    Button playBtn;
    Boolean PLAY_PLAYLIST = false;
    private int CURRENT_SONG_INDEX = 1;

    ImageView slideDownArrow;

    NotificationManager notificationManager;

    int position = 0;
    boolean isPlaying = false;

    Handler mSeekbarUpdateHandler = new Handler();
    Runnable mUpdateSeekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_song_list);

        songArrayList = new ArrayList<>();

        progressBar = findViewById(R.id.main_progress);
        recyclerView = findViewById(R.id.main_rv);
        main_layout = findViewById(R.id.main_layout);
        llBottomSheet = findViewById(R.id.bottom_sheet);
        bottomBar = findViewById(R.id.bottom_bar);
        peek_song_name_tv = findViewById(R.id.peek_song_name_tv);
        permission_not_granted_tv = findViewById(R.id.permission_not_granted_tv);
        peekPlayBtn = findViewById(R.id.peek_song_play_btn);
        fab = findViewById(R.id.fab_btn);
        seekBar = findViewById(R.id.song_seek_bar);
        songNameTv = findViewById(R.id.song_text_tv);
        playBtn = findViewById(R.id.play_btn_playlist);
        prevBtn = findViewById(R.id.prev_song_btn);
        nextBtn = findViewById(R.id.next_song_btn);
        topLevelRel = findViewById(R.id.top_level_rel);
        slideDownArrow = findViewById(R.id.slideDownArrow);

        PLAY_PLAYLIST = false;

        songNameTv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        songNameTv.setSingleLine();
        songNameTv.setMarqueeRepeatLimit(10);
        songNameTv.setFocusable(true);
        songNameTv.setHorizontallyScrolling(true);
        songNameTv.setFocusableInTouchMode(true);
        songNameTv.requestFocus();

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
                    bottomBar.setVisibility(View.GONE);
                    topLevelRel.setVisibility(View.GONE);
                }else if (bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_COLLAPSED){
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

        progressBar.setVisibility(View.VISIBLE);
        main_layout.setVisibility(View.GONE);

        checkPermission();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel();
            registerReceiver(broadcastReceiver, new IntentFilter("SONGS_SONGS"));
            startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
        }

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PLAY_PLAYLIST = true;
                CURRENT_SONG_INDEX = 1;
                onSongClicked(songArrayList.get(CURRENT_SONG_INDEX), CURRENT_SONG_INDEX);
                CURRENT_SONG_INDEX += 1;

                if (isPlaying){
                    onTrackPause();
                }else{
                    onTrackPlay();
                }

            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTrackNext();
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTrackPrevious();
            }
        });

    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(CreateNotification.CHANNEL_ID,
                    "spotify",NotificationManager.IMPORTANCE_LOW);

            notificationManager = getSystemService(NotificationManager.class);

            if (notificationManager != null){
                notificationManager.createNotificationChannel(channel1);
            }
        }
    }

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(PlaylistSongListActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(PlaylistSongListActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(PlaylistSongListActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
            }else{
                ActivityCompat.requestPermissions(PlaylistSongListActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
            }
        }else{
            fetchSongs();
        }
    }

    public void getMusic(){
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri,null,null, null, null);

        if (songCursor != null && songCursor.moveToFirst()){

            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songID = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);

            do{
                long id = songCursor.getLong(songID);
                String currentTitle = songCursor.getString(songTitle);
                String currentArtist = songCursor.getString(songArtist);
                Song song = new Song(id,currentTitle,currentArtist);
                songArrayList.add(song);
            }while (songCursor.moveToNext());

        }

    }


    private void fetchSongs() {
        getMusic();

        progressBar.setVisibility(View.GONE);
        main_layout.setVisibility(View.VISIBLE);

        if (songArrayList.size()!=0){
            RecyclerMainAdpter adapter = new RecyclerMainAdpter(PlaylistSongListActivity.this,songArrayList, this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(PlaylistSongListActivity.this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(layoutManager);
        }else{
            Toast.makeText(PlaylistSongListActivity.this, "Unable to find songs on the device!",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUEST:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(PlaylistSongListActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(PlaylistSongListActivity.this,"Permission Granted", Toast.LENGTH_SHORT).show();
                        fetchSongs();
                    }
                }else{
                    Toast.makeText(PlaylistSongListActivity.this,"Permission Not Granted",Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    permission_not_granted_tv.setVisibility(View.VISIBLE);
                }
                return;
            }
        }
    }

    @Override
    public void onSongClicked(final Song song, final int position) {

        CURRENT_SONG_INDEX = position;

        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,song.getId());

        if (mediaPlayer!=null){
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try{
            mediaPlayer.setDataSource(PlaylistSongListActivity.this,trackUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            songIsPlaying = true;
            peekPlayBtn.setImageResource(R.drawable.ic_action_pause);
            fab.setImageResource(R.drawable.ic_action_pause_black);
            seekBar.setProgress(0);

            CreateNotification.createNotification(PlaylistSongListActivity.this,song,R.drawable.ic_action_pause_black,
                    1,songArrayList.size()-1);

            if (song.getSongName().length()>30){
                peek_song_name_tv.setText(song.getSongName().substring(0,30)+"...");
            }else{
                peek_song_name_tv.setText(song.getSongName());
            }
            songNameTv.setText(song.getSongName());

            peekPlayBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (songIsPlaying){
                        mediaPlayer.pause();
                        songIsPlaying = false;
                        songCompleted = false;
                        peekPlayBtn.setImageResource(R.drawable.ic_action_play);
                        fab.setImageResource(R.drawable.ic_action_play_black);
                    }else{
                        if (songCompleted){
                            onSongClicked(song,position);
                        }else{
                            mediaPlayer.start();
                        }
                        songIsPlaying = true;
                        peekPlayBtn.setImageResource(R.drawable.ic_action_pause);
                        fab.setImageResource(R.drawable.ic_action_pause_black);
                    }
                }
            });

            totalDuration = mediaPlayer.getDuration();

            seekBar.setMax(totalDuration);

            mUpdateSeekbar = new Runnable() {
                @Override
                public void run() {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    mSeekbarUpdateHandler.postDelayed(this, 50);
                }
            };

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (songIsPlaying){
                        onTrackPause();

                    }else{
                        onTrackPlay();
                    }
                }
            });


            mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                int progressValue = 0;

                @Override
                public void onProgressChanged(SeekBar seekBar1, int i, boolean b) {
                    progressValue = i;
                    if (i==totalDuration){
                        mediaPlayer.stop();
                        songIsPlaying = false;
                        songCompleted = true;
                        if (PLAY_PLAYLIST){
                            if (CURRENT_SONG_INDEX >1 && CURRENT_SONG_INDEX <songArrayList.size()){
                                onSongClicked(songArrayList.get(CURRENT_SONG_INDEX),CURRENT_SONG_INDEX);
                                CURRENT_SONG_INDEX = CURRENT_SONG_INDEX + 1;
                            }
                            if (CURRENT_SONG_INDEX ==songArrayList.size()){
                                CURRENT_SONG_INDEX = 0;
                            }
                        }
                        peekPlayBtn.setImageResource(R.drawable.ic_action_play);
                        fab.setImageResource(R.drawable.ic_action_play_black);
                    }

                    if (b){
                        mediaPlayer.seekTo(i);
                        seekBar1.setProgress(i);
                    }

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mediaPlayer.seekTo(progressValue);
                    seekBar.setProgress(progressValue);
                    songPosition = progressValue;
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");

            switch (action){
                case CreateNotification.ACTION_PREVIOUS :
                    onTrackPrevious();
                    break;
                case CreateNotification.ACTION_PLAY :
                    if (songIsPlaying){
                        onTrackPause();
                    }else{
                        onTrackPlay();
                    }
                    break;
                case CreateNotification.ACTION_NEXT :
                    onTrackNext();
                    break;

            }

        }
    };

    @Override
    public void onTrackPrevious() {
        PLAY_PLAYLIST = true;
        CURRENT_SONG_INDEX -=1;
        if (CURRENT_SONG_INDEX>=0){
            onSongClicked(songArrayList.get(CURRENT_SONG_INDEX),CURRENT_SONG_INDEX);
        }else{
            CURRENT_SONG_INDEX = songArrayList.size()-1;
            onSongClicked(songArrayList.get(CURRENT_SONG_INDEX),CURRENT_SONG_INDEX);
        }
        CreateNotification.createNotification(PlaylistSongListActivity.this,songArrayList.get(CURRENT_SONG_INDEX),R.drawable.ic_action_pause_black,
                CURRENT_SONG_INDEX,songArrayList.size()-1);
    }

    @Override
    public void onTrackPlay() {

        if (songCompleted){
            onSongClicked(songArrayList.get(CURRENT_SONG_INDEX),position);
        }else{
            mediaPlayer.start();
            mSeekbarUpdateHandler.postDelayed(mUpdateSeekbar, 0);
        }
        songIsPlaying = true;
        peekPlayBtn.setImageResource(R.drawable.ic_action_pause);
        fab.setImageResource(R.drawable.ic_action_pause_black);

        CreateNotification.createNotification(PlaylistSongListActivity.this,songArrayList.get(CURRENT_SONG_INDEX),R.drawable.ic_action_pause_black,
                CURRENT_SONG_INDEX,songArrayList.size()-1);

    }

    @Override
    public void onTrackPause() {

        mediaPlayer.pause();
        mSeekbarUpdateHandler.removeCallbacks(mUpdateSeekbar);
        songIsPlaying = false;
        songCompleted = false;
        peekPlayBtn.setImageResource(R.drawable.ic_action_play);
        fab.setImageResource(R.drawable.ic_action_play_black);

        CreateNotification.createNotification(PlaylistSongListActivity.this,songArrayList.get(CURRENT_SONG_INDEX),R.drawable.ic_action_play_black,
                CURRENT_SONG_INDEX,songArrayList.size()-1);
    }

    @Override
    public void onTrackNext() {
        PLAY_PLAYLIST = true;
        CURRENT_SONG_INDEX +=1;
        if (CURRENT_SONG_INDEX<songArrayList.size()){
            onSongClicked(songArrayList.get(CURRENT_SONG_INDEX),CURRENT_SONG_INDEX);
        }else{
            CURRENT_SONG_INDEX = 0;
            onSongClicked(songArrayList.get(CURRENT_SONG_INDEX),CURRENT_SONG_INDEX);
        }
        CreateNotification.createNotification(PlaylistSongListActivity.this,songArrayList.get(CURRENT_SONG_INDEX),R.drawable.ic_action_pause_black,
                CURRENT_SONG_INDEX,songArrayList.size()-1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            notificationManager.cancelAll();
        }

        unregisterReceiver(broadcastReceiver);

    }
}
