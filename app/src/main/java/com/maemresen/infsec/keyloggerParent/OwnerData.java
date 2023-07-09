package com.maemresen.infsec.keyloggerParent;

public class OwnerData {
    private static String selectedOwnerName;
    
    public static String getSelectedOwnerName() {
        return selectedOwnerName;
    }
    
    public static void setSelectedOwnerName(String ownerName) {
        selectedOwnerName = ownerName;
    }
}