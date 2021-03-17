package com.example.navigate.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.navigate.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

public class Settings extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    // Variables
    EditText name;
    Button update;
    Spinner spinTransport;
    Spinner spinMeasure;
    String choiceMode;
    String choiceUnit;

    // Firebase variables
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    // Spinner array variables
    String[] mode = {"Driving","Cycling","Walking"};
    String[] unit = {"Metric","Imperial"};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupUI(findViewById(R.id.settingsView));

        // Typecasting
        name = findViewById(R.id.etName2);
        update = findViewById(R.id.button);
        spinTransport = findViewById(R.id.spinTrans2);
        spinMeasure = findViewById(R.id.spinUnit2);

        // Initialize Firebase authentication
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        // Create the AccountHeader
        Map.headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles(
                        new ProfileDrawerItem().withName(fAuth.getCurrentUser().getEmail())
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener()
                {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile)
                    {
                        return false;
                    }
                })
                .build();

        // Create the MaterialNavDrawer
        Map.result = new DrawerBuilder()
                .withAccountHeader(Map.headerResult)
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withFullscreen(true)
                .withActionBarDrawerToggle(true)
                .withToolbar(toolbar)
                .withSelectedItem(3)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Map").withIcon(R.drawable.ic_map).withIdentifier(1).withSelectable(true),
                        new PrimaryDrawerItem().withName("Locations").withIcon(R.drawable.ic_location).withIdentifier(2).withSelectable(true),
                        new PrimaryDrawerItem().withName("Settings").withIcon(R.drawable.ic_settings).withIdentifier(3).withSelectable(true),
                        new PrimaryDrawerItem().withName("Logout").withIcon(R.drawable.ic_logout).withIdentifier(4).withSelectable(true)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener()
                {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem)
                    {
                        if (drawerItem != null)
                        {
                            if (drawerItem.getIdentifier() == 1)
                            {
                                Intent intent = new Intent(Settings.this , Map.class);
                                startActivity(intent);
                            }
                            else if (drawerItem.getIdentifier() == 2)
                            {
                                Intent intent = new Intent(Settings.this, UserHistory.class);
                                startActivity(intent);
                            }
                            else if (drawerItem.getIdentifier() == 3)
                            {
                                Intent intent = new Intent(Settings.this, Settings.class);
                                startActivity(intent);
                            }
                            else if (drawerItem.getIdentifier() == 4)
                            {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(Settings.this, Login.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        }
                        return false;
                    }
                })
                .build();

        // Spinner
        ArrayAdapter<String> aa = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, mode);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinTransport.setAdapter(aa);
        spinTransport.setOnItemSelectedListener(this);

        // Spinner
        ArrayAdapter<String> bb = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, unit);
        bb.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinMeasure.setAdapter(bb);
        spinMeasure.setOnItemSelectedListener(this);

        userID = fAuth.getCurrentUser().getUid();
        DocumentReference documentReference = fStore.collection("users").document(userID);

        // Getting user info from DB
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                if(task.isSuccessful())
                {
                    String fieldMode;
                    String fieldMeasure;
                    DocumentSnapshot doc = task.getResult();
                    StringBuilder fieldFullName = new StringBuilder("");
                    fieldFullName.append(doc.get("FullName"));
                    name.setText(fieldFullName.toString());
                    fieldMode = doc.getString("TransportMode");
                    fieldMeasure = doc.getString("Unit");

                    // Setting mode spinner
                    if(fieldMode.equals("Driving"))
                    {
                        spinTransport.setSelection(0);
                    }
                    else if(fieldMode.equals("Cycling"))
                    {
                        spinTransport.setSelection(1);
                    }
                    else if(fieldMode.equals("Walking"))
                    {
                        spinTransport.setSelection(2);
                    }

                    // Setting measurement spinner
                    if(fieldMeasure.equals("Metric"))
                    {
                        spinMeasure.setSelection(0);
                    }
                    else if(fieldMeasure.equals("Imperial"))
                    {
                        spinMeasure.setSelection(1);
                    }
                }
            }
        });
    }

    // Spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        Spinner spinnerMode = (Spinner)parent;
        Spinner spinnerUnit = (Spinner)parent;

        if(spinnerMode.getId() == R.id.spinTrans2)
        {
            choiceMode = mode[position];
        }
        if(spinnerUnit.getId() == R.id.spinUnit2)
        {
            choiceUnit = unit[position];
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }

    // Method to update user info in DB
    public void update(View view)
    {
        DocumentReference documentReference = fStore.collection("users").document(userID);

        documentReference.update("FullName", name.getText().toString());
        documentReference.update("TransportMode", choiceMode);
        documentReference.update("Unit", choiceUnit);

        Toast.makeText(this, "Update Successful", Toast.LENGTH_SHORT).show();
    }

    // Method to hide soft keyboard on activity load
    public static void hideSoftKeyboard(Activity activity)
    {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        //inputMethodManager.hideSoftInputFromWindow(
                //activity.getCurrentFocus().getWindowToken(), 0);
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
                    hideSoftKeyboard(Settings.this);
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

    // Method to disable back button press
    @Override
    public void onBackPressed()
    {

    }
}