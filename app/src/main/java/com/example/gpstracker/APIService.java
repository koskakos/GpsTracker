package com.example.gpstracker;

import android.location.Location;

import java.time.LocalDateTime;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIService {
    @FormUrlEncoded
    @POST("save")
    Call<Location> save(@Field("longitude") double longitude,
                        @Field("latitude") double latitude,
                        @Field("altitude") double altitude,
                        @Field("speed") double speed,
                        @Field("bearing") double bearing,
                        @Field("date") LocalDateTime date);
}
