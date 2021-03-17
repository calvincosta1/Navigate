package com.example.navigate.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.navigate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.Map;

public class Locations extends AppCompatActivity
{
    // Firebase variables
    FirebaseAuth fAuth;
    String userID;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);

        // Initialize Firebase authentication
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        // Create the AccountHeader
        com.example.navigate.Activities.Map.headerResult = new AccountHeaderBuilder()
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
        com.example.navigate.Activities.Map.result = new DrawerBuilder()
                .withAccountHeader(com.example.navigate.Activities.Map.headerResult)
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withFullscreen(true)
                .withActionBarDrawerToggle(true)
                .withToolbar(toolbar)
                .withSelectedItem(2)
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
                                Intent intent = new Intent(Locations.this , Map.class);
                                startActivity(intent);
                            }
                            else if (drawerItem.getIdentifier() == 2)
                            {
                                Intent intent = new Intent(Locations.this, UserHistory.class);
                                startActivity(intent);
                            }
                            else if (drawerItem.getIdentifier() == 3)
                            {
                                Intent intent = new Intent(Locations.this, Settings.class);
                                startActivity(intent);
                            }
                            else if (drawerItem.getIdentifier() == 4)
                            {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(Locations.this, Login.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        }
                        return false;
                    }
                })
                .build();
    }

    // Method to disable back button press
    @Override
    public void onBackPressed()
    {

    }
}