package com.nomore.example.simplepetonscreen.contract.model

data class PermissionStatus(
    val hasOverlayPermission: Boolean,
    val shouldShowRationale: Boolean = false
)
