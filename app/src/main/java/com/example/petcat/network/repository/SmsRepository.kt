package com.example.petcat.network.repository

import com.example.petcat.network.model.HeartModel
import com.example.petcat.network.model.SmsResponse
import com.example.petcat.utils.RetrofitHelper
import retrofit2.Call

class SmsRepository {

    fun getLoginFun (userid: String, key: String, mobile: String): Call<SmsResponse> {
        return RetrofitHelper.api.postLogin(userid, key, mobile)
    }

    fun getPacketFun(heartModel: HeartModel) : Call<SmsResponse>{
        return RetrofitHelper.api.postPacket(heartModel)
    }
}