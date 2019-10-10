package com.spotify.cl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerMainAdpter extends RecyclerView.Adapter<RecyclerMainAdpter.MyViewHolder> {

    Context context;
    ArrayList<String> songs, artists;
    ArrayList<Song> songArrayList;

    public RecyclerMainAdpter(Context context, ArrayList<Song> songArrayList) {
        this.context = context;
        this.songArrayList = songArrayList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_main_adapter_layout,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Song song = songArrayList.get(position);

        holder.songname.setText(song.songName);
        holder.artistname.setText(song.artistName);
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

}
