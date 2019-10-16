package com.spotify.cl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RecyclerMainAdpter.SongInteractor {

    private static final int PERMISSION_REQUEST = 1;

    ArrayList<Song> songArrayList;

    ProgressBar progressBar;

    RecyclerView recyclerView;

    Boolean songIsPlaying = false;
    Boolean songCompleted = false;
    MediaPlayer mediaPlayer;

    FloatingActionButton fab;

    SeekBar seekBar;

    TextView songNameTv, permission_not_granted_tv;
//    TextView currentTimeTv, totalTimeTv;
    int songPosition;

    LinearLayout main_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startActivity(new Intent(MainActivity.this, PlaylistSongListActivity.class));
        finish();

        songArrayList = new ArrayList<>();

        progressBar = findViewById(R.id.main_progress);
        recyclerView = findViewById(R.id.main_rv);
        fab = findViewById(R.id.fab_btn);
        seekBar = findViewById(R.id.song_seek_bar);
        songNameTv = findViewById(R.id.song_text_tv);
        main_layout = findViewById(R.id.main_layout);
        permission_not_granted_tv = findViewById(R.id.permission_not_granted_tv);
//        currentTimeTv = findViewById(R.id.current_time_tv);
//        totalTimeTv = findViewById(R.id.total_time_tv);

        progressBar  = new ProgressBar(MainActivity.this);

        progressBar.setVisibility(View.VISIBLE);
        main_layout.setVisibility(View.GONE);

        checkPermission();

    }

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST);
            }else{
                ActivityCompat.requestPermissions(MainActivity.this,
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUEST:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(MainActivity.this,"Permission Granted", Toast.LENGTH_SHORT).show();
                        fetchSongs();
                    }
                }else{
                    Toast.makeText(MainActivity.this,"Permission Not Granted",Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    permission_not_granted_tv.setVisibility(View.VISIBLE);
                }
                return;
            }
        }
    }

    private void fetchSongs() {
        getMusic();

        progressBar.setVisibility(View.GONE);
        main_layout.setVisibility(View.VISIBLE);

        if (songArrayList.size()!=0){
            RecyclerMainAdpter adapter = new RecyclerMainAdpter(MainActivity.this,songArrayList, this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(layoutManager);
        }else{
            Toast.makeText(MainActivity.this, "Unable to find songs on the device!",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSongClicked(final Song song, final int position) {

        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,song.getId());

        if (mediaPlayer!=null){
            mediaPlayer.release();
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try{
            mediaPlayer.setDataSource(MainActivity.this,trackUri);
            mediaPlayer.prepare();
            mediaPlayer.start();
            songIsPlaying = true;
            fab.setImageResource(R.drawable.ic_action_pause);
            seekBar.setProgress(0);
//            String totalTime = convertToMinutesAndSeconds(mediaPlayer.getDuration());
//            totalTimeTv.setText(totalTime);
            if (song.getSongName().length()>15){
                songNameTv.setText(song.getSongName().substring(0,15)+"...");
            }else{
                songNameTv.setText(song.getSongName());
            }

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (songIsPlaying){
                        mediaPlayer.pause();
                        songIsPlaying = false;
                        songCompleted = false;
                        fab.setImageResource(R.drawable.ic_action_play);
                    }else{
                        if (songCompleted){
                            onSongClicked(song,position);
                        }else{
                            mediaPlayer.start();
                        }
                        songIsPlaying = true;
                        fab.setImageResource(R.drawable.ic_action_pause);
                    }
                }
            });


            seekBar.setMax(mediaPlayer.getDuration());

            new Thread(){
                public void run(){
                    songPosition=0;
                    while (songPosition < mediaPlayer.getDuration()){
                        try{
                            Thread.sleep(1000);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                        if (songIsPlaying){
                            songPosition+=1000;
                        }
//                        if (!convertToMinutesAndSeconds(songPosition).equals(convertToMinutesAndSeconds(mediaPlayer.getDuration()))){
//
//                        }
//                        Log.d("duration",songPosition+" : "+mediaPlayer.getDuration());
  //                      final String currentTime = convertToMinutesAndSeconds(songPosition);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                seekBar.setProgress(songPosition);
  //                              currentTimeTv.setText(currentTime);
                            }
                        });
                    }
                }
            }.start();

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                int progressValue = 0;

                @Override
                public void onProgressChanged(SeekBar seekBar1, int i, boolean b) {
                    progressValue = i;
                    if (i==mediaPlayer.getDuration()){
                        mediaPlayer.stop();
                        songIsPlaying = false;
                        songCompleted = true;
                        fab.setImageResource(R.drawable.ic_action_play);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    mediaPlayer.seekTo(progressValue);
                    seekBar.setProgress(progressValue);
    //                currentTimeTv.setText(convertToMinutesAndSeconds(progressValue));
                    songPosition = progressValue;
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private String convertToMinutesAndSeconds(int duration) {
        long mins = ((duration / 1000) / 60);
        long secs = ((duration / 1000) % 60);

        return mins+":"+secs;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
