package com.example.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(
    error: Exception,
    operationType: OperationType,
    path: String?,
) {
    val auth = Firebase.auth
    val currentUser = auth.currentUser
    val providerDataJson = JSONArray().apply {
        currentUser?.providerData?.forEach { info ->
            put(
                JSONObject().apply {
                    put("providerId", info.providerId)
                    put("displayName", info.displayName)
                    put("email", info.email)
                    put("photoUrl", info.photoUrl?.toString())
                },
            )
        }
    }

    val payload = JSONObject().apply {
        put("error", error.localizedMessage ?: error.toString())
        put("operationType", operationType.value)
        put("path", path)
        put(
            "authInfo",
            JSONObject().apply {
                put("userId", currentUser?.uid)
                put("email", currentUser?.email)
                put("emailVerified", currentUser?.isEmailVerified)
                put("isAnonymous", currentUser?.isAnonymous)
                put("tenantId", currentUser?.tenantId)
                put("providerInfo", providerDataJson)
            },
        )
    }
    Log.e("FirestoreError", payload.toString(), error)
}
