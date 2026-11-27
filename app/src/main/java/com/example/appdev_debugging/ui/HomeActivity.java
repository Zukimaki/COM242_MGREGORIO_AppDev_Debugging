package com.example.appdev_debugging.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appdev_debugging.R;
import com.example.appdev_debugging.ServiceLocator;
import com.example.appdev_debugging.data.model.User;
import com.example.appdev_debugging.session.SessionManager;

public class    HomeActivity extends AppCompatActivity{
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = ServiceLocator.getSessionManager(this);

        // A remembered session survives process death even though the demo repository is in-memory.
        String email = sessionManager.getLoggedInEmail();
        if (email == null) {
            goToLogin();
            return;
        }
        User user = ServiceLocator.getAuthManager().getUser(email);

        setContentView(R.layout.activity_home);

        TextView tvWelcome = findViewById(R.id.tvWelcome);
        TextView tvEmail = findViewById(R.id.tvEmail);
        Button btnLogout = findViewById(R.id.btnLogout);

        String displayName = user == null ? email.substring(0, email.indexOf('@')) : user.getFullName();
        String displayEmail = user == null ? email : user.getEmail();
        tvWelcome.setText(getString(R.string.home_welcome, displayName));
        tvEmail.setText(getString(R.string.home_email, displayEmail));

        // TODO(DEBUG #5): "Log out" sometimes doesn't really log the user out.
        //  Steps to reproduce (fix DEBUG #1 first):
        //    1. Log in WITHOUT ticking "Remember me". Tap "Log out" -> you see the Login screen. Correct.
        //    2. Log in WITH "Remember me" ticked. Tap "Log out" -> Login screen appears. Looks fine...
        //    3. Now swipe the app away from Recents (or Logcat "Terminate App") and open it again.
        //       You land on Home, still logged in, even though you logged out.
        //  Your tasks:
        //    a) Explain why the bug only appears when "Remember me" was ticked AND the app is restarted.
        //       (Think: where does the session live in each case: memory or disk?)
        //    b) Use Android Studio's Device Explorer to open
        //       /data/data/com.example.simplelogin/shared_prefs/ and inspect the XML file there
        //       before login, after login, and after logout. What do you see?
        //    c) Find and fix the root cause. Do NOT fix it by adding extra code here in HomeActivity;
        //       fix the class that is actually responsible for the session.
        // Answer
        // a) Without "Remember me," the session is stored only in temporary memory and disappears after logout.
        // With "Remember me," the session is saved in SharedPreferences, which remains even after closing the app.
        // If logout does not delete the saved email, the app will automatically log the user back in after restarting.
        // b) Before login, the XML file has no saved email. After logging in with "Remember me," the email is saved in the file.
        // However, after logout, the email is still there, showing that the app failed to remove the saved session.
        // c) Fixed by updating SessionManager.logout() to clear both the in-memory email (currentEmail) and remove the saved email from SharedPreferences (KEY_EMAIL), ensuring session data is completely removed upon logout.

        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            goToLogin();
        });
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

