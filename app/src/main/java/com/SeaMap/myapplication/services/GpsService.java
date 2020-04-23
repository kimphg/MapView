package com.SeaMap.myapplication.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.SeaMap.myapplication.classes.PacketSender;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
//import com.google.android.gms.tasks.OnSuccessListener;

//import java.util.concurrent.Executor;

public class GpsService extends Service {
    PacketSender mPacketSender;
    //private LocationManager mLocationManager;
//    public static final int TIME_MIN = 1000 * 60;
//    public static final float DISTANCE_MIN = 200F;//100m will get location
//    public static final int REQUEST_CODE = 1004;
    boolean locationAccessOK = false;
    private LocationCallback locationCallback;
    // private LocationListener mLocationListener;
    LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationClient;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        //location by phuong


    }
    private boolean isNetworkConnectionAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
    Location oldLocation = new Location("GPS");//use old location to estimate speed and movement

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationCallback = new LocationCallback() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    if(location.getSpeed()<2)
                    {
                        locationRequest.setInterval(30000);
                        locationRequest.setFastestInterval(15000);
                    }
                    else
                    {
                        locationRequest.setInterval(10000);
                        locationRequest.setFastestInterval(5000);
                    }
                    if(oldLocation.distanceTo(location)<10)continue;
                    Intent intent = new Intent("location_update");
                    intent.putExtra("newLocation", location);
                    sendBroadcast(intent);
                    sendOwnLocation(location);
                    oldLocation = location;
                }
            }
        };
        if(isNetworkConnectionAvailable()) {
            mPacketSender = new PacketSender();
            mPacketSender.start();
        }
        locationAccessOK = CheckLocationAccess();
        if (locationAccessOK) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            locationRequest = LocationRequest.create();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
            getLastLocation();
        } else {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            locationRequest = LocationRequest.create();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper());
            getLastLocation();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);//location by phuong

//        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void getDataFromServer() {
        //doc goi tin phan hoi tu may chu
        if(mPacketSender==null)return;
        byte[] answer = mPacketSender.getAnswer();
        if ((answer != null) && (answer.length > 0)) {
            int sizeOne = Short.BYTES + Float.BYTES + Float.BYTES;
            int index = 0;
            List<Location> nearbyShips = new ArrayList<>();
            while (index <= (answer.length - sizeOne)) {
                ByteBuffer buffer;
                buffer = ByteBuffer.wrap(answer, index, 10);

                float lon = buffer.getFloat(0);
                float lat = buffer.getFloat(4);
                short time = buffer.getShort(8);
                Location ship = new Location("GPS");
                ship.setLongitude(lon);
                ship.setLatitude(lat);
                nearbyShips.add(ship);
                index = index + sizeOne;
            }
            int i = 0;
            for (Location ship : nearbyShips) {
                Intent intent = new Intent("location_update");
                intent.putExtra("nearbyShips" + i, ship);
                i++;
                sendBroadcast(intent);
                //sendOwnLocation(location);
            }
        }
    }
    Location lastLocation = new Location("GPS");
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void sendOwnLocation(Location location) {
        //gui toa do den may chu
        if(mPacketSender==null)return;
        if(lastLocation.distanceTo(location)>0.1)// send if moved more than 100m
        {
            mPacketSender.setDataPacket(makePacket(location));
            lastLocation=location;
        }
        getDataFromServer();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private byte[] makePacket(Location location) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES + Float.BYTES + Float.BYTES);
        byteBuffer.putLong(System.currentTimeMillis());
        byteBuffer.putFloat((float) (location.getLongitude()));
        byteBuffer.putFloat((float) (location.getLatitude()));
        return byteBuffer.array();
    }

//    boolean CheckNetworkPermission() {
//        return ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
//                == PackageManager.PERMISSION_GRANTED;
//    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    boolean CheckLocationAccess()//location by phuong
    {
        boolean permissionAccessCoarseLocationApproved =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        boolean permissionAccessBackgroundLocationApproved = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        return permissionAccessBackgroundLocationApproved || permissionAccessCoarseLocationApproved;
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

//    public static int estimateTimeArrival(Location location, double distance) {
//        //distance is in km
//        if (location != null && location.getSpeed() != 0) {
//            return (int) (distance * 1000 / location.getSpeed());
//        } else return -1;
//    }

    void getLastLocation() {
        oldLocation.setLatitude(21);
        oldLocation.setLongitude(105);
//        fusedLocationClient.getLastLocation();
//
//                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        // Got last known location. In some rare situations this can be null.
//                        if (location != null) {
//                            // Logic to handle location object
//                            Intent intent = new Intent("location_update");
//                            intent.putExtra("newLocation", location);
//                            sendBroadcast( intent );
//                        }
//
//                    }
//                });
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
