package com.malmungchi.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malmungchi.core.model.LevelTestQuestion
import com.malmungchi.core.model.LevelTestSubmitAnswer
import com.malmungchi.core.repository.LevelTestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LevelTestViewModel @Inject constructor(
    private val repository: LevelTestRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LevelTestState>(LevelTestState.Idle)
    val state: StateFlow<LevelTestState> = _state

    fun load(stage: Int) {
        _state.value = LevelTestState.Loading
        viewModelScope.launch {
            repository.generateLevelTest(stage)
                .onSuccess { _state.value = LevelTestState.Questions(it) }
                .onFailure { _state.value = LevelTestState.Error(it.message ?: "불러오기 실패") }
        }
    }

    fun submit(answers: List<LevelTestSubmitAnswer>) {
        _state.value = LevelTestState.Loading
        viewModelScope.launch {
            repository.submitLevelTest(answers)
                .onSuccess { res -> _state.value = LevelTestState.SubmitDone(res.correctCount, res.resultLevel) }
                .onFailure { _state.value = LevelTestState.Error(it.message ?: "제출 실패") }
        }
    }
}

sealed interface LevelTestState {
    data object Idle : LevelTestState
    data object Loading : LevelTestState
    data class Questions(val items: List<LevelTestQuestion>) : LevelTestState
    data class SubmitDone(val correctCount: Int, val resultLevel: String) : LevelTestState
    data class Error(val message: String) : LevelTestState
}