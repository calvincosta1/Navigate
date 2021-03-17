package com.example.navigate.Activities;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.navigate.Database.DBHelper;
import com.example.navigate.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

public class Map extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener
{
    // Mapbox variables
    private MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    private DirectionsRoute currentRoute;
    private NavigationMapRoute navigationMapRoute;
    private LatLng currentLocation;
    private static Point currentPoint, destinationPoint;
    private GeoJsonSource source;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private Button btnStart;
    private FloatingActionButton fabSearch;

    // Firebase variables
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String userID;

    // Material Drawer variables
    public static Drawer result = null;
    public static AccountHeader headerResult = null;

    // Log TAG variable
    private static final String TAG = "MapActivity";

    // User preferences variables
    String fieldMode;
    String fieldMeasure;

    // Creating variable to call DatabaseHelper javaclass
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Mapbox.getInstance(this, getString(R.string.access_token));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        btnStart = findViewById(R.id.btnStart);
        fabSearch = findViewById(R.id.fabSearch);

        db = new DBHelper(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        // Initialize Firebase authentication
        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        userID = fAuth.getCurrentUser().getUid();

        fabSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent search = searchForLocation();
                startActivityForResult(search, 1);
            }
        });

        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
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
        result = new DrawerBuilder()
                .withAccountHeader(headerResult)
                .withActivity(this)
                .withFullscreen(true)
                .withActionBarDrawerToggle(true)
                .withToolbar(toolbar)
                .withSelectedItem(1)
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
                                Intent intent = new Intent(Map.this , Map.class);
                                startActivity(intent);
                            }
                            else if (drawerItem.getIdentifier() == 2)
                            {
                                Intent intent = new Intent(Map.this, UserHistory.class);
                                startActivity(intent);
                            }
                            else if (drawerItem.getIdentifier() == 3)
                            {
                                Intent intent = new Intent(Map.this, Settings.class);
                                startActivity(intent);
                            }
                            else if (drawerItem.getIdentifier() == 4)
                            {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(Map.this, Login.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }
                        }
                        return false;
                    }
                })
                .build();

        DocumentReference documentReference = fStore.collection("users").document(userID);

        // Getting users preferred mode of transport and unit of measurement from DB
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>()
        {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task)
            {
                DocumentSnapshot doc = task.getResult();
                fieldMode = doc.getString("TransportMode");
                fieldMeasure = doc.getString("Unit");
            }
        });
    }

    // Method for search functionality
    private Intent searchForLocation()
    {
        Point pointOfProximity = Point.fromLngLat(currentLocation.getLongitude(), currentLocation.getLatitude());

        Intent sendThrough = new PlaceAutocomplete.IntentBuilder()
                .accessToken(Mapbox.getAccessToken())
                .placeOptions(PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#ffffff"))
                        .hint("Enter an Address")
                        .country(Locale.getDefault())
                        .proximity(pointOfProximity)
                        .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS,
                                GeocodingCriteria.TYPE_POI,
                                GeocodingCriteria.TYPE_PLACE)
                        .limit(5)
                        .build(PlaceOptions.MODE_CARDS))
                .build(Map.this);

        return sendThrough;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Map.RESULT_OK && requestCode == 1)
        {
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            if (mapboxMap != null)
            {
                Style style = mapboxMap.getStyle();
                if (style != null)
                {
                    GeoJsonSource geoJsonSource = style.getSourceAs(geojsonSourceLayerId);
                    if (geoJsonSource != null)
                    {
                        geoJsonSource.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    getCurrentLocation();

                    destinationPoint = (Point) selectedCarmenFeature.geometry();
                    LatLng destination = new LatLng(destinationPoint.latitude(), destinationPoint.longitude());

                    // Moves the camera to the destination address
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(destination)
                                    .zoom(15)
                                    .build()), 2000);

                    // Sets the style of the map
                    source = mapboxMap.getStyle().getSourceAs("destination-source-id");

                    if (source != null)
                    {
                        source.setGeoJson(Feature.fromGeometry(destinationPoint));
                    }

                    // Runs get route method which returns directions route JSON and overlays it on the map
                    getRoute(currentPoint, destinationPoint);

                    btnStart.setEnabled(true);
                    btnStart.setBackgroundResource(R.color.colorPrimaryNight);
                }
            }
        }
    }

    // Method to save data from the route to the DB
    private void saveRouteHistory()
    {
        // Variable used to determine if the trip could be saved
        boolean save;

        // Storing the current date
        Date cd = Calendar.getInstance().getTime();
        SimpleDateFormat ft= new SimpleDateFormat("yyyyMMdd HH:mm");
        String currDateTime = ft.format(cd);

        // Retrieve users lat and long co-ords for start and destination
        LatLng startLatLng = new LatLng(currentPoint.latitude(), currentPoint.longitude());
        LatLng destinationLatLng = new LatLng(destinationPoint.latitude(), destinationPoint.longitude());
        String transport = fieldMode;

        //Toast.makeText(this, "Route History Successfully Updated", Toast.LENGTH_SHORT).show();

        //Toast.makeText(this, userID, Toast.LENGTH_SHORT).show();

        // Save the record to the DB
        save = db.insertHist(userID,currDateTime,transport,startLatLng,destinationLatLng);

        // If the record was saved successfully, display a message
        //if(save)
        //{
        //    Toast.makeText(this, "Route saved", Toast.LENGTH_SHORT).show();
        //}
        //else
        //{
        //    Toast.makeText(this, "Route cannot be saved", Toast.LENGTH_SHORT).show();
        //}
    }

    private void getCurrentLocation()
    {
        currentLocation = new LatLng(mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude(),
                mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude());

        currentPoint = Point.fromLngLat(currentLocation.getLongitude(), currentLocation.getLatitude());
    }

    @SuppressWarnings( {"MissingPermission"})
    @Override
    public boolean onMapClick(@NonNull LatLng point)
    {
        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        Point originPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude());

        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
        if (source != null)
        {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }

        getRoute(originPoint, destinationPoint);
        btnStart.setEnabled(true);
        btnStart.setBackgroundResource(R.color.colorPrimaryNight);
        return true;
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap)
    {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(getString(R.string.navigation_guidance_day), new Style.OnStyleLoaded()
        {
            @Override
            public void onStyleLoaded(@NonNull Style style)
            {
                enableLocationComponent(style);
                addDestinationIconSymbolLayer(style);

                mapboxMap.addOnMapClickListener(Map.this);
                btnStart = findViewById(R.id.btnStart);
                btnStart.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        boolean simulateRoute = false;
                        NavigationLauncherOptions options = NavigationLauncherOptions.builder()
                                .directionsRoute(currentRoute)
                                .shouldSimulateRoute(simulateRoute)
                                .build();
                        // Call this method with Context from within an Activity
                        NavigationLauncher.startNavigation(Map.this, options);

                        try
                        {
                            saveRouteHistory();
                        }
                        catch (Exception e)
                        {
                            Toast.makeText(Map.this, "Could not save the route!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // Fetches the current user location on Map Ready
                currentLocation = new LatLng(mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude(),
                        mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude());
            }
        });
    }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle)
    {
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain)
    {
        Toast.makeText(this, "Location Permission needed for app", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted)
    {
        if (granted)
        {
            enableLocationComponent(mapboxMap.getStyle());
        }
        else
        {
            Toast.makeText(this, "Location Permission needed for app", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void getRoute(Point origin, Point destination)
    {
        this.destinationPoint = destination;
        getCurrentLocation();

        // Initialize Variables to store Profiles
        String unit = null;
        String type = null;

        // If statement sets correct Unit Preference for user
        if(fieldMeasure.equals("Metric"))
        {
            unit = DirectionsCriteria.METRIC;
        }
        else
        {
            unit = DirectionsCriteria.IMPERIAL;
        }

        // If statement sets correct Transport Preference for user
        if(fieldMode.equals("Driving"))
        {
            type = DirectionsCriteria.PROFILE_DRIVING;
        }
        else if(fieldMode.equals("Cycling"))
        {
            type = DirectionsCriteria.PROFILE_CYCLING;
        }
        else
        {
            type = DirectionsCriteria.PROFILE_WALKING;
        }

        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .profile(type)
                .voiceUnits(unit)
                .build()
                .getRoute(new Callback<DirectionsResponse>()
                {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response)
                    {
                        Log.d(TAG, "Response : " + response.code());
                        if (response.body() == null)
                        {
                            Log.e(TAG, "No routes found, check access_token.");
                            return;
                        }
                        else if (response.body().routes().size() < 1)
                        {
                            Log.e(TAG, "No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

                        // Draw route on map
                        if (navigationMapRoute != null)
                        {
                            navigationMapRoute.removeRoute();
                        }
                        else
                        {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t)
                    {
                        Log.e(TAG, "error " + t.getMessage());
                    }
                });
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle)
    {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this))
        {
            // Activate the Mapbox Map LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
        }
        else
        {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressWarnings( {"MissingPermission"})
    @Override
    protected void onStart()
    {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    // Method to disable back button press
    @Override
    public void onBackPressed()
    {

    }
}