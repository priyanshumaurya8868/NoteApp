package com.priyanshumaurya8868.noteapp.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.priyanshumaurya8868.noteapp.utils.Event
import com.priyanshumaurya8868.noteapp.utils.LoadingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class LoginViewModel : ViewModel() {

    private val _loadingState = MutableStateFlow(LoadingState.IDLE)
    var loadingState: StateFlow<LoadingState> = _loadingState

    private val _uiState = MutableStateFlow(Event(""))
    val uiState: StateFlow<Event<String>> = _uiState

    fun signWithCredential(credential: AuthCredential) = viewModelScope.launch {
        Log.d("omegaRanger","Got Credentials $credential")
        try {
            _loadingState.emit(LoadingState.LOADING)
            Firebase.auth.signInWithCredential(credential).await()
            _loadingState.emit(LoadingState.LOADED)
        } catch (e: Exception) {
            Log.d("omegaRanger","Got expt ${e.localizedMessage}")
            _loadingState.emit(LoadingState.error(e.localizedMessage))
            _uiState.value = Event(e.localizedMessage?:"Something went wrong!")
        }
    }
}