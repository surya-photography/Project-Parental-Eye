package com.maemresen.infsec.keyloggerParent;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BadWordsAdapter extends RecyclerView.Adapter<BadWordsAdapter.BadWordViewHolder> {
    private List<String> badWordsList;
    
    public BadWordsAdapter(List<String> badWordsList) {
        this.badWordsList = badWordsList;
    }
    
    @NonNull
    @Override
    public BadWordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bad_word, parent, false);
        return new BadWordViewHolder(itemView);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BadWordViewHolder holder, int position) {
        String badWord = String.valueOf(badWordsList.get(getItemCount() - position - 1));
        holder.bind(badWord);
    }
    
    @Override
    public int getItemCount() {
        return badWordsList.size();
    }
    
    static class BadWordViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewBadWord;
        
        public BadWordViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewBadWord = itemView.findViewById(R.id.textView2);
        }
        
        public void bind(String badWord) {
            textViewBadWord.setText(badWord);
        }
    }
}