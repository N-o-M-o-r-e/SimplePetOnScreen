package com.nomore.example.simplepetonscreen.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.nomore.example.simplepetonscreen.contract.action.PetAction
import com.nomore.example.simplepetonscreen.contract.event.PetEvent
import com.nomore.example.simplepetonscreen.contract.state.PetState
import com.nomore.example.simplepetonscreen.viewmodel.PetViewModel
import com.nomore.example.simplepetonscreen.di.PetViewModelFactory
import com.nomore.example.simplepetonscreen.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: PetViewModel

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.handleAction(PetAction.CheckPermission)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, PetViewModelFactory(this))[PetViewModel::class.java]

        binding.toggleButton.setOnClickListener {
            val currentState = viewModel.state.value
            if (!currentState.isServiceRunning) {
                viewModel.handleAction(PetAction.StartPet)
            } else {
                viewModel.handleAction(PetAction.StopPet)
            }
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { updateUI(it) }
                }
                launch {
                    viewModel.events.collect { handleEvent(it) }
                }
            }
        }
    }

    private fun updateUI(state: PetState) {
        binding.toggleButton.apply {
            text = state.buttonText
            isEnabled = !state.isLoading
        }

        binding.pbLoading.isVisible = state.isLoading

        state.error?.let { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            viewModel.handleAction(PetAction.ClearError)
        }
    }

    private fun handleEvent(event: PetEvent) {
        when (event) {
            is PetEvent.ShowToast -> {
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
            }

            is PetEvent.RequestOverlayPermission -> {
                overlayPermissionLauncher.launch(event.intent)
            }

            is PetEvent.ShowPermissionDialog -> {
                showPermissionDialog()
            }
        }
    }

    private fun showPermissionDialog() {
        AlertDialog.Builder(this).setTitle("Quyền Overlay")
            .setMessage("Ứng dụng cần quyền hiển thị overlay để hiển thị Pet trên màn hình. Bạn có muốn cấp quyền không?")
            .setPositiveButton("Cấp quyền") { _, _ ->
                viewModel.handleAction(PetAction.RequestPermission)
            }.setNegativeButton("Hủy") { _, _ ->
                Toast.makeText(
                    this,
                    "Không thể hiển thị Pet mà không có quyền overlay",
                    Toast.LENGTH_LONG
                ).show()
            }.show()
    }
}