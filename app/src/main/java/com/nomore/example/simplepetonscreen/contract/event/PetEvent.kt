package com.nomore.example.simplepetonscreen.contract.event

import android.content.Intent

sealed class PetEvent {
    object ShowPermissionDialog : PetEvent()
    data class ShowToast(val message: String) : PetEvent()
    data class RequestOverlayPermission(val intent: Intent) : PetEvent()
}
