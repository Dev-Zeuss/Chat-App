package com.zeus.chatme.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    String yourApiKey =""; // your firebase cloud messaging key
    @Headers( {
            "Content-Type:application/json",
            "Authorization:key=" + yourApiKey

    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
