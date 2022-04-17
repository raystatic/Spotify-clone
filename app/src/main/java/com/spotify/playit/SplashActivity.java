package com.spotify.playit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spotify.playit.audio.AudioActivity;

import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    private static long SLEEP_TIME = 2;
    private static String TAG = SplashActivity.class.getName();
    FirebaseFirestore db;
    private String android_id;

    private Dialog chooserDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Intent intent = new Intent();
            intent.setClassName(SplashActivity.this.getPackageName(),"com.spotify.playit.video.VideoActivity");
            SplashActivity.this.startActivity(intent);
            SplashActivity.this.finish();
        }catch (Exception e){
            e.printStackTrace();
        }

        db = FirebaseFirestore.getInstance();

        updateDeviceDetails();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // Removes title bar

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.splash_screen1);

        initChooserDialog();

//        IntentLauncher launcher = new IntentLauncher();
//        launcher.start();

    }

    private void initChooserDialog() {
        chooserDialog = new Dialog(this);
        chooserDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        chooserDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        chooserDialog.setContentView(LayoutInflater.from(this).inflate(R.layout.choose_dialog,null, false));

        CardView audio = chooserDialog.findViewById(R.id.cardAudio);
        CardView video = chooserDialog.findViewById(R.id.cardVideo);

        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SplashActivity.this, AudioActivity.class);
                SplashActivity.this.startActivity(intent);
                SplashActivity.this.finish();
            }
        });

        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent();
                    intent.setClassName(SplashActivity.this.getPackageName(),"com.spotify.playit.video.VideoActivity");
                    SplashActivity.this.startActivity(intent);
                    SplashActivity.this.finish();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });

    }

    private class IntentLauncher extends Thread {

        @Override
        /**
         * Sleep for some time and than start new activity.
         */
        public void run() {
            try {
                // Sleeping
                Thread.sleep(SLEEP_TIME*1000);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            // Start main activity
//            Intent intent = new Intent(SplashActivity.this, AudioActivity.class);
//            SplashActivity.this.startActivity(intent);
//            SplashActivity.this.finish();

            try {
                Intent intent = new Intent();
                intent.setClassName(SplashActivity.this.getPackageName(),"com.spotify.playit.video.VideoActivity");
                SplashActivity.this.startActivity(intent);
                SplashActivity.this.finish();
            }catch (Exception e){
                e.printStackTrace();
            }

//            SplashActivity.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (chooserDialog!=null){
//                        chooserDialog.show();
//                        Window window = chooserDialog.getWindow();
//                        window.setLayout(
//                                WindowManager.LayoutParams.MATCH_PARENT,
//                                WindowManager.LayoutParams.WRAP_CONTENT
//                        );
//                    }
//                }
//            });
        }
    }

    private void updateDeviceDetails(){
        try{
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(),0);
            String version = pInfo.versionName;
            Map<String,String> map = new HashMap<>();
            map.put("os_version",System.getProperty("os.version"));
            map.put("api_level", Build.VERSION.SDK_INT+"");
            map.put("device", Build.DEVICE);
            map.put("model",Build.MODEL);
            map.put("product",Build.PRODUCT);
            map.put("current_version",version);

            android_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

            db.collection("users")
                    .document(android_id)
                    .set(map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Details updated");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
                        }
                    });

        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
    }

}
