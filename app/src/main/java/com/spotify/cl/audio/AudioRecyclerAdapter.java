package com.spotify.cl.audio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.spotify.cl.R;
import com.spotify.cl.RecyclerMainAdpter;

import java.util.ArrayList;

public class AudioRecyclerAdapter extends RecyclerView.Adapter<AudioRecyclerAdapter.MyViewHolder> {

    Context context;
    ArrayList<String> songs, artists;
    ArrayList<Audio> songArrayList;
    SongInteractor listener;
    int selectedPosition=-1;

    public AudioRecyclerAdapter(Context context, ArrayList<Audio> songArrayList, SongInteractor listener) {
        this.context = context;
        this.songArrayList = songArrayList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_main_adapter_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AudioRecyclerAdapter.MyViewHolder holder, final int position) {

        final Audio song = songArrayList.get(position);

        holder.songname.setText(song.getTitle());
        holder.artistname.setText(song.getArtist());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onSongPlayed(song, position);
                selectedPosition=position;
            }
        });

    }

    @Override
    public int getItemCount() {
        return songArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView songname, artistname;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            songname = itemView.findViewById(R.id.song_name_tv);
            artistname = itemView.findViewById(R.id.artist_name_tv);
        }
    }

    public interface SongInteractor{
        //void onSongClicked(Audio song, int position);
        void onSongPlayed(Audio song, int position);
    }

}
