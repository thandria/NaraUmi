package com.example.naraumi;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;

public class LessonProgressManager {

        private static final String PROGRESS_PREFERENCES_NAME = "JapLearnProgress";
    private static final String COMPLETED_LESSONS_KEY = "completed_lessons";

        public static void markLessonCompleted(Context context, String lessonKey) {
        SharedPreferences preferences = getProgressPreferences(context);
        Set<String> completedLessons = getCompletedLessonsSet(preferences);
        
                completedLessons.add(lessonKey);
        
                preferences.edit()
                  .putStringSet(COMPLETED_LESSONS_KEY, completedLessons)
                  .apply();
        
                StreakManager.updateStreak(context);
    }

        public static boolean isLessonCompleted(Context context, String lessonKey) {
        SharedPreferences preferences = getProgressPreferences(context);
        Set<String> completedLessons = getCompletedLessonsSet(preferences);
        return completedLessons.contains(lessonKey);
    }

        public static boolean hasAnyCompletedLessons(Context context, String lessonType, String lessonGroup) {
        SharedPreferences preferences = getProgressPreferences(context);
        Set<String> completedLessons = getCompletedLessonsSet(preferences);

                String lessonPrefix = lessonType + "|" + lessonGroup + "|";
        
                for (String completedLesson : completedLessons) {
            if (completedLesson.startsWith(lessonPrefix)) {
                return true;
            }
        }

        return false;
    }

        public static boolean isGroupCompleted(Context context, String groupName) {
        SharedPreferences preferences = getProgressPreferences(context);
        Set<String> completedLessons = getCompletedLessonsSet(preferences);

                for (String completedLesson : completedLessons) {
            String[] lessonParts = completedLesson.split("\\|");
            
                        if (lessonParts.length >= 2 && lessonParts[1].equals(groupName)) {
                return true;
            }
        }
        
        return false;
    }

        public static String createLessonKey(String lessonType, String lessonGroup, String activityType) {
        return lessonType + "|" + lessonGroup + "|" + activityType;
    }

        public static int countUniqueCompletedGroups(Context context, String lessonType) {
        SharedPreferences preferences = getProgressPreferences(context);
        Set<String> completedLessons = getCompletedLessonsSet(preferences);

        Set<String> completedGroups = new HashSet<>();

                for (String completedLesson : completedLessons) {
            String[] lessonParts = completedLesson.split("\\|");
            
                        if (lessonParts.length >= 3 && lessonParts[0].equals(lessonType)) {
                completedGroups.add(lessonParts[1]);             }
        }

        return completedGroups.size();
    }

        public static boolean areAllSubgroupsCompleted(Context context, String categoryTitle, String groupLabel) {
        SharedPreferences prefs = getProgressPreferences(context);

                String categoryKey = categoryTitle.replace(" ", "_");
        String groupSetKey = categoryKey + groupLabel;

        Set<String> subgroups = prefs.getStringSet(groupSetKey, new HashSet<>());

        if (subgroups.isEmpty()) {
            return false;         }

                for (String subgroup : subgroups) {
            if (!hasAnyCompletedLessons(context, categoryTitle, subgroup)) {
                return false;             }
        }

        return true;     }

        private static SharedPreferences getProgressPreferences(Context context) {
        return context.getSharedPreferences(PROGRESS_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

        private static Set<String> getCompletedLessonsSet(SharedPreferences preferences) {
        Set<String> storedLessons = preferences.getStringSet(COMPLETED_LESSONS_KEY, new HashSet<>());
                return new HashSet<>(storedLessons);
    }
}