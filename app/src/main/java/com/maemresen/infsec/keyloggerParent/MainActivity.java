package com.maemresen.infsec.keyloggerParent;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    
    private Spinner ownerSpinner;
    private Spinner dateSpinner;
    private List<String> ownerNames;
    private ArrayAdapter<String> ownerAdapter;
    private List<String> dateList;
    private ArrayAdapter<String> dateAdapter;
    private String selectedOwnerName;
    private String selectedDate;
    private ImageView arrowImageView, ParentalLock, closeChildLock;
    private View deviceOptionsView, viewblur, lockchildlayout;
    
    private LottieAnimationView BigBubbles;
    
    private boolean isOptionsVisible = false;
    
    private TabAdapter tabAdapter;
    private ViewPager viewPager;
    private TextView parentalID;
    
    private String DataBaseReference = "";
    
    private Button lockButton, logoutButton;
    private String lockDurationString;
    private EditText lockduration;
    private boolean isPhoneLocked = false;
    private long lockDurationMillis = 0L;
    private long lastLockButtonPressTime = 0L;
    private final Handler handler = new Handler();
    private Runnable unlockRunnable = new Runnable() {
        @Override
        public void run() {
            isPhoneLocked = false;
            lockButton.setText( "Lock" );
            lockButton.setBackgroundResource( R.drawable.button_signin );
            lockButton.setEnabled( true );
            Toast.makeText( MainActivity.this, "Phone is now unlocked", Toast.LENGTH_SHORT ).show();
        }
    };
    
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
        
        viewblur = findViewById( R.id.viewblur );
        BigBubbles = findViewById( R.id.BigBubbles );
        arrowImageView = findViewById( R.id.arrowImageView );
        deviceOptionsView = findViewById( R.id.device_options );
        arrowImageView.setImageResource( R.drawable.menu );
        deviceOptionsView.setVisibility( View.INVISIBLE );
        lockduration = findViewById( R.id.lockduration );
        ParentalLock = findViewById( R.id.ParentalLock );
        lockchildlayout = findViewById( R.id.lockchildlayout );
        closeChildLock = findViewById( R.id.closeChildLock );
        logoutButton = findViewById( R.id.logoutButton );
        parentalID = findViewById( R.id.parentalID );
        
        arrowImageView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if (isOptionsVisible) {
                    slideUp( deviceOptionsView );
                    arrowImageView.setImageResource( R.drawable.menu );
                    viewblur.setVisibility( View.GONE );
                    BigBubbles.setVisibility( View.GONE );
                } else {
                    slideDown( deviceOptionsView );
                    arrowImageView.setImageResource( R.drawable.cross );
                    BigBubbles.setVisibility( View.VISIBLE );
                    viewblur.setVisibility( View.VISIBLE );
                }
                isOptionsVisible = !isOptionsVisible;
            }
        } );
        
        
        TabLayout tabLayout = findViewById( R.id.tab_layout );
        viewPager = findViewById( R.id.view_pager );
        tabAdapter = new TabAdapter( getSupportFragmentManager() );
        viewPager.setAdapter( tabAdapter );
        
        // Create custom tab viewsa
        int[] tabIcons = { R.drawable.ic_tab_all_records,
                R.drawable.ic_tab_keyboard_activity, R.drawable.ic_tab_caught_using_badwords,
                R.drawable.location_new, R.drawable.acknowledgement };
        String[] tabTitles = { "All Records", "Keyboard Activity", "Using BadWords", "Realtime " +
                "Location", "Credits" };
        
        for (int i = 0; i < tabIcons.length; i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            View tabView = LayoutInflater.from( this ).inflate( R.layout.costom_tab, null );
            
            ImageView tabIcon = tabView.findViewById( R.id.tab_icon );
            TextView tabTitle = tabView.findViewById( R.id.tab_title );
            
            tabIcon.setImageResource( tabIcons[ i ] );
            tabTitle.setText( tabTitles[ i ] );
            
            tab.setCustomView( tabView );
            tabLayout.addTab( tab );
        }
        
        // Set the TabLayout's OnTabSelectedListener to update the ViewPager's current item
        tabLayout.addOnTabSelectedListener( new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected( TabLayout.Tab tab ) {
                viewPager.setCurrentItem( tab.getPosition() );
            }
            
            @Override
            public void onTabUnselected( TabLayout.Tab tab ) {
            }
            
            @Override
            public void onTabReselected( TabLayout.Tab tab ) {
            }
        } );
        
        // Set the ViewPager's OnPageChangeListener to update the selected tab
        viewPager.addOnPageChangeListener( new TabLayout.TabLayoutOnPageChangeListener( tabLayout ) );
        
        
        ownerSpinner = findViewById( R.id.ownerSpinner );
        dateSpinner = findViewById( R.id.dateSpinner );
        
        // Set the adapter for the owner spinner
        ownerNames = new ArrayList<>();
        ownerAdapter = new ArrayAdapter<>( this, android.R.layout.simple_spinner_item, ownerNames );
        ownerAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        ownerSpinner.setAdapter( ownerAdapter );
        
        // Set the adapter for the date spinner
        dateList = new ArrayList<>();
        dateAdapter = new ArrayAdapter<>( this, android.R.layout.simple_spinner_item, dateList );
        dateAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        dateSpinner.setAdapter( dateAdapter );
        
        
        SharedPreferences preference = getSharedPreferences( "UserLoginActivity", MODE_PRIVATE );
        String ParentalID = preference.getString( "UserEmailPref", "Unknown User" );
        parentalID.setText( ParentalID );
        DataBaseReference = "Keylogger: User ( " + ParentalID + " )";
        
        DatabaseReference ownerRef = FirebaseDatabase.getInstance().getReference( DataBaseReference );
        ownerRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange( @NonNull DataSnapshot dataSnapshot ) {
                ownerNames.clear();
                for (DataSnapshot ownerSnapshot : dataSnapshot.getChildren()) {
                    String ownerName = ownerSnapshot.getKey();
                    ownerNames.add( ownerName );
                }
                ownerAdapter.notifyDataSetChanged();
            }
            
            @Override
            public void onCancelled( @NonNull DatabaseError databaseError ) {
                Log.e( "FirebaseError", "Data retrieval canceled: " + databaseError.getMessage() );
            }
        } );
        
        // Set the owner spinner listener to update the selected owner name and fetch dates
        ownerSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
                selectedOwnerName = parent.getItemAtPosition( position ).toString();
                fetchDatesForOwner( selectedOwnerName );
                ownerSpinner.setSelection( ownerNames.indexOf( selectedOwnerName ) );
            }
            
            @Override
            public void onNothingSelected( AdapterView<?> parent ) {
//                selectedOwnerName = Build.MODEL;
//                fetchDatesForOwner( selectedOwnerName );
            }
        } );
        
        // Set the date spinner listener to update the selected date and update fragments
        dateSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
                selectedDate = parent.getItemAtPosition( position ).toString();
                dateSpinner.setSelection( dateList.indexOf( selectedDate ) );
                updateFragments( selectedOwnerName, selectedDate );
            }
            
            @Override
            public void onNothingSelected( AdapterView<?> parent ) {
//                Date now = DateTimeHelper.getCurrentDay();
//                SimpleDateFormat dateFormat = new SimpleDateFormat( "dd-MM-yyyy", Locale.getDefault() );
//                String databaseName = dateFormat.format( now );
//                updateFragments( Build.MODEL, databaseName );
            }
        } );
        
        ParentalLock.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                lockchildlayout.setVisibility( View.VISIBLE );
            }
        } );
        closeChildLock.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                lockchildlayout.setVisibility( View.GONE );
            }
        } );
        
        
        lockButton = findViewById( R.id.lockButton );
        lockButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if (isPhoneLocked) {
                    Toast.makeText( MainActivity.this,
                            "Phone is already locked\n\tWait for " + lockDurationString + " minute" +
                                    "(s)",
                            Toast.LENGTH_SHORT ).show();
                } else {
                    // Phone is not locked, so lock it
                    lockDurationString = "1";
                    lockDurationMillis = 60 * 1000;
                    
                    // Extract lock duration from the EditText and convert it to milliseconds
                    String durationInput = lockduration.getText().toString().trim();
                    if (!durationInput.isEmpty()) {
                        lockDurationString = durationInput;
                        lockDurationMillis = Long.parseLong( lockDurationString ) * 60 * 1000;
                    }
                    
                    // Disable the lock button and start the lock process
                    lockButton.setText( "Locked Sucessfully" );
                    lockButton.setEnabled( false );
                    lockButton.setBackgroundResource( R.drawable.google_gradient );
                    Toast.makeText( MainActivity.this, "Phone is locked for " + lockDurationString + " minute(s)", Toast.LENGTH_SHORT ).show();
                    
                    // Set the phone as locked and schedule the unlock task
                    isPhoneLocked = true;
                    lastLockButtonPressTime = SystemClock.elapsedRealtime();
                    handler.postDelayed( unlockRunnable, lockDurationMillis );
                }
            }
        } );
        
        
        logoutButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                SharedPreferences preference = getSharedPreferences( "UserLoginActivity", MODE_PRIVATE );
                SharedPreferences.Editor editor = preference.edit();
                editor.putBoolean( "login", false );
                editor.remove("UserEmailPref");
                editor.apply();
                
                Toast customToast = new Toast( MainActivity.this );
                customToast.setDuration( Toast.LENGTH_SHORT );
                // Inflate the custom layout for the Toast
                LayoutInflater inflater = LayoutInflater.from( MainActivity.this );
                View toastView = inflater.inflate( R.layout.costum_toast, null );
                customToast.setView( toastView );
                customToast.setGravity( Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0 );
                customToast.show();
                
                Intent logoutIntent = new Intent( MainActivity.this, SignInUpActivity.class );
                startActivity( logoutIntent );
                finish();
                
            }
        } );
        
    }
    
    private void updateFragments( String ownerName, String date ) {
    
        SharedPreferences preference = getSharedPreferences( "UserLoginActivity", MODE_PRIVATE );
        String ParentalID = preference.getString( "UserEmailPref", "Unknown User" );
        String DataBaseReference = "Keylogger: User ( " + ParentalID + " )";
        
        
        Bundle bundle = new Bundle();
        bundle.putString( "OWNER_NAME", ownerName );
        bundle.putString( "SELECTED_DATE", date );
        bundle.putString( "DatabaseName",DataBaseReference );
        
        Intent serviceIntent = new Intent( MainActivity.this, NotificationService.class );
        serviceIntent.putExtra("OWNER_NAME", ownerName);
        serviceIntent.putExtras( bundle );
        
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService( serviceIntent );
        } else {
            startService( serviceIntent );
        }
        
        
