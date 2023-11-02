package com.example.petcat.ui

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.motion.widget.MotionLayout
import com.example.petcat.MainActivity
import com.example.petcat.databinding.ActivitySplashscreenBinding

@SuppressLint("CustomSplashScreen")
class Splashscreen : AppCompatActivity() {
    private lateinit var binding: ActivitySplashscreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashscreenBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.splashEvents.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {}
            override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float) {}
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                startActivity(Intent(this@Splashscreen, MainActivity::class.java))
                finish()
            }
            override fun onTransitionTrigger(motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float) {}
        })
    }

}