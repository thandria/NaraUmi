package com.example.naraumi;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class StreakManager {
    
        private static final String STREAK_PREFERENCES_NAME = "StreakPrefs";
    
        private static final String STREAK_COUNT_KEY = "streak_count";
    private static final String LAST_COMPLETION_DATE_KEY = "last_completion_date";
    
        private static final String DATE_FORMAT = "yyyy-MM-dd";

        public static void updateStreak(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(STREAK_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        String todayDate = getCurrentDateString();
        String lastCompletionDate = preferences.getString(LAST_COMPLETION_DATE_KEY, "");

        if (lastCompletionDate.isEmpty()) {
                        editor.putInt(STREAK_COUNT_KEY, 1);
        } else if (isConsecutiveDay(lastCompletionDate, todayDate)) {
                        int currentStreak = preferences.getInt(STREAK_COUNT_KEY, 0);
            editor.putInt(STREAK_COUNT_KEY, currentStreak + 1);
        } else if (!lastCompletionDate.equals(todayDate)) {
                        editor.putInt(STREAK_COUNT_KEY, 1);
        }
        
                editor.putString(LAST_COMPLETION_DATE_KEY, todayDate);
        editor.apply();
    }

        public static int getCurrentStreak(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(STREAK_PREFERENCES_NAME, Context.MODE_PRIVATE);
        return preferences.getInt(STREAK_COUNT_KEY, 0);
    }

        private static boolean isConsecutiveDay(String earlierDate, String laterDate) {
        try {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            Date previousDate = dateFormatter.parse(earlierDate);
            Date currentDate = dateFormatter.parse(laterDate);

            if (previousDate == null || currentDate == null) {
                return false;
            }

                        long timeDifferenceMs = currentDate.getTime() - previousDate.getTime();
            long daysDifference = timeDifferenceMs / (1000 * 60 * 60 * 24);

                        return daysDifference == 1;
        } catch (Exception e) {
                        return false;
        }
    }

        private static String getCurrentDateString() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return dateFormatter.format(Calendar.getInstance().getTime());
    }
}