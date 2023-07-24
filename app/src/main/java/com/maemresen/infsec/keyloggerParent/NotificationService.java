package com.maemresen.infsec.keyloggerParent;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationService extends Service implements Application.ActivityLifecycleCallbacks {
    
    private static final String CHANNEL_ID = "my_channel";
    private static final CharSequence CHANNEL_NAME = "Bad Words Detector";
    private static final String CHANNEL_DESCRIPTION = "This notification detects and push " +
            "notification if your child types any bad word in his device";
    
    private static final int NOTIFICATION_ID = 1;
    
    private static final int NOTIFICATION_ID_FOREGROUND = 1;
    private static final int NOTIFICATION_ID_BACKGROUND = 2;
    
    private static final long NOTIFICATION_INTERVAL =  30 * 1000; // 30 seconds
    private Date lastNotificationTime = null;
    private NotificationManager notificationManager;
    private boolean isAppInForeground = false;
    
    
    private static String ownerName = "";
    
    public String DatabaseReference = "";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize notification manager
        notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        
        // Create notification channel for Android Oreo and above
        createNotificationChannel();
        
        // Create a notification for the foreground service
        Notification notification = buildNotification();
        
    
        startForeground(NOTIFICATION_ID, notification);
        
        // Register the activity lifecycle callbacks
        Application application = (Application) getApplicationContext();
        application.registerActivityLifecycleCallbacks(this);
    }
    
    
    private Notification buildNotification() {
        // Get the app icon drawable
        Drawable appIcon = getApplicationInfo().loadIcon( getPackageManager() );
        
        // Convert the drawable to a bitmap
        Bitmap appIconBitmap;
        if (appIcon instanceof BitmapDrawable) {
            appIconBitmap = ( (BitmapDrawable) appIcon ).getBitmap();
        } else {
            // Create a fallback bitmap if the drawable is not a BitmapDrawable
            appIconBitmap = Bitmap.createBitmap( appIcon.getIntrinsicWidth(),
                    appIcon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888 );
            appIcon.setBounds( 0, 0, appIconBitmap.getWidth(), appIconBitmap.getHeight() );
            Canvas canvas = new Canvas( appIconBitmap );
            appIcon.draw( canvas );
        }
        
        // Set the app icon as both small icon and large icon in the notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder( this, CHANNEL_ID )
                .setSmallIcon( R.drawable.app_icon ) // Use a placeholder small icon here
                .setLargeIcon( appIconBitmap )
                .setContentTitle( "Parental Eye" )
                .setContentText( "Running in the background" )
                .setPriority( NotificationCompat.PRIORITY_DEFAULT );
        
        return builder.build();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel( CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT );
            channel.setDescription( CHANNEL_DESCRIPTION );
            channel.enableLights( true );
            channel.setLightColor( Color.RED );
            notificationManager.createNotificationChannel( channel );
        }
    }
    
    @Override
    public int onStartCommand( Intent intent, int flags, int startId ) {
        ownerName = intent.getStringExtra("OWNER_NAME");
   
        Date now = DateTimeHelper.getCurrentDay();
        SimpleDateFormat dateFormat = new SimpleDateFormat( "dd-MM-yyyy", Locale.getDefault() );
        String selectedDate = dateFormat.format( now );
    
        SharedPreferences preference = getSharedPreferences( "UserLoginActivity", MODE_PRIVATE );
        String userEmail = preference.getString( "UserEmailPref", "Unknown User" );
        DatabaseReference = "Keylogger: User ( " + userEmail + " )";
    
        Toast.makeText( this, ownerName, Toast.LENGTH_SHORT ).show();
        Toast.makeText( this, DatabaseReference, Toast.LENGTH_SHORT ).show();
        // Initialize Firebase database reference
        DatabaseReference allRecordsRef = FirebaseDatabase.getInstance().getReference( DatabaseReference )
                .child( ownerName )
                .child( selectedDate )
                .child( "Bad Word Usage" );
        
        // Add a ValueEventListener to track new messages
        allRecordsRef.addValueEventListener( new ValueEventListener() {
            
            public void onDataChange( @NonNull DataSnapshot dataSnapshot ) {
                
                if (!isAppInForeground()) {
                    Date currentTime = new Date();
                    if (lastNotificationTime == null || currentTime.getTime() - lastNotificationTime.getTime() >= NOTIFICATION_INTERVAL) {
                        sendNotification();
                        lastNotificationTime = currentTime; // Update the last notification time
                    }
                }
            }
            
            @Override
            public void onCancelled( @NonNull DatabaseError databaseError ) {
                // Handle database error
                Log.e( "FirebaseError", "Data retrieval canceled: " + databaseError.getMessage() );
            }
        } );
        
        // Return START_STICKY to indicate that the service should be restarted if it gets terminated
        return START_STICKY;
    }
    
    public void sendNotification() {
        // Get the app icon drawable
        Drawable appIcon = getApplicationInfo().loadIcon( getPackageManager() );
        
        // Convert the drawable to a bitmap
        Bitmap appIconBitmap;
        if (appIcon instanceof BitmapDrawable) {
            appIconBitmap = ( (BitmapDrawable) appIcon ).getBitmap();
        } else {
            // Create a fallback bitmap if the drawable is not a BitmapDrawable
            appIconBitmap = Bitmap.createBitmap( appIcon.getIntrinsicWidth(),
                    appIcon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888 );
            appIcon.setBounds( 0, 0, appIconBitmap.getWidth(), appIconBitmap.getHeight() );
            Canvas canvas = new Canvas( appIconBitmap );
            appIcon.draw( canvas );
        }
    
        SharedPreferences preference = getSharedPreferences( "UserLoginActivity", MODE_PRIVATE );
        String userEmail = preference.getString( "UserEmailPref", "Unknown User" );
        // Set the app icon as both small icon and large icon in the notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder( this, CHANNEL_ID )
                .setSmallIcon( R.drawable.app_icon ) // Use a placeholder small icon here
                .setLargeIcon( appIconBitmap )
                .setContentTitle( "Parental Eye" )
                .setContentText( "Detected Usage of Bad Word in your Child's Phone Connected to " +
                        "ParentalID "+userEmail)
                .setPriority( NotificationCompat.PRIORITY_DEFAULT );

        
        Notification notification = builder.build();
        notificationManager.notify( NOTIFICATION_ID, notification );
    }
    
    
    
    
    
    @Nullable
    @Override
    public IBinder onBind( Intent intent ) {
        return null;
    }
    
    @Override
    public void onActivityCreated( @NonNull Activity activity, @Nullable Bundle bundle ) {
    
    }
    
    @Override
    public void onActivityStarted( @NonNull Activity activity ) {
    
    }
    
    public void onActivityResumed( Activity activity) {
        isAppInForeground = true;
    }
    
    @Override
    public void onActivityPaused(Activity activity) {
        isAppInForeground = false;
    }
    
    @Override
    public void onActivityStopped( @NonNull Activity activity ) {
    
    }
    @Override
    public void onActivitySaveInstanceState( @NonNull Activity activity, @NonNull Bundle bundle ) {
    
    }
    
    @Override
    public void onActivityDestroyed( @NonNull Activity activity ) {
    
    }
    public boolean isAppInForeground() {
        return isAppInForeground;
    }
}