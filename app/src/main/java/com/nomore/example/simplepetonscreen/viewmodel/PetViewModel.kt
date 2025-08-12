package com.nomore.example.simplepetonscreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nomore.example.simplepetonscreen.contract.action.PetAction
import com.nomore.example.simplepetonscreen.contract.event.PetEvent
import com.nomore.example.simplepetonscreen.contract.model.PermissionStatus
import com.nomore.example.simplepetonscreen.contract.state.PetState
import com.nomore.example.simplepetonscreen.repository.PetRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class PetViewModel(private val repository: PetRepository) : ViewModel() {

    private val _state = MutableStateFlow(PetState())
    val state: StateFlow<PetState> = _state.asStateFlow()

    private val _events = Channel<PetEvent>()
    val events = _events.receiveAsFlow()

    init {
        handleAction(PetAction.CheckPermission)
    }

    fun handleAction(action: PetAction) {
        when (action) {
            is PetAction.StartPet -> {
                setLoading()
                handleStartPet()
            }

            is PetAction.StopPet -> {
                setLoading()
                handleStopPet()
            }

            is PetAction.CheckPermission -> {
                setLoading()
                handleCheckPermission()
            }

            is PetAction.OnPermissionResult -> {
                _state.value = _state.value.copy(
                    hasOverlayPermission = action.hasPermission, isLoading = false
                )
                handlePermissionResult(action.hasPermission)
            }

            is PetAction.RequestPermission -> {
                setLoading()
                handleRequestPermission()
            }

            is PetAction.ClearError -> {
                _state.value = _state.value.copy(error = null)
            }
        }
    }

    private fun setLoading() {
        _state.value = _state.value.copy(isLoading = true, error = null)
    }

    private suspend fun showToast(message: String) {
        _events.send(PetEvent.ShowToast(message))
    }

    private fun handleStartPet() {
        viewModelScope.launch {
            try {
                repository.startPetService().onSuccess {
                        _state.value = _state.value.copy(
                            isServiceRunning = true,
                            isLoading = false,
                            buttonText = "Stop Pet",
                            error = null
                        )
                        showToast("Pet đã được khởi động!")
                    }.onFailure { error ->
                        when (error) {
                            is SecurityException -> {
                                _state.value = _state.value.copy(
                                    isLoading = false, error = "Cần quyền overlay để hiển thị Pet"
                                )
                                _events.send(PetEvent.ShowPermissionDialog)
                            }

                            else -> {
                                _state.value = _state.value.copy(
                                    isLoading = false,
                                    error = "Không thể khởi động Pet: ${error.message}"
                                )
                                showToast("Lỗi: ${error.message}")
                            }
                        }
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false, error = "Lỗi không xác định: ${e.message}"
                )
                showToast("Lỗi không xác định")
            }
        }
    }

    private fun handleStopPet() {
        viewModelScope.launch {
            try {
                repository.stopPetService().onSuccess {
                        _state.value = _state.value.copy(
                            isServiceRunning = false,
                            isLoading = false,
                            buttonText = "Start Pet",
                            error = null
                        )
                        showToast("Pet đã được dừng!")
                    }.onFailure { error ->
                        _state.value = _state.value.copy(
                            isLoading = false, error = "Không thể dừng Pet: ${error.message}"
                        )
                        showToast("Lỗi: ${error.message}")
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false, error = "Lỗi không xác định: ${e.message}"
                )
                showToast("Lỗi không xác định")
            }
        }
    }

    private fun handleCheckPermission() {
        viewModelScope.launch {
            try {
                val permissionStatus: PermissionStatus = repository.checkOverlayPermission()
                _state.value = _state.value.copy(
                    hasOverlayPermission = permissionStatus.hasOverlayPermission, isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false, error = "Không thể kiểm tra quyền"
                )
            }
        }
    }

    private fun handleRequestPermission() {
        viewModelScope.launch {
            try {
                val intent = repository.createOverlayPermissionIntent()
                _events.send(PetEvent.RequestOverlayPermission(intent))
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false, error = "Không thể yêu cầu quyền"
                )
                showToast("Lỗi khi yêu cầu quyền")
            }
        }
    }

    private fun handlePermissionResult(hasPermission: Boolean) {
        viewModelScope.launch {
            if (hasPermission) {
                showToast("Quyền overlay đã được cấp!")
            } else {
                showToast("Cần quyền overlay để sử dụng Pet")
            }
        }
    }
}