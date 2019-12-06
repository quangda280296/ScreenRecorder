package com.vmb.ads_in_app.Interface;

import com.vmb.ads_in_app.LibrayData;
import com.vmb.ads_in_app.model.AdsConfig;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IAPIControl {

    @GET(LibrayData.Url.URL_API_CONTROL_ADS)
    Call<AdsConfig> getAds(@Query("deviceID") String deviceID,
                           @Query("code") String code,
                           @Query("version") String version,
                           @Query("country") String country,
                           @Query("timereg") String timereg,
                           @Query("packg") String packg,
                           @Query("phone_name") String phone_name,
                           @Query("os_version") String os_version);
}
