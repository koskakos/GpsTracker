package com.example.gpstracker;

import java.time.LocalDateTime;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface APIService {
    @FormUrlEncoded
    @POST("test")
    Call<LocationBody> test(@Field("longitude") double longitude);


    @FormUrlEncoded
    @POST("test1")
    Call<LocationBody> test1(@Field("longitude") double longitude,
                             @Field("latitude") double latitude,
                             @Field("altitude") double altitude,
                             @Field("speed") double speed,
                             @Field("bearing") double bearing);


    @FormUrlEncoded
    @POST("save")
    Call<LocationBody> save(@Field("longitude") double longitude,
                            @Field("latitude") double latitude,
                            @Field("altitude") double altitude,
                            @Field("speed") double speed,
                            @Field("bearing") double bearing,
                            @Field("date") String date);
}
