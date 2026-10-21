package com.yogesh.quizlock;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Home screen: enable the service and tick the apps to lock. */
public class MainActivity extends Activity {

    private TextView status;
    private ListView list;
    private final List<String> labels = new ArrayList<>();
    private final List<String> packages = new ArrayList<>();

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), 0);

        status = new TextView(this);
        status.setTextSize(15);
        status.setTypeface(Typeface.DEFAULT_BOLD);
        status.setPadding(0, 0, 0, dp(8));
        root.addView(status);

        Button enable = new Button(this);
        enable.setText("Open Accessibility settings");
        enable.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        root.addView(enable);

        Button test = new Button(this);
        test.setText("Try the quiz now");
        test.setOnClickListener(v -> startActivity(new Intent(this, QuizActivity.class)));
        root.addView(test);

        TextView hint = new TextView(this);
        hint.setText("Tick the apps to lock:");
        hint.setTextSize(14);
        hint.setPadding(0, dp(12), 0, dp(4));
        root.addView(hint);

        list = new ListView(this);
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        root.addView(list, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
        loadApps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean on = isServiceEnabled();
        status.setText(on ? "✅ Guard is ON" : "⚠️ Guard is OFF — enable \"QuizLock app guard\" in Accessibility settings");
        status.setTextColor(on ? Color.parseColor("#1B873F") : Color.parseColor("#C62828"));
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN);
        main.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = pm.queryIntentActivities(main, 0);

        List<String[]> rows = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ResolveInfo ri : apps) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(getPackageName()) || !seen.add(pkg)) continue;
            rows.add(new String[]{ri.loadLabel(pm).toString(), pkg});
        }
        Collections.sort(rows, (a, b) -> a[0].compareToIgnoreCase(b[0]));

        labels.clear();
        packages.clear();
        for (String[] r : rows) { labels.add(r[0]); packages.add(r[1]); }

        list.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, labels));
        Set<String> locked = LockPrefs.getLocked(this);
        for (int i = 0; i < packages.size(); i++) list.setItemChecked(i, locked.contains(packages.get(i)));

        list.setOnItemClickListener((parent, view, pos, id) ->
                LockPrefs.setLocked(this, packages.get(pos), list.isItemChecked(pos)));
    }

    private boolean isServiceEnabled() {
        String enabled = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (enabled == null) return false;
        ComponentName me = new ComponentName(this, LockAccessibilityService.class);
        TextUtils.SimpleStringSplitter s = new TextUtils.SimpleStringSplitter(':');
        s.setString(enabled);
        while (s.hasNext()) {
            ComponentName c = ComponentName.unflattenFromString(s.next());
            if (me.equals(c)) return true;
        }
        return false;
    }
}
