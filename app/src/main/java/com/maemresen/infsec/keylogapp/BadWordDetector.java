package com.maemresen.infsec.keylogapp;

import java.util.HashSet;
import java.util.Set;

public class BadWordDetector {
    // i took refrence from his website https://en.wiktionary.org/wiki/Category:English_swear_words
    
    private static final String TAG = "BadWordDetector";
    private Set<String> filterList;
    
    public BadWordDetector() {
        // Initialize the filter list with bad words
        filterList = new HashSet<>();
        filterList.add( "ass" );
        filterList.add( "asshole" );
        filterList.add( "bastard" );
        filterList.add( "bitch" );
        filterList.add( "bloody" );
        filterList.add( "botherfucker" );
        filterList.add( "bullshit" );
        filterList.add( "cock" );
        filterList.add( "damn" );
        filterList.add( "dick" );
        filterList.add( "fuck" );
        filterList.add( "fuckoff" );
        filterList.add( "fuckyou" );
        filterList.add( "hell" );
        filterList.add( "holy shit" );
        filterList.add( "shit" );
        filterList.add( "motherfucker" );
        filterList.add( "piss" );
        filterList.add( "pussy" );
        filterList.add( "shite" );
        filterList.add( "whore" );
        filterList.add( "erotic" );
        filterList.add( "sex" );
        filterList.add( "piss" );
        filterList.add( "pussy" );
        filterList.add( "shite" );
    }
    
    public boolean containsBadWord( String text ) {
        for (String word : filterList) {
            if (text.toLowerCase().contains( word.toLowerCase() )) {
                return true;
            }
        }
        return false;
    }
}