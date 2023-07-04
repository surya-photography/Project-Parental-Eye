package com.maemresen.infsec.keyloggerParent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


public class TabAdapter extends FragmentPagerAdapter {
    
    public TabAdapter( @NonNull FragmentManager fm ) {
        super( fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT );
    }
    
    @NonNull
    @Override
    public Fragment getItem( int position ) {
        // Return the appropriate fragment based on the tab position
        switch (position) {
            case 1:
                return new DigitalWellBeingFragment();
            case 0:
                return new AllRecordsFragment();
            case 2:
                return new KeyboardActivityFragment();
            case 3:
                return new CaughtUsingBadWordsFragment();
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
            case 1:
                return "Digital Well-being";
            case 0:
                return "All Records";
            case 2:
                return "Keyboard Activity";
            case 3:
                return "Caught Using Bad Words";
            case 4:
                return "Credits";
            default:
                return null;
        }
    }
}