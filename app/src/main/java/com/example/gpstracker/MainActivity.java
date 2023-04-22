package com.example.gpstracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;

public class MainActivity extends AppCompatActivity implements LocListenerInterface {

    private TextView tvLatitude, tvLongitude, tvAltitude, tvSpeed, tvBearing;

    private Switch swGps;

    private Location lastLocation;
    private LocationManager locationManager;
    private LocListener locListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLatitude = findViewById(R.id.latitude);
        tvLongitude = findViewById(R.id.longitude);
        tvAltitude = findViewById(R.id.altitude);
        tvSpeed = findViewById(R.id.speed);
        tvBearing = findViewById(R.id.bearing);
        swGps = findViewById(R.id.gpsSwitch);
        init();
    }

    public void init() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locListener = new LocListener();
        locListener.setLocListenerInterface(this);
        checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults[0] == RESULT_OK) {
            checkPermissions();
        } else {
            checkPermissions();
        }
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, locListener);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLocationChanged(Location loc) {
        tvLongitude.setText("Longitude: " + loc.getLongitude());
        tvLatitude.setText("Latitude: " + loc.getLatitude());
        tvAltitude.setText("Altitude: " + loc.getAltitude());
        tvSpeed.setText("Speed: " + loc.getSpeed());
        tvBearing.setText("Bearing: " + loc.getBearing());
    }

}