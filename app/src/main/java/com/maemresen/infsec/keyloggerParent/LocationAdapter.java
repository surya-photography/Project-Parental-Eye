package com.maemresen.infsec.keyloggerParent;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
    private static List<String> locationList;
    
    public LocationAdapter( List<String> locationList) {
        this.locationList = locationList;
    }
    
    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_card, parent,
                false);
        return new LocationViewHolder(itemView);
    }
    
    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        String badWord = String.valueOf(locationList.get(getItemCount() - position - 1));
        holder.bind(badWord);
    }
    
    @Override
    public int getItemCount() {
        return locationList.size();
    }
    
    static class LocationViewHolder extends RecyclerView.ViewHolder {
        private TextView LocationText;
        private ImageView Directions;
        
        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            LocationText = itemView.findViewById(R.id.locationText);
            Directions = itemView.findViewById(R.id.directions);
    
            Directions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Get the coordinates or address associated with this card
                    String location = locationList.get(getAdapterPosition());
            
                    // Open Google Maps with the coordinates or address
                    openGoogleMaps(location);
                }
            });
        }
        
        public void bind(String location) {
            LocationText.setText(location);
        }
        private void openGoogleMaps(String location) {
            String regex = "LOCATION: ([+-]?\\d+\\.\\d+) lat, ([+-]?\\d+\\.\\d+) lon";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(location);
            if (matcher.find()) {
                String latitude = matcher.group(1);
                String longitude = matcher.group(2);
            
                // Create an intent to open Google Maps with the extracted latitude and longitude
                String coordinates = latitude + "," + longitude;
                Uri gmmIntentUri = Uri.parse("geo:" + coordinates + "?q=" + coordinates);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
            
                // Check if there's an app available to handle the intent
                PackageManager packageManager = itemView.getContext().getPackageManager();
                if (mapIntent.resolveActivity(packageManager) != null) {
                    // Start the activity
                    itemView.getContext().startActivity(mapIntent);
                } else {
                    // Handle the case where Google Maps app is not installed
                    Toast.makeText(itemView.getContext(), "Google Maps app is not installed", Toast.LENGTH_SHORT).show();
                }
            }
        }
        
    
    }
}