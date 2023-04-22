package com.example.gpstracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;

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
    private Button button;

    private LocationBody lastSentLocation;
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

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                updateUI(locationResult.getLastLocation());
                save(locationResult.getLastLocation());
            }
        };

        button = findViewById(R.id.button);

        swLocationUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(swLocationUpdates.isChecked()) {
                    startLocationUpdates();
                } else {
                    stopLocationUpdates();
                }
            }
        });
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "granted", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "not granted", Toast.LENGTH_LONG).show();
        }

//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                save();
//            }
//        });
        updateGps();
    }
    public void save(Location location) {
        //LocalDateTime localDateTime = LocalDateTime.now();
        //Toast.makeText(this, localDateTime.toString(), Toast.LENGTH_LONG).show();
        if(retrofit == null || api == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://gpstracker.herokuapp.com/")
                    .addConverterFactory(GsonConverterFactory.create()).build();

            api = retrofit.create(APIService.class);
        }
        Call<LocationBody> call = api.test1(location.getLongitude(), location.getLatitude(),
                location.getAltitude(), location.getSpeed(), location.getBearing());
        call.enqueue(new Callback<LocationBody>() {
            @Override
            public void onResponse(Call<LocationBody> call, Response<LocationBody> response) {
                //tvBearing.setText(response.message());
                //tvBearing.setText(response.code());
//                LocationBody responseFromAPI = response.body();
//                Toast.makeText(MainActivity.this, String.valueOf(responseFromAPI.longitude), Toast.LENGTH_LONG).show();
                //tvBearing.setText(String.valueOf(response.body().longitude));
                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<LocationBody> call, Throwable t) {
                //tvBearing.setText(t.getLocalizedMessage());
                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();

            }
        });

        //return new LocationBody(location, localDateTime);
    }
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        updateGps();
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

        if(requestCode == PERMISSION_FINE_LOCATION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            updateGps();
        } else {
            Toast.makeText(this, "No GPS permissions", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void updateGps() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateUI(location);
                    //save(location);
                }
            });
        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.INTERNET}, PERMISSION_FINE_LOCATION);
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