package com.example.navigate.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.navigate.Database.Adapter;
import com.example.navigate.Database.DBHelper;
import com.example.navigate.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class UserHistory extends AppCompatActivity
{
    // Variables
    DBHelper db;
    private FirebaseAuth fAuth;
    private Context context;
    Button clearHist;
    private Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);

        context = this;

        fAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        db = new DBHelper(context);
        clearHist = findViewById(R.id.btnClear);
        FirebaseUser user = fAuth.getCurrentUser();
        String userID = user.getUid();

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
                                Intent intent = new Intent(UserHistory.this , Map.class);
                                startActivity(intent);
                            }
                            else if (drawerItem.getIdentifier() == 2)
                            {
                                Intent intent = new Intent(UserHistory.this, UserHistory.class);
                                startActivity(intent);
                            }
                            else if (drawerItem.getIdentifier() == 3)
                            {
                                Intent intent = new Intent(UserHistory.this, Settings.class);
                                startActivity(intent);
                            }
                            else if (drawerItem.getIdentifier() == 4)
                            {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(UserHistory.this, Login.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        }
                        return false;
                    }
                })
                .build();

        clearHist.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(db.deleteHist(userID))
                {
                    Toast.makeText(context, "History cleared.", Toast.LENGTH_SHORT).show();
                    finish();
                    Intent i = new Intent(UserHistory.this, UserHistory.class);
                    startActivity(i);
                }
                else
                {
                    Toast.makeText(context, "History could not be cleared.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Cursor cursor = db.getHistory(userID);

        if (cursor.getCount() != 0)
        {
            ArrayList<String> date = new ArrayList<>();
            ArrayList<String> trans = new ArrayList<>();
            ArrayList<String> startAdd = new ArrayList<>();
            ArrayList<String> endAdd = new ArrayList<>();

            // While the cursor has new rows, enter the info into the Array Lists
            while(cursor.moveToNext())
            {
                date.add(cursor.getString(1));

                trans.add(cursor.getString(2));

                String[] separated1 = cursor.getString(3).split(",");
                double latitudeE61 = Double.parseDouble(separated1[0]);
                double longitudeE61 = Double.parseDouble(separated1[1]);
                GeoPoint gp1 = new GeoPoint(latitudeE61, longitudeE61);

                String[] separated2 = cursor.getString(4).split(",");
                double latitudeE62 = Double.parseDouble(separated2[0]);
                double longitudeE62 = Double.parseDouble(separated2[1]);
                GeoPoint gp2 = new GeoPoint(latitudeE62, longitudeE62);

                String originAddress = "Address could not be found.";
                String destinationAddress = "Address could not be found.";

                geocoder = new Geocoder(context, Locale.getDefault());
                try
                {
                    originAddress = geocoder.getFromLocation(
                            gp1.getLatitude(), gp1.getLongitude(), 1)
                            .get(0).getAddressLine(0);
                    destinationAddress = geocoder.getFromLocation(
                            gp2.getLatitude(), gp2.getLongitude(), 1)
                            .get(0).getAddressLine(0);

                    startAdd.add(originAddress);
                    endAdd.add(destinationAddress);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            recycleView(date, trans, startAdd, endAdd);
        }
        else
        {
            Toast.makeText(this, "You have not recently visited any recent locations", Toast.LENGTH_SHORT).show();
        }
    }

    public void recycleView(ArrayList<String> keys, ArrayList<String> methods, ArrayList<String> originAdd, ArrayList<String> destAdd)
    {
        RecyclerView recyclerView;
        recyclerView = findViewById(R.id.rvHist);

        Adapter adapter = new Adapter(context, keys, methods, originAdd, destAdd);

        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }

    public void historyToMain(View view)
    {
        startActivity(new Intent(this,Map.class));
    }
}