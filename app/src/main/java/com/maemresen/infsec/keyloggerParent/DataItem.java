package com.maemresen.infsec.keyloggerParent;

public class DataItem {
    private String uuid;
    private String keyLogDate;
    private String accessibilityEvent;
    private String msg;
    
    public DataItem( ) {
    
    }
    
    public DataItem( String uuid, String keyLogDate, String accessibilityEvent, String msg ) {
        this.uuid = uuid;
        this.keyLogDate = keyLogDate;
        this.accessibilityEvent = accessibilityEvent;
        this.msg = msg;
    }
    
    public String getUuid() {
        return uuid;
    }
    
    public void setUuid( String uuid ) {
        this.uuid = uuid;
    }
    
    public String getKeyLogDate() {
        return keyLogDate;
    }
    
    public void setKeyLogDate( String keyLogDate ) {
        this.keyLogDate = keyLogDate;
    }
    
    public String getAccessibilityEvent() {
        return accessibilityEvent;
    }
    
    public void setAccessibilityEvent( String accessibilityEvent ) {
        this.accessibilityEvent = accessibilityEvent;
    }
    public String getMsg() {
        return msg;
    }
    
    public void setMsg( String msg ) {
        this.msg = msg;
    }
}