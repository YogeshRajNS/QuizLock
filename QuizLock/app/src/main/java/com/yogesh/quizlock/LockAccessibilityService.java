package com.yogesh.quizlock;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;

/**
 * Watches which app comes to the foreground. If it is a locked app that has not
 * been unlocked in this visit, the quiz screen is shown on top of it.
 * An app stays unlocked until you switch to a different app (or go home).
 */
public class LockAccessibilityService extends AccessibilityService {

    /** Package the user unlocked by passing the quiz; cleared when they leave it. */
    static volatile String unlockedPackage = null;

    private String lastPromptPkg = null;
    private long lastPromptTime = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        CharSequence p = event.getPackageName();
        if (p == null) return;
        String pkg = p.toString();

        // Our own quiz screen, the status bar / notification shade and the keyboard
        // must not count as "leaving" the unlocked app.
        if (pkg.equals(getPackageName()) || isOverlayPackage(pkg)) return;

        if (pkg.equals(unlockedPackage)) return;

        // User moved to some other app -> lock the previous one again.
        unlockedPackage = null;

        if (LockPrefs.isLocked(this, pkg)) {
            long now = SystemClock.elapsedRealtime();
            if (pkg.equals(lastPromptPkg) && now - lastPromptTime < 1500) return; // debounce
            lastPromptPkg = pkg;
            lastPromptTime = now;

            Intent i = new Intent(this, QuizActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra(QuizActivity.EXTRA_PACKAGE, pkg);
            startActivity(i);
        }
    }

    private boolean isOverlayPackage(String pkg) {
        if (pkg.equals("com.android.systemui") || pkg.equals("android")) return true;
        String ime = Settings.Secure.getString(getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        return ime != null && ime.startsWith(pkg + "/");
    }

    @Override
    public void onInterrupt() {}
}
