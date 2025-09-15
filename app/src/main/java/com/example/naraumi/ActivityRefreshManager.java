package com.example.naraumi;

import android.app.Activity;


public class ActivityRefreshManager {
    

    private static boolean needsRefresh = false;
    

    public static void setRefreshNeeded() {
        needsRefresh = true;
    }
    

    public static boolean isRefreshNeeded() {
        return needsRefresh;
    }
    public static void clearRefreshNeeded() {
        needsRefresh = false;
    }
    

    public static void refreshCurrentActivity(Activity activity) {
        if (isRefreshNeeded()) {
            clearRefreshNeeded();
            activity.recreate();
        }
    }
} 