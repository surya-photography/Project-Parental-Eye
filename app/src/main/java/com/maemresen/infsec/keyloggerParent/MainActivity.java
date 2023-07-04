package com.maemresen.infsec.keyloggerParent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;


public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        
        TabLayout tabLayout = findViewById( R.id.tab_layout );
        ViewPager viewPager = findViewById( R.id.view_pager );
        TabAdapter tabAdapter = new TabAdapter( getSupportFragmentManager() );
        viewPager.setAdapter( tabAdapter );
        
        // Create custom tab views
        int[] tabIcons = { R.drawable.ic_tab_all_records,R.drawable.ic_tab_digital_wellbeing,
                R.drawable.ic_tab_keyboard_activity, R.drawable.ic_tab_caught_using_badwords,
                R.drawable.acknowledgement };
        String[] tabTitles = { "All Records", "Digital WellBeing", "Keyboard Activity",
                "Using BadWords", "Credits" };
        
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
        
        
    }
}