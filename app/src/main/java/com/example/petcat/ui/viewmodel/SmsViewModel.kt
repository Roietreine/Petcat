package com.example.petcat.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.petcat.network.model.HeartModel
import com.example.petcat.network.model.SmsResponse
import com.example.petcat.network.repository.SmsRepository
import com.example.petcat.service.Md5
import com.example.petcat.utils.Constant.NETWORK_FAILED
import com.example.petcat.utils.Constant.PACKET_SUCESS_CONNECT
import com.example.petcat.utils.Constant.SUCCESS_CONNECTION
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SmsViewModel(
    app: Application,
    val smsRepository: SmsRepository
) : AndroidViewModel(app) {

    val data = MutableLiveData<HeartModel>()

    private val loginResponseMutableLiveData: MutableLiveData<SmsResponse> = MutableLiveData()

    private val heartPacketMutableLiveData : MutableLiveData<SmsResponse> = MutableLiveData()

    fun getHeartPacketLiveData() : LiveData<SmsResponse>{
        return  heartPacketMutableLiveData
    }
    fun getLoginLivedata(): LiveData<SmsResponse> {
        return loginResponseMutableLiveData
    }

    fun getLoginValue(userid: String, key: String, mobile: String) {
        val call = smsRepository.getLoginFun(userid,key,mobile)
        call.enqueue(object : Callback<SmsResponse> {
            override fun onResponse(call: Call<SmsResponse>, response: Response<SmsResponse>) {
                if (response.isSuccessful) {
                    loginResponseMutableLiveData.value = response.body()

                    Log.d(SUCCESS_CONNECTION,response.body().toString())

                }
            }

            override fun onFailure(call: Call<SmsResponse>, t: Throwable) {
                t.localizedMessage?.let { Log.d(NETWORK_FAILED, it) }
            }
        })
    }

    fun getPacketValue(heartModel: HeartModel){
        val call = smsRepository.getPacketFun(heartModel)
        call.enqueue(object : Callback<SmsResponse>{
            override fun onResponse(call: Call<SmsResponse>, response: Response<SmsResponse>) {
                if(response.isSuccessful){
                    heartPacketMutableLiveData.value = response.body()

                    Log.d(PACKET_SUCESS_CONNECT,response.body().toString())
                }
            }

            override fun onFailure(call: Call<SmsResponse>, t: Throwable) {
                t.localizedMessage?.let { Log.d(NETWORK_FAILED, it) }
            }

        })
    }


    fun calculateSign(user_id: String, timestamp: String, key: String): String {
        val dataToSign = "${user_id}${timestamp}$key"
        return Md5.md5(dataToSign)
    }
}