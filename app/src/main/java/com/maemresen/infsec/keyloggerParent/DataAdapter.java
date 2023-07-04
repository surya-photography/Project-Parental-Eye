package com.maemresen.infsec.keyloggerParent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataItemViewHolder> {
    private List<DataItem> dataItemList;
    
    public DataAdapter( List<DataItem> dataItemList ) {
        this.dataItemList = dataItemList;
    }
    
    @NonNull
    @Override
    public DataItemViewHolder onCreateViewHolder( @NonNull ViewGroup parent, int viewType ) {
        View view = LayoutInflater.from( parent.getContext() )
                .inflate( R.layout.item_data, parent, false );
        return new DataItemViewHolder( view );
    }
    
    @Override
    public void onBindViewHolder( @NonNull DataItemViewHolder holder, int position ) {
        DataItem dataItem = dataItemList.get( position );
        holder.uuidTextView.setText( "Uuid: "+dataItem.getUuid() );
        holder.keyLogDateTextView.setText( "KeyLog Date: "+dataItem.getKeyLogDate() );
        holder.accessibilityEventTextView.setText( "Event Accesed: "+dataItem.getAccessibilityEvent() );
        holder.msgTextView.setText( "Message: "+dataItem.getMsg() );
        
    }
    
    @Override
    public int getItemCount() {
        return dataItemList.size();
    }
    
    static class DataItemViewHolder extends RecyclerView.ViewHolder {
        private TextView uuidTextView;
        private TextView keyLogDateTextView;
        private TextView accessibilityEventTextView;
        private TextView msgTextView;
        
        public DataItemViewHolder( @NonNull View itemView ) {
            super( itemView );
            uuidTextView = itemView.findViewById( R.id.uuidTextView );
            keyLogDateTextView = itemView.findViewById( R.id.keyLogDateTextView );
            accessibilityEventTextView = itemView.findViewById( R.id.accessibilityEventTextView );
            msgTextView = itemView.findViewById( R.id.msgTextView );
        }
    }
}