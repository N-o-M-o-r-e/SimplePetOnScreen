package com.nomore.example.simplepetonscreen.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import com.nomore.example.simplepetonscreen.contract.model.PermissionStatus
import com.nomore.example.simplepetonscreen.contract.model.ServiceState
import com.nomore.example.simplepetonscreen.service.PetService

class PetRepositoryImpl(private val context: Context) : PetRepository {

    override suspend fun checkOverlayPermission(): PermissionStatus {
        return try {
            val hasPermission = Settings.canDrawOverlays(context)
            PermissionStatus(
                hasOverlayPermission = hasPermission, shouldShowRationale = !hasPermission
            )
        } catch (e: Exception) {
            PermissionStatus(
                hasOverlayPermission = false, shouldShowRationale = false
            )
        }
    }

    override suspend fun startPetService(): Result<ServiceState> {
        return try {
            // Kiểm tra quyền trước khi start
            val permissionStatus = checkOverlayPermission()
            if (!permissionStatus.hasOverlayPermission) {
                return Result.failure(
                    SecurityException("Overlay permission required")
                )
            }

            // Start service
            val intent = Intent(context, PetService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }

            val serviceState = ServiceState(
                isRunning = true, canStart = false
            )

            Result.success(serviceState)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun stopPetService(): Result<ServiceState> {
        return try {
            val intent = Intent(context, PetService::class.java)
            context.stopService(intent)

            val serviceState = ServiceState(
                isRunning = false, canStart = true
            )

            Result.success(serviceState)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun createOverlayPermissionIntent(): Intent {
        return Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            data = "package:${context.packageName}".toUri()
        }
    }

    override suspend fun getCurrentServiceState(): ServiceState {
        // Logic để check xem service có đang chạy không
        return ServiceState(
            isRunning = false, canStart = true
        )
    }
}
