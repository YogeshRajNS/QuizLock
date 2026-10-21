# QuizLock

Lock any Android app behind 5 random RAG / GenAI / Agentic AI questions.
Answer all 5 correctly → the app opens. Get any wrong → you see the right answers with explanations, then that app is locked for 12 minutes (countdown screen, no questions). After 12 minutes you get a fresh set of 5.

## Get the APK (no Android Studio needed)
1. Create a new GitHub repo and upload everything in this folder (keep the `.github` folder).
2. Open the repo's **Actions** tab → "Build QuizLock APK" runs automatically (or click **Run workflow**).
3. When it finishes (~3–4 min), open the run → download **QuizLock-apk** → unzip → `app-debug.apk`.

## Or with Android Studio
Open this folder → let it sync → **Build > Build APK(s)**.

## Set up on the phone
1. Install the APK (allow "Install unknown apps" for your browser/file manager).
2. Open QuizLock → **Open Accessibility settings** → Installed apps → **QuizLock app guard** → ON.
   - Android 13+: if the toggle is greyed out, go to Settings → Apps → QuizLock → ⋮ → **Allow restricted settings**, then try again.
3. Back in QuizLock, tick the apps you want to lock.
4. Turn off battery optimisation for QuizLock so the service isn't killed (Settings → Apps → QuizLock → Battery → Unrestricted).

## How it behaves
- Opening a locked app shows the quiz on top of it. Back / Cancel sends you to the home screen.
- After passing, the app stays unlocked until you switch to another app or go home.
- Questions and answer options are shuffled every time; each set mixes all three topics.

## Add your own questions
Edit `app/src/main/java/com/yogesh/quizlock/QuestionBank.java` → `build()`:
```java
q.add(new Question(R, "Your question?", 0 /* index of correct option */,
        "Explanation shown when wrong",
        "Correct option", "Wrong 1", "Wrong 2", "Wrong 3"));
```
Change `QUESTION_COUNT` (number of questions) or `COOLDOWN_MS` (lockout after a wrong answer) in `QuizActivity.java`.
