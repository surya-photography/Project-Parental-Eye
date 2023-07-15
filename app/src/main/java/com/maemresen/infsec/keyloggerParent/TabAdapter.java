package com.maemresen.infsec.keyloggerParent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


public class TabAdapter extends FragmentPagerAdapter {
    
    public TabAdapter( @NonNull FragmentManager fm ) {
        super( fm, FragmentPagerAdapter.BEHAVIOR_SET_USER_VISIBLE_HINT );
    }
    
    @NonNull
    @Override
    public Fragment getItem( int position ) {
        // Return the appropriate fragment based on the tab position
        switch (position) {
            case 0:
                return new AllRecordsFragment();
            case 1:
                return new KeyboardActivityFragment();
            case 2:
                return new CaughtUsingBadWordsFragment();
            case 3:
                return new LocationFragment();
            case 4:
                return new Credits();
            default:
                return null;
        }
    }
    
    @Override
    public int getCount() {
        return 5;
    }
    
    @Override
    public CharSequence getPageTitle( int position ) {
        // Return the title of each tab
        switch (position) {
            case 0:
                return "All Records";
            case 1:
                return "Keyboard Activity";
            case 2:
                return "Caught Using Bad Words";
            case 3:
                return "Location";
            case 4:
                return "Credits";
            default:
                return null;
        }
    }
}