package com.nomore.example.simplepetonscreen.service

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.nomore.example.simplepetonscreen.R
import com.nomore.example.simplepetonscreen.ui.main.MainActivity

class PetService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var petView: PetView
    private lateinit var layoutParams: WindowManager.LayoutParams
    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())

        if (!isRunning) {
            createPetView()
            startPetAnimation()
            isRunning = true
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::petView.isInitialized && petView.isAttachedToWindow) {
            windowManager.removeView(petView)
        }
        handler.removeCallbacksAndMessages(null)
        isRunning = false
    }

    private fun createPetView() {
        petView = PetView(this)

        // Simplified window layout - works on SDK 24+
        layoutParams = WindowManager.LayoutParams(
            120, // Fixed width
            120, // Fixed height
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 100
        layoutParams.y = 200

        windowManager.addView(petView, layoutParams)
    }

    private fun startPetAnimation() {
        val animationRunnable = object : Runnable {
            override fun run() {
                if (isRunning && ::petView.isInitialized) {
                    petView.updateAnimation()

                    // Update window position
                    layoutParams.x = petView.petX.toInt()
                    layoutParams.y = petView.petY.toInt()

                    try {
                        windowManager.updateViewLayout(petView, layoutParams)
                    } catch (e: Exception) {
                        // Handle case where view is removed
                    }

                    handler.postDelayed(this, 16) // ~60 FPS
                }
            }
        }

        handler.post(animationRunnable)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pet Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pet is running")
            .setContentText("Your pet is active on screen")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "PetServiceChannel"
    }
}