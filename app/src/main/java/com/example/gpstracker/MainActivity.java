package com.example.gpstracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_FINE_LOCATION = 100;
    private TextView tvLatitude, tvLongitude, tvAltitude, tvSpeed, tvBearing;

    private Switch swLocationUpdates;

    private LocationRequest locationRequest;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    private Retrofit retrofit;
    private APIService api;

    private Location lastSentLocation;
    private LocalDateTime lastSentDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLatitude = findViewById(R.id.latitude);
        tvLongitude = findViewById(R.id.longitude);
        tvAltitude = findViewById(R.id.altitude);
        tvSpeed = findViewById(R.id.speed);
        tvBearing = findViewById(R.id.bearing);
        swLocationUpdates = findViewById(R.id.locationUpdatesSwitch);
        locationRequest = new LocationRequest.Builder(1000).
                setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .build();
        if(!isHavePermissions()) requestPermissions();
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location currentLocation = locationResult.getLastLocation();
                updateUI(currentLocation);
                if (lastSentLocation == null) {
                    lastSentDateTime = save(currentLocation);
                    lastSentLocation = currentLocation;
                } else {
                    if (Math.abs(lastSentLocation.getBearing() - currentLocation.getBearing()) >= 10) {
                        Toast.makeText(MainActivity.this, "Bearing", Toast.LENGTH_LONG).show();
                    } else if (lastSentLocation.distanceTo(currentLocation) >= 200) {
                        Toast.makeText(MainActivity.this, "Distance", Toast.LENGTH_LONG).show();
                    } else if (Math.abs(lastSentDateTime.toEpochSecond(ZoneOffset.UTC) - LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) >= 600) {
                        Toast.makeText(MainActivity.this, "Time", Toast.LENGTH_LONG).show();
                    } else return;
                    lastSentDateTime = save(currentLocation);
                    lastSentLocation = currentLocation;
                }
            }
        };

        swLocationUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (swLocationUpdates.isChecked()) {
                    startLocationUpdates();
                } else {
                    stopLocationUpdates();
                }
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "granted", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "not granted", Toast.LENGTH_LONG).show();
        }

    }

    public LocalDateTime save(Location location) {
        LocalDateTime localDateTime = LocalDateTime.now();
        if (retrofit == null || api == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://gpstracker.herokuapp.com/")
                    .addConverterFactory(GsonConverterFactory.create()).build();

            api = retrofit.create(APIService.class);
        }
        Call<Location> call = api.save(location.getLongitude(), location.getLatitude(),
                location.getAltitude(), location.getSpeed(), location.getBearing(), localDateTime);
        call.enqueue(new Callback<Location>() {
            @Override
            public void onResponse(Call<Location> call, Response<Location> response) {

            }

            @Override
            public void onFailure(Call<Location> call, Throwable t) {

            }
        });

        return localDateTime;
    }

    public boolean isGeoDisabled() {
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean mIsGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean mIsNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean mIsGeoDisabled = !mIsGPSEnabled && !mIsNetworkEnabled;
        return mIsGeoDisabled;
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if(isGeoDisabled()) startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        updateGps();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void stopLocationUpdates() {
        tvLongitude.setText("Longitude: Not tracking");
        tvLatitude.setText("Latitude: Not tracking");
        tvAltitude.setText("Altitude: Not tracking");
        tvSpeed.setText("Speed: Not tracking");
        tvBearing.setText("Bearing: Not tracking");
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (!(requestCode == PERMISSION_FINE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "No GPS permissions", Toast.LENGTH_LONG).show();
            updateGps();
        }
    }

    private boolean isHavePermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, PERMISSION_FINE_LOCATION);
    }

    private void updateGps() {
        if (isHavePermissions()) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        } else {
            requestPermissions();
        }
    }

    private void updateUI(Location location) {
        tvLongitude.setText("Longitude: " + location.getLongitude());
        tvLatitude.setText("Latitude: " + location.getLatitude());
        tvAltitude.setText("Altitude: " + location.getAltitude());
        tvSpeed.setText("Speed: " + location.getSpeed());
        tvBearing.setText("Bearing: " + location.getBearing());
    }
}