//        // Update all fragments in the ViewPager
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();

        for (Fragment fragment : fragments) {
            if (fragment instanceof AllRecordsFragment) {
                ( (AllRecordsFragment) fragment ).setArguments( bundle );
                ( (AllRecordsFragment) fragment ).UpdateFragment(ownerName, date, DataBaseReference);
            } else if (fragment instanceof KeyboardActivityFragment) {
                ( (KeyboardActivityFragment) fragment ).setArguments( bundle );
                ( (KeyboardActivityFragment) fragment ).UpdateFragment( ownerName, date,DataBaseReference);
            } else if (fragment instanceof CaughtUsingBadWordsFragment) {
                ( (CaughtUsingBadWordsFragment) fragment ).setArguments( bundle );
                ( (CaughtUsingBadWordsFragment) fragment ).UpdateFragment( ownerName, date,DataBaseReference );
            } else if (fragment instanceof LocationFragment) {
                ( (LocationFragment) fragment ).setArguments( bundle );
                ( (LocationFragment) fragment ).UpdateFragment( ownerName, date,DataBaseReference );
            }
        }
        
    }
    
    
    private void fetchDatesForOwner( String ownerName ) {
        
        SharedPreferences preference = getSharedPreferences( "UserLoginActivity", MODE_PRIVATE );
        String ParentalID = preference.getString( "UserEmailPref", "Unknown User" );
        String DataBaseReference = "Keylogger: User ( " + ParentalID + " )";
        
        DatabaseReference datesRef = FirebaseDatabase.getInstance().getReference( DataBaseReference )
                .child( ownerName );
        
        datesRef.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange( @NonNull DataSnapshot dataSnapshot ) {
                dateList.clear();
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    String date = dateSnapshot.getKey();
                    dateList.add( date );
                }
                dateAdapter = new ArrayAdapter<>( MainActivity.this, android.R.layout.simple_spinner_item, dateList );
                dateAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
                dateSpinner.setAdapter( dateAdapter );
            }
            
            @Override
            public void onCancelled( @NonNull DatabaseError databaseError ) {
                Log.e( "FirebaseError", "Data retrieval canceled: " + databaseError.getMessage() );
            }
        } );
    }
    
    private void slideDown( View view ) {
        ObjectAnimator animator = ObjectAnimator.ofFloat( view, "translationY", -view.getHeight(), 0 );
        animator.setDuration( 300 );
        animator.start();
        view.setVisibility( View.VISIBLE );
    }
    
    private void slideUp( View view ) {
        ObjectAnimator animator = ObjectAnimator.ofFloat( view, "translationY", 0, -view.getHeight() );
        animator.setDuration( 300 );
        animator.start();
        view.setVisibility( View.GONE );
    }
}