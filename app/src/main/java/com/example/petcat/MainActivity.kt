package com.example.petcat

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.petcat.databinding.ActivityMainBinding
import com.example.petcat.network.model.HeartModel
import com.example.petcat.network.repository.SmsRepository
import com.example.petcat.service.Md5
import com.example.petcat.ui.WebActivity
import com.example.petcat.ui.viewmodel.SmsViewModel
import com.example.petcat.ui.viewmodel.SmsViewModelProviders
import com.example.petcat.utils.Constant.REQUEST_PHONE_STATE_PERMISSION
import com.example.petcat.utils.Constant.REQUEST_SMS_PERMISSION


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var keyReceived = false
    private lateinit var smsViewModel: SmsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        smsPermissionHelper()
        phoneStatePermissionHelper()

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val savedUserId = sharedPreferences.getString("userid", "")
        val savedKey = sharedPreferences.getString("key", "")
//        val savedMobile = sharedPreferences.getString("mobile", "")

        binding.editTextID.setText(savedUserId)
        binding.editTextKey.setText(savedKey)
//        binding.editTextMobileNum.setText(savedMobile)

        if (!savedUserId.isNullOrBlank() && !savedKey.isNullOrBlank()) {
            val intent = Intent(this, WebActivity::class.java)
            startActivity(intent)
            finish()
        }

        val smsRepository = SmsRepository()
        val viewModelProviderFactory = SmsViewModelProviders(smsRepository, application)
        smsViewModel = ViewModelProvider(this, viewModelProviderFactory)[SmsViewModel::class.java]
        setContentView(binding.root)
        inputUserDataFun()
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    private fun inputUserDataFun() {

        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val phoneNumber = telephonyManager.line1Number

        binding.submitButton.setOnClickListener {
            val userid = binding.editTextID.text.toString()
            val key = binding.editTextKey.text.toString()
//            val mobile = binding.editTextMobileNum.text.toString()


            if (userid.isNotEmpty() && key.isNotEmpty()) {
                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("userid", userid)
                editor.putString("key", key)
//                editor.putString("mobile", mobile)
                editor.apply()
                smsViewModel.getLoginValue(userid, key, phoneNumber.toString())

                val currentTimeMillis = System.currentTimeMillis().toString()
                val sign = smsViewModel.calculateSign(userid, currentTimeMillis, key)
                if (sign.isNotEmpty()) {
                    val heartModel = HeartModel(user_id = userid, timestamp = currentTimeMillis, sign = sign)
                    smsViewModel.getPacketValue(heartModel)
                    Log.d("signValue","value of : $sign")
                } else {
                 Log.d("FailedPocket","Unable to get sign key")
                }
            }
        }

        smsViewModel.getLoginLivedata().observe(this) { response ->
//            val mobile = binding.editTextMobileNum.text.toString()
            if (response != null && response.code == 1) {
                keyReceived = true
                val intent = Intent(this, WebActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)

//                if (mobile != phoneNumber) {
//                    Toast.makeText(this, "Phone number and input number are not match", Toast.LENGTH_SHORT).show()
//                } else {
//
//                }
            } else {
                Toast.makeText(this, "Invalid key or cellphone number", Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun smsPermissionHelper() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECEIVE_SMS
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS),
                REQUEST_SMS_PERMISSION
            )
        }
        return
    }

    private fun phoneStatePermissionHelper() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                REQUEST_PHONE_STATE_PERMISSION
            )
        }

        getTelephoneDataFun()

    }

    @SuppressLint("MissingPermission", "HardwareIds")
    private fun getTelephoneDataFun() {
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val phoneNumber = telephonyManager.line1Number
        Log.d("phoneNumber", phoneNumber.toString())
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_SMS_PERMISSION && requestCode == REQUEST_PHONE_STATE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                smsPermissionHelper()
                phoneStatePermissionHelper()

            } else {
                Toast.makeText(
                    this,
                    "Permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}