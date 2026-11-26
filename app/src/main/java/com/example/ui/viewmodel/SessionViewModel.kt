package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SessionRepository
import com.example.model.AppAccessConfig
import com.example.model.FirestoreUiState
import com.example.model.LoginPortalMode
import com.example.model.UserSession
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModel(
    private val repo: SessionRepository,
    private val currentUserId: String,
    initialIsBootstrapAdmin: Boolean,
) : ViewModel() {

    private val _portalSyncCompleted = MutableStateFlow(false)
    val portalSyncCompleted: StateFlow<Boolean> = _portalSyncCompleted

    val ownSessionState: StateFlow<FirestoreUiState<UserSession?>> = repo.observeOwnSession(currentUserId)
        .map<UserSession?, FirestoreUiState<UserSession?>> { FirestoreUiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val rootError = firestoreError ?: error
            emit(FirestoreUiState.Error(rootError.localizedMessage ?: "Failed to load session"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = FirestoreUiState.Loading,
        )

    val appAccessConfig: StateFlow<AppAccessConfig> = repo.observeAppAccessConfig()
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            emit(AppAccessConfig(customersAllowed = true, maintenanceMessage = ""))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = AppAccessConfig(customersAllowed = true, maintenanceMessage = ""),
        )

    private val hasAdminDoc: StateFlow<Boolean> = repo.observeOwnAdminEntry(currentUserId)
        .catch { error ->
            if (error is CancellationException) throw error
            emit(false)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = false,
        )

    val isAdmin: StateFlow<Boolean> = combine(
        hasAdminDoc,
        ownSessionState,
    ) { adminDocExists, sessionUi ->
        val sessionRole = (sessionUi as? FirestoreUiState.Success)?.data?.role
        initialIsBootstrapAdmin || adminDocExists || sessionRole == "admin"
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = initialIsBootstrapAdmin,
    )

    val allSessionsState: StateFlow<FirestoreUiState<List<UserSession>>> = isAdmin
        .flatMapLatest { admin ->
            if (!admin) {
                flowOf(FirestoreUiState.Success(emptyList()))
            } else {
                repo.observeAllUserSessions()
                    .map<List<UserSession>, FirestoreUiState<List<UserSession>>> {
                        FirestoreUiState.Success(it)
                    }
                    .catch { error ->
                        val firestoreError = generateSequence(error) { it.cause }
                            .filterIsInstance<FirebaseFirestoreException>()
                            .firstOrNull()
                        if (firestoreError == null && error is CancellationException) throw error
                        val rootError = firestoreError ?: error
                        emit(FirestoreUiState.Error(rootError.localizedMessage ?: "Failed to load active users"))
                    }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = FirestoreUiState.Loading,
        )

    suspend fun syncOnSignIn(
        portalMode: LoginPortalMode,
        currentScreen: String,
        activePresetName: String,
    ): Result<Boolean> {
        val result = repo.syncSessionOnSignIn(portalMode, currentScreen, activePresetName)
        if (result.getOrNull() == true) {
            _portalSyncCompleted.value = true
        }
        return result
    }

    fun updateLiveActivity(
        currentScreen: String,
        activePresetName: String,
        sessionActive: Boolean,
    ) {
        viewModelScope.launch {
            repo.updateLiveActivity(currentScreen, activePresetName, sessionActive)
        }
    }

    suspend fun markSignedOut(): Result<Unit> = repo.markSignedOut(currentUserId)

    fun setUserAccess(
        targetUserId: String,
        enabled: Boolean,
        blockedReason: String,
        onResult: (Result<Unit>) -> Unit,
    ) {
        viewModelScope.launch {
            onResult(repo.setUserAccess(targetUserId, enabled, blockedReason))
        }
    }

    fun setUserAdminRole(
        targetSession: UserSession,
        makeAdmin: Boolean,
        onResult: (Result<Unit>) -> Unit,
    ) {
        viewModelScope.launch {
            onResult(repo.setUserAdminRole(targetSession, makeAdmin))
        }
    }

    fun setGlobalCustomersAllowed(
        allowed: Boolean,
        maintenanceMessage: String,
        onResult: (Result<Unit>) -> Unit,
    ) {
        viewModelScope.launch {
            onResult(repo.setGlobalCustomersAllowed(allowed, maintenanceMessage))
        }
    }
}
