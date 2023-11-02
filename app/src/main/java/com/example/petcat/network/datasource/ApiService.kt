package com.example.petcat.network.datasource


import com.example.petcat.network.model.HeartModel
import com.example.petcat.network.model.SmsBody
import com.example.petcat.network.model.SmsResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {


    @FormUrlEncoded
    @POST("api/auto/smsloginnew")
    fun postLogin(
        @Field("userId") userId: String,
        @Field("key") key: String,
        @Field("mobile") mobile : String
    ): Call<SmsResponse>

    @POST("api/auto/sms")
    fun postSms(
        @Body smsBody: SmsBody
    ): Call<SmsResponse>

    @POST("api/auto/smsheart")
    fun postPacket(
        @Body heartModel: HeartModel
    ): Call<SmsResponse>

}