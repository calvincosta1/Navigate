package com.example.navigate.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.navigate.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity
{
    // Variables
    TextView login;
    EditText email;
    EditText pass;
    Button log;
    ProgressBar pbLog;

    // Firebase variable
    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");
        setupUI(findViewById(R.id.loginView));

        // Typecasting
        login = findViewById(R.id.tvLogin);
        email = findViewById(R.id.etEmail);
        pass = findViewById(R.id.etPassword);
        log = findViewById(R.id.btnLogin);
        pbLog = findViewById(R.id.progressBarLog);

        // Initialize Firebase authentication
        fAuth = FirebaseAuth.getInstance();
    }

    // Method to validate login info
    public void login(View view)
    {
        // Will control the login button click event
        // If the user clicks on the btn widget go get the info
        // from the edit texts and use it to authenticate

        if(view.getId() == R.id.btnLogin)
        {
            // Get the info from the edit texts
            String em = email.getText().toString().trim();
            String pas = pass.getText().toString().trim();

            // Using a text utility for validation control
            if(TextUtils.isEmpty(em))
            {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if(TextUtils.isEmpty(pas))
            {
                Toast.makeText(this, "Enter a valid password", Toast.LENGTH_SHORT).show();
                return;
            }

            if(pass.length()<6)
            {
                // Then reset the widgets
                pass.setText(null);
                Toast.makeText(this, "Password min is 6 chars", Toast.LENGTH_SHORT).show();
                return;
            }

            pbLog.setVisibility(view.VISIBLE);

            fAuth.signInWithEmailAndPassword(em, pas).addOnCompleteListener(new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if(task.isSuccessful())
                    {
                        Intent intent = new Intent(Login.this, Map.class);
                        startActivity(intent);
                        // Don't forget to stop the timer
                        finish();

                        Toast.makeText(Login.this, "Welcome " + fAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        pbLog.setVisibility(view.INVISIBLE);
                        Toast.makeText(Login.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Method to load registration activity
    public void reg(View view)
    {
        Intent intent = new Intent(Login.this, Register.class);
        startActivity(intent);

        // Don't forget to stop the timer
        finish();
    }

    // Method to disable back button press
    @Override
    public void onBackPressed()
    {

    }

    // Method to hide soft keyboard on activity load
    public static void hideSoftKeyboard(Activity activity)
    {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        //inputMethodManager.hideSoftInputFromWindow(
        //        activity.getCurrentFocus().getWindowToken(), 0);
    }

    // Method to add touch anywhere to dismiss keyboard
    public void setupUI(View view)
    {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText))
        {
            view.setOnTouchListener(new View.OnTouchListener()
            {
                public boolean onTouch(View v, MotionEvent event)
                {
                    hideSoftKeyboard(Login.this);
                    return false;
                }
            });
        }

        // If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup)
        {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
            {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }
}