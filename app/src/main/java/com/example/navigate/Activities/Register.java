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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.navigate.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class Register extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    // Variables
    EditText name;
    EditText email;
    EditText password;
    Button reg;
    ProgressBar pbReg;
    Spinner spinnerMode;
    Spinner spinnerUnit;

    // Firebase variables
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    // User preferences variables
    String choiceMode;
    String choiceUnit;

    // Spinner array variables
    String[] mode = {"Driving","Cycling","Walking"};
    String[] unit = {"Metric","Imperial"};

    // Log TAG variable
    public static final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("Register");
        setupUI(findViewById(R.id.registerView));

        // Typecasting
        name = findViewById(R.id.etName);
        email = findViewById(R.id.etRegEmail);
        password = findViewById(R.id.etRegPassword);
        reg = findViewById(R.id.btnRegister);
        pbReg = findViewById(R.id.progressBarReg);
        spinnerMode = findViewById(R.id.spinTrans);
        spinnerUnit = findViewById(R.id.spinUnit);

        // Initialize Firebase authentication
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // Spinner
        ArrayAdapter<String> aa = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mode);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMode.setAdapter(aa);
        spinnerMode.setOnItemSelectedListener(this);

        // Spinner
        ArrayAdapter<String> bb = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, unit);
        bb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(bb);
        spinnerUnit.setOnItemSelectedListener(this);

    }

    // Spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        Spinner spinnerMode = (Spinner)parent;
        Spinner spinnerUnit = (Spinner)parent;

        if(spinnerMode.getId() == R.id.spinTrans)
        {
            choiceMode = mode[position];
        }
        if(spinnerUnit.getId() == R.id.spinUnit)
        {
            choiceUnit = unit[position];
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }

    // Method to navigate to Login activity
    public void log(View view)
    {
        Intent intent = new Intent(Register.this, Login.class);
        startActivity(intent);

        // Don't forget to stop the timer
        finish();
    }

    // Method to handle registration
    public void register(View view)
    {
        if (view.getId() == R.id.btnRegister)
        {
            String em = email.getText().toString().trim();
            String pa = password.getText().toString().trim();
            String fname = name.getText().toString();

            // Using a text utility for validation control
            if(TextUtils.isEmpty(em))
            {
                Toast.makeText(this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if(TextUtils.isEmpty(pa))
            {
                Toast.makeText(this, "Enter a valid password", Toast.LENGTH_SHORT).show();
                return;
            }

            if(password.length() < 6)
            {
                // Then reset the widgets
                password.setText(null);
                Toast.makeText(this, "Password min is 6 chars", Toast.LENGTH_SHORT).show();
                return;
            }

            pbReg.setVisibility(view.VISIBLE);

            fAuth.createUserWithEmailAndPassword(em, pa).addOnCompleteListener(new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(Register.this, "Registration Successful", Toast.LENGTH_SHORT).show();

                        userID = fAuth.getCurrentUser().getUid();
                        DocumentReference documentReference = fStore.collection("users").document(userID);
                        Map<String, Object> user = new HashMap<>();
                        user.put("FullName", fname);
                        user.put("Email", em);
                        user.put("TransportMode", choiceMode);
                        user.put("Unit", choiceUnit);
                        documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>()
                        {
                            @Override
                            public void onSuccess(Void aVoid)
                            {
                                Timber.d("onSuccess");
                            }
                        }).addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Timber.d("onFailure: %s", e.toString());
                            }
                        });

                        Intent intent = new Intent(Register.this, Login.class);
                        startActivity(intent);
                        // Don't forget to stop the timer
                        finish();
                    }
                    else
                    {
                        pbReg.setVisibility(view.INVISIBLE);
                        Toast.makeText(Register.this, "Error! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Method to navigate to Login activity if the back button is pressed
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(Register.this, Login.class);
        startActivity(intent);
    }

    // Method to hide soft keyboard on activity load
    public static void hideSoftKeyboard(Activity activity)
    {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
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
                    hideSoftKeyboard(Register.this);
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