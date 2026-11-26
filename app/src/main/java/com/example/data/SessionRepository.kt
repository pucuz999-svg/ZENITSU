package com.example.data

import android.content.Context
import android.os.Build
import com.example.R
import com.example.model.AppAccessConfig
import com.example.model.LoginPortalMode
import com.example.model.UserSession
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SessionRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id),
        ),
    )

    companion object {
        const val BOOTSTRAP_ADMIN_EMAIL = "pucuz999@gmail.com"
        private const val GLOBAL_CONFIG_DOC = "global"
    }

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

    fun isBootstrapAdmin(user: FirebaseUser?): Boolean {
        if (user == null) return false
        return user.isEmailVerified &&
            user.email?.equals(BOOTSTRAP_ADMIN_EMAIL, ignoreCase = true) == true
    }

    suspend fun checkIsAdmin(user: FirebaseUser?): Boolean {
        if (user == null) return false
        if (isBootstrapAdmin(user)) return true
        return try {
            db.collection("admins").document(user.uid).get().await().exists()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, "admins/${user.uid}")
            false
        }
    }

    fun observeOwnSession(userId: String): Flow<UserSession?> = flow {
        if (userId.isBlank()) {
            emit(null)
            return@flow
        }
        val path = "user_sessions/$userId"
        emitAll(
            db.collection("user_sessions").document(userId)
                .snapshots()
                .map { snap ->
                    snap.toObject(UserSession::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(ex, OperationType.GET, path)
                    }
                    throw error
                },
        )
    }

    fun observeOwnAdminEntry(userId: String): Flow<Boolean> = flow {
        if (userId.isBlank()) {
            emit(false)
            return@flow
        }
        val path = "admins/$userId"
        emitAll(
            db.collection("admins").document(userId)
                .snapshots()
                .map { snap -> snap.exists() }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(ex, OperationType.GET, path)
                    }
                    emit(false)
                },
        )
    }

    fun observeAppAccessConfig(): Flow<AppAccessConfig> = flow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            emit(AppAccessConfig(customersAllowed = true))
            return@flow
        }
        val path = "app_config/$GLOBAL_CONFIG_DOC"
        emitAll(
            db.collection("app_config").document(GLOBAL_CONFIG_DOC)
                .snapshots()
                .map { snap ->
                    snap.toObject(AppAccessConfig::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?: AppAccessConfig(customersAllowed = true, maintenanceMessage = "")
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser != null) {
                        handleFirestoreError(ex, OperationType.GET, path)
                    }
                    emit(AppAccessConfig(customersAllowed = true, maintenanceMessage = ""))
                },
        )
    }

    fun observeAllUserSessions(): Flow<List<UserSession>> = flow {
        val uid = requireUserId()
        val path = "user_sessions"
        emitAll(
            db.collection("user_sessions")
                .snapshots()
                .map { snap ->
                    snap.toObjects(UserSession::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.lastActiveAt ?: it.updatedAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val ex = firestoreError ?: (error as? Exception)
                    if (ex != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(ex, OperationType.LIST, path)
                    }
                    throw error
                },
        )
    }

    suspend fun syncSessionOnSignIn(
        portalMode: LoginPortalMode,
        currentScreen: String,
        activePresetName: String,
    ): Result<Boolean> {
        val user = auth.currentUser
            ?: return Result.failure(IllegalStateException("Not authenticated"))
        val path = "user_sessions/${user.uid}"
        return try {
            val isAdmin = checkIsAdmin(user)
            if (portalMode == LoginPortalMode.ADMIN && !isAdmin) {
                return Result.success(false)
            }

            val safeName = (user.displayName?.takeIf { it.isNotBlank() }
                ?: user.email?.substringBefore("@")
                ?: "Player").take(80)
            val safeEmail = (user.email?.takeIf { it.length >= 3 } ?: "unknown@user").take(160)
            val safeDevice = "${Build.MANUFACTURER} ${Build.MODEL}".trim().ifBlank { "Android Device" }.take(80)
            val safeScreen = currentScreen.ifBlank { "Boost Hub" }.take(40)
            val safePreset = activePresetName.take(100)
            val safeLoginMode = if (isAdmin && portalMode == LoginPortalMode.ADMIN) "ADMIN" else "CUSTOMER"

            val docRef = db.collection("user_sessions").document(user.uid)
            val existingSnap = docRef.get().await()

            if (!existingSnap.exists()) {
                val payload = mapOf(
                    "userId" to user.uid,
                    "displayName" to safeName,
                    "email" to safeEmail,
                    "loginMode" to safeLoginMode,
                    "role" to if (isAdmin) "admin" else "customer",
                    "accessEnabled" to true,
                    "blockedReason" to "",
                    "sessionActive" to true,
                    "currentScreen" to safeScreen,
                    "deviceModel" to safeDevice,
                    "activePresetName" to safePreset,
                    "lastActiveAt" to FieldValue.serverTimestamp(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
                docRef.set(payload).await()
            } else {
                val updatePayload = mutableMapOf<String, Any>(
                    "displayName" to safeName,
                    "email" to safeEmail,
                    "loginMode" to safeLoginMode,
                    "sessionActive" to true,
                    "currentScreen" to safeScreen,
                    "deviceModel" to safeDevice,
                    "activePresetName" to safePreset,
                    "lastActiveAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
                if (isAdmin && existingSnap.getString("role") != "admin") {
                    updatePayload["role"] = "admin"
                }
                docRef.update(updatePayload).await()
            }
            Result.success(true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun updateLiveActivity(
        currentScreen: String,
        activePresetName: String,
        sessionActive: Boolean,
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.success(Unit)
        val path = "user_sessions/$uid"
        return try {
            val safeScreen = currentScreen.ifBlank { "Boost Hub" }.take(40)
            val safePreset = activePresetName.take(100)
            val safeDevice = "${Build.MANUFACTURER} ${Build.MODEL}".trim().ifBlank { "Android Device" }.take(80)
            db.collection("user_sessions").document(uid).update(
                mapOf(
                    "sessionActive" to sessionActive,
                    "currentScreen" to safeScreen,
                    "deviceModel" to safeDevice,
                    "activePresetName" to safePreset,
                    "lastActiveAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun markSignedOut(uid: String): Result<Unit> {
        if (uid.isBlank()) return Result.success(Unit)
        val path = "user_sessions/$uid"
        return try {
            db.collection("user_sessions").document(uid).update(
                mapOf(
                    "sessionActive" to false,
                    "currentScreen" to "Signed Out",
                    "lastActiveAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun setUserAccess(
        targetUserId: String,
        enabled: Boolean,
        blockedReason: String,
    ): Result<Unit> {
        val path = "user_sessions/$targetUserId"
        return try {
            requireUserId()
            require(targetUserId.isNotBlank()) { "Target userId cannot be blank" }
            val safeReason = if (enabled) "" else blockedReason.trim().ifBlank { "Access paused by administrator." }.take(200)
            db.collection("user_sessions").document(targetUserId).update(
                mapOf(
                    "accessEnabled" to enabled,
                    "blockedReason" to safeReason,
                    "lastActiveAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    suspend fun setUserAdminRole(
        targetSession: UserSession,
        makeAdmin: Boolean,
    ): Result<Unit> {
        val targetUid = targetSession.userId.ifBlank { targetSession.id }
        val sessionPath = "user_sessions/$targetUid"
        val adminPath = "admins/$targetUid"
        return try {
            val adminUid = requireUserId()
            val grantorEmail = auth.currentUser?.email ?: adminUid
            require(targetUid.isNotBlank()) { "Target userId cannot be blank" }
            require(targetUid != adminUid || makeAdmin) { "Cannot revoke your own admin status." }

            if (makeAdmin) {
                val adminRef = db.collection("admins").document(targetUid)
                val existingAdmin = adminRef.get().await()
                if (!existingAdmin.exists()) {
                    adminRef.set(
                        mapOf(
                            "uid" to targetUid,
                            "email" to targetSession.email.ifBlank { "unknown@user" }.take(160),
                            "grantedBy" to grantorEmail.take(160),
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp(),
                        ),
                    ).await()
                } else {
                    adminRef.update(
                        mapOf(
                            "email" to targetSession.email.ifBlank { "unknown@user" }.take(160),
                            "grantedBy" to grantorEmail.take(160),
                            "updatedAt" to FieldValue.serverTimestamp(),
                        ),
                    ).await()
                }
            } else {
                db.collection("admins").document(targetUid).delete().await()
            }

            db.collection("user_sessions").document(targetUid).update(
                mapOf(
                    "role" to if (makeAdmin) "admin" else "customer",
                    "lastActiveAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                ),
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "$sessionPath / $adminPath")
            Result.failure(e)
        }
    }

    suspend fun setGlobalCustomersAllowed(
        allowed: Boolean,
        maintenanceMessage: String,
    ): Result<Unit> {
        val path = "app_config/$GLOBAL_CONFIG_DOC"
        return try {
            requireUserId()
            val safeMsg = maintenanceMessage.trim().take(240)
            val docRef = db.collection("app_config").document(GLOBAL_CONFIG_DOC)
            val snap = docRef.get().await()
            if (!snap.exists()) {
                docRef.set(
                    mapOf(
                        "customersAllowed" to allowed,
                        "maintenanceMessage" to safeMsg,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "customersAllowed" to allowed,
                        "maintenanceMessage" to safeMsg,
                        "updatedAt" to FieldValue.serverTimestamp(),
                    ),
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }
}
