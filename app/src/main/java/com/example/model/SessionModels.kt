package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class LoginPortalMode(
    val wireValue: String,
    val title: String,
    val subtitle: String,
) {
    CUSTOMER(
        wireValue = "CUSTOMER",
        title = "Customer Login",
        subtitle = "Sign in with Google to unlock performance boost tools, Scribd sensitivity presets & FPS controls.",
    ),
    ADMIN(
        wireValue = "ADMIN",
        title = "Admin Login",
        subtitle = "Administrator portal to monitor live user activity and control who can access the app.",
    ),
}

@Keep
@IgnoreExtraProperties
data class UserSession(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val loginMode: String = "CUSTOMER",
    val role: String = "customer",
    val accessEnabled: Boolean = true,
    val blockedReason: String = "",
    val sessionActive: Boolean = false,
    val currentScreen: String = "Boost Hub",
    val deviceModel: String = "",
    val activePresetName: String = "",
    @ServerTimestamp val lastActiveAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class AppAccessConfig(
    @DocumentId val id: String = "",
    val customersAllowed: Boolean = true,
    val maintenanceMessage: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class AdminEntry(
    @DocumentId val id: String = "",
    val uid: String = "",
    val email: String = "",
    val grantedBy: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

sealed interface FirestoreUiState<out T> {
    data object Loading : FirestoreUiState<Nothing>
    data class Success<T>(val data: T) : FirestoreUiState<T>
    data class Error(val message: String) : FirestoreUiState<Nothing>
}
