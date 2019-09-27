package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.myapplication.MainActivity;

public class GPS_Services extends Service {
    private LocationManager mLocationManager;
    public static  final int TIME_MIN = 1000 * 60 * 2;//2 minute will get location
    public static  final float DISTANCE_MIN = 100F;//100m will get location
    private LocationListener mLocationListener;
    private Location lastLocation;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public GPS_Services() {
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                lastLocation = location;
                Intent intent = new Intent("update_service");
                intent.putExtra("coordinate",location.getLongitude()+" "+ location.getLatitude());
                sendBroadcast(intent);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }
            //IF GPS PROVIDER  DONT ACCEPT , DIRECT USER TO SETTING.
            @Override
            public void onProviderDisabled(String s) {
                Intent myIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity( myIntent );
            }
        };
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);


        //CHECK PERMISSION IS MISSING
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,TIME_MIN,DISTANCE_MIN,mLocationListener);

    }

    @Override
    public void onDestroy() {
        if (mLocationManager!=null)
            mLocationManager.removeUpdates(mLocationListener);
        super.onDestroy();
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificalChanel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText("THIS APP USE YOUR LOCATION")
                .setSmallIcon(R.drawable.ic_chat)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        //do heavy work on a background thread


        //stopSelf();

        return START_NOT_STICKY;
    }

    protected void EnableGpsDialog( String title, String message ){
        AlertDialog.Builder builder = new AlertDialog.Builder( this );
        builder.setMessage(message)
                .setCancelable( false )
                .setTitle(title)
                .setPositiveButton("Turn on GPS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent myIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity( myIntent );
                        dialogInterface.cancel();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createNotificalChanel(){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "FOREGROUND SERVICE CHANEL",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
        else {

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
