package com.nomore.example.simplepetonscreen.contract.state

data class PetState(
    val isServiceRunning: Boolean = false,
    val isLoading: Boolean = false,
    val hasOverlayPermission: Boolean = false,
    val buttonText: String = "Start Pet",
    val error: String? = null
)
