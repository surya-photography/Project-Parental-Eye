package com.maemresen.infsec.keylogapp.util;

import java.util.UUID;

public class Helper {
    
    private final static String APP_UUID = UUID.randomUUID().toString();
    ;
    private final static String LOG_TAG_PREFIX = "KeyLogger";
    
    public static String getLogTag( Class<?> clazz ) {
        return String.format( "%s-%s", LOG_TAG_PREFIX, clazz.getSimpleName() );
    }
    
    public static String getUuid() {
        return APP_UUID;
    }
    
    public static boolean isURL( String url ) {
        return url != null && ( url.startsWith( "http://" ) || url.startsWith( "https://" ) );
    }
}