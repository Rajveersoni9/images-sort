package com.mediaflow.ai.ui.screens.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediaflow.ai.domain.model.ActivityTask
import com.mediaflow.ai.domain.usecase.GetActivityHistoryUseCase
import com.mediaflow.ai.domain.usecase.RunAiOrganizationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivityUiState(
    val tasks: List<ActivityTask> = emptyList(),
    val isProcessing: Boolean = false
)

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val getActivityHistoryUseCase: GetActivityHistoryUseCase,
    private val runAiOrganizationUseCase: RunAiOrganizationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
        observeProcessing()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            getActivityHistoryUseCase().collectLatest { taskList ->
                _uiState.update { it.copy(tasks = taskList) }
            }
        }
    }

    private fun observeProcessing() {
        viewModelScope.launch {
            runAiOrganizationUseCase.isProcessing.collectLatest { processing ->
                _uiState.update { it.copy(isProcessing = processing) }
            }
        }
    }

    fun triggerAiScan() {
        viewModelScope.launch {
            runAiOrganizationUseCase.runBatchClassification()
            runAiOrganizationUseCase.detectDuplicates()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            getActivityHistoryUseCase.clear()
        }
    }
}
