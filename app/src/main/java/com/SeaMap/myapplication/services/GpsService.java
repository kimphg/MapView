package com.SeaMap.myapplication.services;

import android.Manifest;
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
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.SeaMap.myapplication.Activity.MainActivity;
import com.SeaMap.myapplication.R;
import com.SeaMap.myapplication.classes.Coordinate;
import  com.SeaMap.myapplication.function.Distance;
public class GpsService extends Service {
    private LocationManager mLocationManager;
    public static  final int TIME_MIN = 1000 * 60 * 2;//2 minute will get location
    public static  final float DISTANCE_MIN = 100F;//100m will get location
    private LocationListener mLocationListener;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Intent intent = new Intent("location_update");

                intent.putExtra("newLocation", location);

                sendBroadcast( intent );
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
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity( intent );
            }
        };

    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        //CHECK PERMISSION IS MISSING
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,TIME_MIN,DISTANCE_MIN,mLocationListener);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (mLocationManager!=null)
            mLocationManager.removeUpdates(mLocationListener);
        super.onDestroy();
    }

    public static double distance( double lon1, double lat1, double lon2, double lat2 ){
//        double dlat = 6371 * Math.toRadians(Math.abs( l1.getLatitude() - lat ));
//        double dlon = 6371 * Math.cos( Math.toRadians( Math.abs(
//                l1.getLatitude()
//        ))) * Math.toRadians( Math.abs( l1.getLongitude() - lon));
//
//        return Math.sqrt( Math.pow( dlat, 2 ) + Math.pow( dlon, 2 ) );
        double deltaLat = Math.toRadians( lat2 - lat1 );
        double deltaLon = Math.toRadians( lon2 - lon1 );

        double haversine = Math.pow(Math.sin( deltaLat / 2 ), 2) +
                Math.cos( Math.toRadians( lat1 ) ) * Math.cos( Math.toRadians( lat2 )) *
                        Math.pow( Math.sin(deltaLon/2), 2);

        return 2 * 6371 * Math.atan2( Math.sqrt( haversine ), Math.sqrt(1 - haversine));
    }

    public static int estimateTimeArrival( Location location , double distance){
        //distance is in km
        if( location != null && location.getSpeed() != 0 ){
            return ( int )(distance * 1000 / location.getSpeed());
        }
        else return -1;
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
//        createNotificalChanel();
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                0, notificationIntent, 0);
//
//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("Foreground Service")
//                .setContentText("lon : ")
//                .setSmallIcon(R.drawable.ic_chat)
//                .setContentIntent(pendingIntent)
//                .build();
//
//        startForeground(1, notification);
//
//        //do heavy work on a background thread
//
//
//        //stopSelf();
//
//        return START_NOT_STICKY;
//    }

//    protected void EnableGpsDialog( String title, String message ){
//        AlertDialog.Builder builder = new AlertDialog.Builder( this );
//        builder.setMessage(message)
//                .setCancelable( false )
//                .setTitle(title)
//                .setPositiveButton("Turn on GPS", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Intent myIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
//                        myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity( myIntent );
//                        dialogInterface.cancel();
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.cancel();
//                    }
//                });
//
//        AlertDialog alert = builder.create();
//        alert.show();
//    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public void createNotificalChanel(){
//        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
//            NotificationChannel notificationChannel = new NotificationChannel(
//                    CHANNEL_ID,
//                    "FOREGROUND SERVICE CHANEL",
//                    NotificationManager.IMPORTANCE_DEFAULT
//            );
//            NotificationManager manager = getSystemService(NotificationManager.class);
//            manager.createNotificationChannel(notificationChannel);
//        }
//    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

//    public String checkGlonass(){
//        boolean isFromGlonass = false;
//        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
//        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
//
//        final GpsStatus gpsStatus = this.mLocationManager.getGpsStatus(null);
//        final Iterable<GpsSatellite> gpsSatellite = gpsStatus.getSatellites();
//
//        for( GpsSatellite satellite : gpsSatellite ){
//            if ( satellite.usedInFix()){
//                if( satellite.getPrn() > 65 && satellite.getPrn() < 88 ){
//                    isFromGlonass = true;
//                }
//                else{
//                    isFromGlonass = false;
//                }
//            }
//            else {
//                isFromGlonass = false;
//            }
//        }
//
//        if( isFromGlonass ){
//            return "Location is form GLONASS";
//        }
//        else{
//            return  "Location is not from GLONASS";
//        }
//
//    }
//
//    //custom get speed function
//    private double GetSpeed(Location currentLocation){
//
//
//        if (currentLocation.hasSpeed()){
//            return currentLocation.getSpeed();
//        }
//        else if(lastLocation!=null) {
//
//            Coordinate point1, point2;
//            point1 = new Coordinate(lastLocation.getLatitude(), lastLocation.getLongitude());
//            point2 = new Coordinate(currentLocation.getLatitude(), currentLocation.getLongitude());
//
//            //distance between two location
//            Distance FunctionDistance = new Distance();
//            double distance = FunctionDistance.DistanceHaversine(point1, point2);//distance is km
//            //time is second
//
//            double timeDif = currentLocation.getTime() - lastLocation.getTime();
//
//            //calculate v by v = s/t
//
//            return (distance * 1000) / timeDif;//current Speed is m/s
//        }
//        return 0;
//    }
}
