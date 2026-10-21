package com.yogesh.quizlock;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/** Shows 5 questions. All 5 correct -> opens the locked app. Otherwise -> explanations + new set. */
public class QuizActivity extends Activity {

    public static final String EXTRA_PACKAGE = "target_package";
    private static final int QUESTION_COUNT = 5;
    private static final long COOLDOWN_MS = 12 * 60 * 1000L; // 12 minutes after a failed attempt

    private final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private Runnable ticker;

    private String targetPkg;
    private List<QuestionBank.Question> questions;
    private final List<RadioGroup> groups = new ArrayList<>();
    private LinearLayout root;
    private Button submit;
    private boolean showingResults = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        targetPkg = intent.getStringExtra(EXTRA_PACKAGE);
        loadNewQuiz();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private String appLabel(String pkg) {
        if (pkg == null) return "Test quiz";
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
            return pm.getApplicationLabel(ai).toString();
        } catch (Exception e) {
            return pkg;
        }
    }

    private String cooldownKey() {
        return targetPkg == null ? "__test__" : targetPkg;
    }

    private void stopTicker() {
        if (ticker != null) handler.removeCallbacks(ticker);
        ticker = null;
    }

    /** Shown instead of questions while the 12-minute lockout is running. */
    private void showCooldownScreen() {
        stopTicker();
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setBackgroundColor(Color.parseColor("#F4F6FA"));
        box.setPadding(dp(24), dp(24), dp(24), dp(24));

        TextView title = new TextView(this);
        title.setText("⏳ " + appLabel(targetPkg) + " is locked");
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor("#1A1A2E"));
        title.setGravity(Gravity.CENTER);
        box.addView(title);

        TextView msg = new TextView(this);
        msg.setText("You got a question wrong. You can try again after the timer ends.");
        msg.setTextSize(15);
        msg.setTextColor(Color.parseColor("#555566"));
        msg.setGravity(Gravity.CENTER);
        msg.setPadding(0, dp(8), 0, dp(16));
        box.addView(msg);

        TextView timer = new TextView(this);
        timer.setTextSize(44);
        timer.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        timer.setTextColor(Color.parseColor("#C62828"));
        timer.setGravity(Gravity.CENTER);
        box.addView(timer);

        Button home = new Button(this);
        home.setText("Go home");
        home.setOnClickListener(v -> goHome());
        box.addView(home);

        setContentView(box);

        ticker = new Runnable() {
            @Override public void run() {
                long left = LockPrefs.getCooldownUntil(QuizActivity.this, cooldownKey()) - System.currentTimeMillis();
                if (left <= 0) { stopTicker(); loadNewQuiz(); return; }
                long s = (left + 999) / 1000;
                timer.setText(String.format(java.util.Locale.US, "%02d:%02d", s / 60, s % 60));
                handler.postDelayed(this, 1000);
            }
        };
        ticker.run();
    }

    @Override
    protected void onDestroy() {
        stopTicker();
        super.onDestroy();
    }

    private void loadNewQuiz() {
        stopTicker();
        if (System.currentTimeMillis() < LockPrefs.getCooldownUntil(this, cooldownKey())) {
            showCooldownScreen();
            return;
        }
        showingResults = false;
        questions = QuestionBank.pick(QUESTION_COUNT);
        groups.clear();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.parseColor("#F4F6FA"));
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(24), dp(16), dp(32));
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("🔒 Unlock " + appLabel(targetPkg));
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor("#1A1A2E"));
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Answer all " + QUESTION_COUNT + " RAG / GenAI / Agentic AI questions correctly to open the app.");
        sub.setTextSize(14);
        sub.setTextColor(Color.parseColor("#555566"));
        sub.setPadding(0, dp(4), 0, dp(16));
        root.addView(sub);

        for (int i = 0; i < questions.size(); i++) {
            QuestionBank.Question q = questions.get(i);

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundColor(Color.WHITE);
            card.setPadding(dp(14), dp(12), dp(14), dp(12));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = dp(12);
            card.setLayoutParams(lp);

            TextView tag = new TextView(this);
            tag.setText(q.topic.toUpperCase());
            tag.setTextSize(11);
            tag.setTypeface(Typeface.DEFAULT_BOLD);
            tag.setTextColor(Color.parseColor("#3D5AFE"));
            card.addView(tag);

            TextView qt = new TextView(this);
            qt.setText((i + 1) + ". " + q.text);
            qt.setTextSize(16);
            qt.setTextColor(Color.parseColor("#1A1A2E"));
            qt.setPadding(0, dp(4), 0, dp(8));
            card.addView(qt);

            RadioGroup rg = new RadioGroup(this);
            for (int o = 0; o < q.options.length; o++) {
                RadioButton rb = new RadioButton(this);
                rb.setId(View.generateViewId());
                rb.setTag(o);
                rb.setText(q.options[o]);
                rb.setTextSize(15);
                rb.setPadding(dp(4), dp(6), 0, dp(6));
                rg.addView(rb);
            }
            groups.add(rg);
            card.addView(rg);
            root.addView(card);
        }

        submit = new Button(this);
        submit.setText("Submit answers");
        submit.setOnClickListener(v -> {
            if (showingResults) showCooldownScreen(); else checkAnswers();
        });
        root.addView(submit);

        Button cancel = new Button(this);
        cancel.setText("Cancel (go home)");
        cancel.setOnClickListener(v -> goHome());
        root.addView(cancel);

        setContentView(scroll);
    }

    private int selectedIndex(RadioGroup rg) {
        int id = rg.getCheckedRadioButtonId();
        if (id == -1) return -1;
        View v = rg.findViewById(id);
        return v == null ? -1 : (Integer) v.getTag();
    }

    private void checkAnswers() {
        for (RadioGroup rg : groups) {
            if (selectedIndex(rg) == -1) {
                Toast.makeText(this, "Please answer all " + QUESTION_COUNT + " questions", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int correct = 0;
        for (int i = 0; i < questions.size(); i++) {
            QuestionBank.Question q = questions.get(i);
            RadioGroup rg = groups.get(i);
            boolean ok = selectedIndex(rg) == q.answer;
            if (ok) correct++;

            // Lock the answers and colour them
            for (int c = 0; c < rg.getChildCount(); c++) {
                RadioButton rb = (RadioButton) rg.getChildAt(c);
                rb.setEnabled(false);
                int idx = (Integer) rb.getTag();
                if (idx == q.answer) rb.setTextColor(Color.parseColor("#1B873F"));
                else if (rb.isChecked()) rb.setTextColor(Color.parseColor("#C62828"));
            }
            if (!ok) {
                TextView why = new TextView(this);
                why.setText("✗ Correct: " + q.options[q.answer] + "\n" + q.why);
                why.setTextSize(13);
                why.setTextColor(Color.parseColor("#7A4B00"));
                why.setPadding(0, dp(8), 0, 0);
                ((LinearLayout) rg.getParent()).addView(why);
            }
        }

        if (correct == questions.size()) {
            unlockAndOpen();
        } else {
            showingResults = true;
            LockPrefs.setCooldownUntil(this, cooldownKey(), System.currentTimeMillis() + COOLDOWN_MS);
            Toast.makeText(this, correct + "/" + QUESTION_COUNT + " correct. Locked for 12 minutes.", Toast.LENGTH_LONG).show();
            submit.setText("Locked for 12 min (" + correct + "/" + QUESTION_COUNT + ") — see timer");
        }
    }

    private void unlockAndOpen() {
        Toast.makeText(this, "5/5 — unlocked!", Toast.LENGTH_SHORT).show();
        if (targetPkg == null) { finish(); return; }   // test mode from main screen
        LockAccessibilityService.unlockedPackage = targetPkg;
        Intent launch = getPackageManager().getLaunchIntentForPackage(targetPkg);
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launch);
        }
        finish();
    }

    private void goHome() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(home);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Back must not reveal the locked app underneath.
        if (targetPkg == null) { super.onBackPressed(); return; }
        goHome();
    }
}
