package com.spotify.playit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();

        updateDeviceDetails();

        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // Removes title bar

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.splash_screen1);

        IntentLauncher launcher = new IntentLauncher();
        launcher.start();

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
            Intent intent = new Intent(SplashActivity.this, AudioActivity.class);
            SplashActivity.this.startActivity(intent);
            SplashActivity.this.finish();
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
