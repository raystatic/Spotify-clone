package com.spotify.cl.utility;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {

    Context context;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    int PRIVATE_MODE = 0;

    public static final String PREF_NAME = "spotify_clone";
    public static final String RECENT_SONG_INDEX = "recent_song_index";

    public PrefManager(Context context){
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        editor = pref.edit();
    }

    public void saveString(String key, String data){
        editor.putString(key,data);
        editor.commit();
    }

    public void saveInt(String key, int data){
        editor.putInt(key,data);
        editor.commit();
    }

    public int getInt(String key){
        return pref.getInt(key, -1);
    }

    public String getString(String key){
        return pref.getString(key,null);
    }

}
