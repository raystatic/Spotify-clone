package com.spotify.cl;

import android.app.Notification;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class CreateNotification {
    public static final String CHANNEL_ID = "channel1";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_NEXT= "action_next";

    public static Notification notification;

    public static void createNotification(Context context, Song song, int playButton, int pos, int size){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context,"tag");

            Bitmap icon = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_action_song);

            Log.d("notificationdebug","here i am!");

            notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_action_logo)
                    .setContentTitle(song.getSongName())
                    .setContentText(song.getArtistName())
                    .setLargeIcon(icon)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();

            notificationManagerCompat.notify(1,notification);

        }

    }

}
