package com.example.navigate.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.navigate.BuildConfig;
import com.example.navigate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity
{
    // Variables
    TextView navigate;
    ImageView img;
    TextView loading;
    TextView version;
    String versionName = BuildConfig.VERSION_NAME;
    ProgressBar pbSplash;

    // Timer
    long delay = 2500 ;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        setTitle("");

        // Typecasting
        img = findViewById(R.id.imageLogo);
        loading = findViewById(R.id.tvLoading);
        navigate = findViewById(R.id.tvHeading);
        version = findViewById(R.id.tvVersion);
        pbSplash = findViewById(R.id.progressBarSplash);

        version.setText("v" + versionName);

        // Create a handler object
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                // Bypasses the Login activity if a user is logged in already
                if (firebaseUser != null)
                {
                    startActivity(new Intent(SplashScreen.this, Map.class));
                }
                else
                {
                    Intent intent = new Intent(SplashScreen.this, Login.class);
                    startActivity(intent);

                    // Don't forget to stop the timer
                    finish();
                }
            }
        }, delay);
    }
}