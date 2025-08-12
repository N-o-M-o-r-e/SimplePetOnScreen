package com.nomore.example.simplepetonscreen.service

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.random.Random

class PetView(context: Context) : View(context) {

    // Pet properties
    var petX: Float = 100f
    var petY: Float = 200f
    private var velocityX: Float = 2f
    private var velocityY: Float = 1f
    private var isDragging = false

    // Screen boundaries
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    // Animation
    private var animationFrame = 0
    private var frameCounter = 0

    // Drawing
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val petSize = 80f

    // Gesture detection
    private val gestureDetector = GestureDetector(context, PetGestureListener())

    init {
        getScreenSize()
        setupPaint()
    }

    private fun getScreenSize() {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            screenWidth = bounds.width()
            screenHeight = bounds.height()
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            screenWidth = displayMetrics.widthPixels
            screenHeight = displayMetrics.heightPixels
        }
    }

    private fun setupPaint() {
        paint.color = Color.parseColor("#FF6B6B")
        paint.style = Paint.Style.FILL
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(120, 120) // Fixed size for consistent touch area
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawPet(canvas)
    }

    private fun drawPet(canvas: Canvas) {
        val centerX = petSize / 2
        val centerY = petSize / 2

        when (animationFrame) {
            0 -> {
                // Basic circle pet
                canvas.drawCircle(centerX, centerY, petSize / 3, paint)

                // Eyes
                paint.color = Color.WHITE
                canvas.drawCircle(centerX - 10, centerY - 8, 6f, paint)
                canvas.drawCircle(centerX + 10, centerY - 8, 6f, paint)

                // Eye pupils
                paint.color = Color.BLACK
                canvas.drawCircle(centerX - 10, centerY - 8, 3f, paint)
                canvas.drawCircle(centerX + 10, centerY - 8, 3f, paint)

                // Mouth
                paint.color = Color.BLACK
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                canvas.drawArc(
                    centerX - 8, centerY, centerX + 8, centerY + 12,
                    0f, 180f, false, paint
                )

                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#FF6B6B")
            }
            1 -> {
                // Slightly squished (walking animation)
                canvas.save()
                canvas.scale(1.1f, 0.9f, centerX, centerY)
                canvas.drawCircle(centerX, centerY, petSize / 3, paint)
                canvas.restore()

                // Eyes
                paint.color = Color.WHITE
                canvas.drawCircle(centerX - 10, centerY - 5, 6f, paint)
                canvas.drawCircle(centerX + 10, centerY - 5, 6f, paint)

                paint.color = Color.BLACK
                canvas.drawCircle(centerX - 10, centerY - 5, 3f, paint)
                canvas.drawCircle(centerX + 10, centerY - 5, 3f, paint)

                paint.color = Color.parseColor("#FF6B6B")
            }
            2 -> {
                // Bouncing animation
                canvas.drawCircle(centerX, centerY - 3, petSize / 3, paint)

                // Happy eyes
                paint.color = Color.BLACK
                paint.strokeWidth = 2f
                paint.style = Paint.Style.STROKE

                // Curved happy eyes
                canvas.drawArc(
                    centerX - 15, centerY - 15, centerX - 5, centerY - 5,
                    30f, 120f, false, paint
                )
                canvas.drawArc(
                    centerX + 5, centerY - 15, centerX + 15, centerY - 5,
                    30f, 120f, false, paint
                )

                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#FF6B6B")
            }
        }
    }

    fun updateAnimation() {
        if (!isDragging) {
            // Update position
            petX += velocityX
            petY += velocityY

            // Bounce off screen edges
            if (petX <= 0 || petX >= screenWidth - 120) {
                velocityX = -velocityX
            }
            if (petY <= 0 || petY >= screenHeight - 120) {
                velocityY = -velocityY
            }

            // Keep within bounds
            petX = petX.coerceIn(0f, (screenWidth - 120).toFloat())
            petY = petY.coerceIn(0f, (screenHeight - 120).toFloat())
        }

        // Update animation frame
        frameCounter++
        if (frameCounter >= 30) { // Change frame every 30 updates (~0.5 seconds)
            animationFrame = (animationFrame + 1) % 3
            frameCounter = 0
        }

        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDragging = true
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val newX = event.rawX - petSize / 2
                    val newY = event.rawY - petSize / 2

                    // Keep within screen bounds
                    petX = newX.coerceIn(0f, (screenWidth - 120).toFloat())
                    petY = newY.coerceIn(0f, (screenHeight - 120).toFloat())

                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                isDragging = false
                return true
            }
        }

        return true // Always consume touch events
    }

    private inner class PetGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            // Random jump on tap
            velocityX = Random.nextFloat() * 8 - 4 // -4 to 4
            velocityY = Random.nextFloat() * 8 - 4 // -4 to 4

            // Fun animation
            animationFrame = 2 // Happy face
            frameCounter = 0

            return true
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Teleport to random location on double tap
            petX = Random.nextFloat() * (screenWidth - 120)
            petY = Random.nextFloat() * (screenHeight - 120)

            // Reset velocity
            velocityX = Random.nextFloat() * 4 - 2
            velocityY = Random.nextFloat() * 4 - 2

            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            // Apply fling velocity (scaled down)
            this@PetView.velocityX = velocityX / 200
            this@PetView.velocityY = velocityY / 200

            return true
        }
    }
}