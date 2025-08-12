package com.nomore.example.simplepetonscreen.repository

import android.content.Intent
import com.nomore.example.simplepetonscreen.contract.model.PermissionStatus
import com.nomore.example.simplepetonscreen.contract.model.ServiceState

interface PetRepository {
    suspend fun checkOverlayPermission(): PermissionStatus
    suspend fun startPetService(): Result<ServiceState>
    suspend fun stopPetService(): Result<ServiceState>
    fun createOverlayPermissionIntent(): Intent
    suspend fun getCurrentServiceState(): ServiceState
}
