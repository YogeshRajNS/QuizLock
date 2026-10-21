package com.yogesh.quizlock;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/** Stores which packages are locked behind the quiz. */
public final class LockPrefs {
    private static final String FILE = "quizlock_prefs";
    private static final String KEY_LOCKED = "locked_packages";

    private LockPrefs() {}

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static Set<String> getLocked(Context c) {
        // Copy: the set returned by SharedPreferences must not be modified.
        return new HashSet<>(prefs(c).getStringSet(KEY_LOCKED, new HashSet<>()));
    }

    public static boolean isLocked(Context c, String pkg) {
        return getLocked(c).contains(pkg);
    }

    /** Time (System.currentTimeMillis) until which this app can't be attempted again. */
    public static long getCooldownUntil(Context c, String key) {
        return prefs(c).getLong("cooldown_" + key, 0L);
    }

    public static void setCooldownUntil(Context c, String key, long until) {
        prefs(c).edit().putLong("cooldown_" + key, until).apply();
    }

    public static void setLocked(Context c, String pkg, boolean locked) {
        Set<String> set = getLocked(c);
        if (locked) set.add(pkg); else set.remove(pkg);
        prefs(c).edit().putStringSet(KEY_LOCKED, set).apply();
    }
}
