package com.spotify.cl;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.spotify.cl.services.NotificationActionService;

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

            PendingIntent pendingIntentPrevious;
            int drw_previous;
            if (pos == 0){
                pendingIntentPrevious = null;
                drw_previous = 0;
            }else {
                Intent intentPrevious = new Intent(context, NotificationActionService.class)
                        .setAction(ACTION_PREVIOUS);
                pendingIntentPrevious = PendingIntent.getBroadcast(context, 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
                drw_previous = R.drawable.ic_action_prev_black;
            }

            Intent intentPlay = new Intent(context, NotificationActionService.class)
                    .setAction(ACTION_PLAY);
            PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0, intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);


            PendingIntent pendingIntentNext;
            int drw_Next;
            if (pos == size){
                pendingIntentNext = null;
                drw_Next = 0;
            }else {
                Intent intentNext = new Intent(context, NotificationActionService.class)
                        .setAction(ACTION_NEXT);
                pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);
                drw_Next = R.drawable.ic_action_next_black;
            }

            notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_action_song)
                    .setContentTitle(song.getSongName())
                    .setContentText(song.getArtistName())
                    .setLargeIcon(icon)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .addAction(drw_previous,"Previous",pendingIntentPrevious)
                    .addAction(playButton,"Play",pendingIntentPlay)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0,1,2)
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                    .addAction(drw_Next,"Next",pendingIntentNext)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();

            notificationManagerCompat.notify(1,notification);

        }

    }

}
