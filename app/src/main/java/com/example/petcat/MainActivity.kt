package com.example.petcat

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import com.example.petcat.databinding.ActivityMainBinding
import com.example.petcat.network.model.HeartModel
import com.example.petcat.network.repository.SmsRepository
import com.example.petcat.ui.WebActivity
import com.example.petcat.ui.viewmodel.SmsViewModel
import com.example.petcat.ui.viewmodel.SmsViewModelProviders
import com.example.petcat.utils.Constant.REQUEST_CODE


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var keyReceived = false
    private var phoneNumbers = false
    private var backToExit = false
    var targetSimCardFound = false
    private lateinit var smsViewModel: SmsViewModel


    private val permissions = arrayOf(
        Manifest.permission.READ_PHONE_NUMBERS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

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
        if (!sharedPreferences.getBoolean("permissionsRequested", false)) {
            requestPermissions()
        } else {
            inputUserDataFun()
        }

    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
    }


    @SuppressLint("MissingPermission", "HardwareIds")
    private fun inputUserDataFun() {

        val phoneNumber = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val subscriptionManager =  getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager

        val infoList: List<SubscriptionInfo> = subscriptionManager.activeSubscriptionInfoList
        val targetSimSlotIndex = 1


        for (i in infoList.indices) {
            val info = infoList[i]
            info.simSlotIndex
            info.number
            Log.d("phoneListInfo",info.number.toString())
            Log.d("phoneSlotList",info.simSlotIndex.toString())
        }

        binding.submitButton.setOnClickListener {
            val userid = binding.editTextID.text.toString()
            val key = binding.editTextKey.text.toString()

            if (infoList.isEmpty()) {
                // No active subscriptions
                Toast.makeText(this, "No sim card found", Toast.LENGTH_SHORT).show()
                Log.d("phoneListInfo", "No active subscriptions found")
            } else {
                for (info in infoList) {
                    if (info.simSlotIndex == targetSimSlotIndex) {
                        // Found the target SIM card
                        val phoneNumber = info.number
                        smsViewModel.getLoginValue(userid, key, phoneNumber.toString())
                        Log.d("simcard1", "correct sim card number")
                        Log.d("simcard1", phoneNumber ?: "Phone number not available")
                        targetSimCardFound = true
                        break
                    }
                }
            }
// If the first target SIM card was not found, check the second one
            if (!targetSimCardFound) {
                val secondTargetSimSlotIndex = 0 // 0 for SIM 1, 1 for SIM 2

                for (info in infoList) {
                    if (info.simSlotIndex == secondTargetSimSlotIndex) {
                        // Found the second target SIM card
                        val phoneNumber = info.number

                        smsViewModel.getLoginValue(userid, key, phoneNumber.toString())
                        Log.d("simcard2", phoneNumber ?: "Phone number not available")
                        Log.d("simcard2", "correct sim card number" )

                        break
                    }
                }
            }
            if (userid.isNotEmpty() && key.isNotEmpty()) {
                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("userid", userid)
                editor.putString("key", key)
                editor.apply()
                val currentTimeMillis = System.currentTimeMillis().toString()
                val sign = smsViewModel.calculateSign(userid, currentTimeMillis, key)
                if (sign.isNotEmpty()) {
                    val heartModel =
                        HeartModel(user_id = userid, timestamp = currentTimeMillis, sign = sign)
                    smsViewModel.getPacketValue(heartModel)
                    Log.d("signValue", "value of : $sign")
                } else {
                    Log.d("FailedPocket", "Unable to get sign key")
                }
            }
            smsViewModel.getLoginLivedata().observe(this) { response ->
//            val mobile = binding.editTextMobileNum.text.toString()
                if (response != null && response.code == 1) {
                    keyReceived = true
                    val intent = Intent(this, WebActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Invalid key or cellphone number", Toast.LENGTH_SHORT).show()
                    val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.remove("userid")
                    editor.remove("key")
                    editor.apply()
                }
            }
        }

    }


//    @SuppressLint("MissingPermission", "HardwareIds")
//    private fun getTelephoneDataFun() {
//        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
//        val phoneNumber = telephonyManager.line1Number
//        Log.d("phoneNumber", phoneNumber.toString())
//    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_CODE) {
            val grantedPermissions = ArrayList<String>()
            val deniedPermissions = ArrayList<String>()

            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    grantedPermissions.add(permissions[i])
                } else {
                    deniedPermissions.add(permissions[i])
                }
            }

            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            // Check if this is the first run of the app
            val isFirstRun = sharedPreferences.getBoolean("firstRun", true)

            if (isFirstRun) {
                // Handle first-run logic
                if (grantedPermissions.isNotEmpty()) {
                    // Permissions were granted on the first run
                    sharedPreferences.edit {
                        putBoolean("permissionsRequested", true)
                        putBoolean("firstRun", false)
                    }
                    // Proceed with your app logic, e.g., call inputUserDataFun()
                    inputUserDataFun()
                } else {
                    // Permissions were not granted on the first run
                    Toast.makeText(
                        this,
                        "Permission denied. This app requires these permissions to function.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            } else {
                // Handle non-first-run logic (e.g., re-request permissions)
                if (grantedPermissions.isNotEmpty()) {
                    // Permissions were granted
                    sharedPreferences.edit {
                        putBoolean("permissionsRequested", true)
                    }
                    // Proceed with your app logic, e.g., call inputUserDataFun()
                    inputUserDataFun()
                } else {

                    Toast.makeText(
                        this,
                        "Permission denied. This app requires these permissions to function.",
                        Toast.LENGTH_SHORT
                    ).show()
                        finish()
                }
            }
        }
    }
    override fun onStop() {
        super.onStop()
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            remove("permissionsRequested")
            commit()
        }
    }
    override fun onBackPressed() {
        if (backToExit) {
            super.finishAffinity()
            return
        }
        backToExit = true
        Toast.makeText(this, "Press back again to exit.", Toast.LENGTH_SHORT).show()
        Handler().postDelayed({ backToExit = false }, 2000)
    }

}