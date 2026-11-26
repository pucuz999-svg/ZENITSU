package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.AppAccessConfig
import com.example.model.FirestoreUiState
import com.example.model.UserSession
import com.example.ui.components.MiniSpecChip
import com.example.ui.components.ResponsiveScreenContainer
import com.example.ui.components.StatusPill
import com.example.ui.components.TacticalCardSurface
import com.example.ui.components.TacticalSectionHeader
import com.example.ui.theme.ApexOrange
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.OptimalGreen
import com.example.ui.theme.TacticalCard
import com.example.ui.theme.TacticalCardBright
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThermalAmber
import java.util.concurrent.TimeUnit

private enum class AdminUserFilter(val label: String) {
    ALL("All"),
    ACTIVE_NOW("Using Now"),
    ALLOWED("Allowed"),
    BLOCKED("Blocked"),
    ADMINS("Admins"),
}

@Composable
fun AdminDashboardScreen(
    currentUserId: String,
    sessionsState: FirestoreUiState<List<UserSession>>,
    appAccessConfig: AppAccessConfig,
    onToggleUserAccess: (UserSession, Boolean, String) -> Unit,
    onToggleAdminRole: (UserSession, Boolean) -> Unit,
    onSaveGlobalPolicy: (Boolean, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(AdminUserFilter.ALL) }
    var blockDialogUser by remember { mutableStateOf<UserSession?>(null) }
    var draftCustomersAllowed by remember(appAccessConfig.customersAllowed) {
        mutableStateOf(appAccessConfig.customersAllowed)
    }
    var draftMaintenanceMsg by remember(appAccessConfig.maintenanceMessage) {
        mutableStateOf(appAccessConfig.maintenanceMessage)
    }

    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(appAccessConfig) {
        draftCustomersAllowed = appAccessConfig.customersAllowed
        draftMaintenanceMsg = appAccessConfig.maintenanceMessage
    }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(15_000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            ResponsiveScreenContainer {
                TacticalSectionHeader(
                    title = "ADMIN ACCESS & LIVE USER MONITOR",
                    icon = Icons.Rounded.AdminPanelSettings,
                    accentColor = CyanTelemetry,
                    trailing = {
                        StatusPill(text = "LIVE SYNC", color = OptimalGreen)
                    },
                )
            }
        }

        // Global Customer Access Switch
        item {
            ResponsiveScreenContainer {
                TacticalCardSurface(borderColor = CyanTelemetry.copy(alpha = 0.4f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Security,
                                    contentDescription = null,
                                    tint = if (draftCustomersAllowed) OptimalGreen else CriticalRed,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = "Master Customer Access",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (draftCustomersAllowed) {
                                    "Enabled: Authorized customers can sign in and use FF ApexBoost."
                                } else {
                                    "Paused: All non-admin customers are locked out in real time."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                        }
                        Switch(
                            checked = draftCustomersAllowed,
                            onCheckedChange = { enabled ->
                                draftCustomersAllowed = enabled
                                onSaveGlobalPolicy(enabled, draftMaintenanceMsg)
                            },
                            modifier = Modifier.testTag("admin_global_access_switch"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = TextPrimary,
                                checkedTrackColor = OptimalGreen,
                                uncheckedThumbColor = TextPrimary,
                                uncheckedTrackColor = CriticalRed,
                            ),
                        )
                    }

                    if (!draftCustomersAllowed) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = draftMaintenanceMsg,
                            onValueChange = { draftMaintenanceMsg = it },
                            label = { Text("Pause message shown to customers") },
                            placeholder = { Text("App access temporarily paused by administrator.") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanTelemetry,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                            ),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { onSaveGlobalPolicy(draftCustomersAllowed, draftMaintenanceMsg) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("admin_save_global_policy"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanTelemetry,
                                contentColor = Color(0xFF04131A),
                            ),
                        ) {
                            Text("Save Pause Message", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        when (sessionsState) {
            is FirestoreUiState.Loading -> {
                item {
                    ResponsiveScreenContainer {
                        TacticalCardSurface {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = CyanTelemetry,
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Loading live user sessions…", color = TextSecondary)
                            }
                        }
                    }
                }
            }

            is FirestoreUiState.Error -> {
                item {
                    ResponsiveScreenContainer {
                        TacticalCardSurface(borderColor = CriticalRed.copy(alpha = 0.55f)) {
                            Text(
                                text = "Unable to observe user sessions",
                                style = MaterialTheme.typography.titleMedium,
                                color = CriticalRed,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sessionsState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                            )
                        }
                    }
                }
            }

            is FirestoreUiState.Success -> {
                val allUsers = sessionsState.data
                val onlineCount = allUsers.count { isCurrentlyActive(it, nowMillis) }
                val allowedCount = allUsers.count { it.accessEnabled }
                val blockedCount = allUsers.count { !it.accessEnabled }

                // Live KPI strip
                item {
                    ResponsiveScreenContainer {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MiniSpecChip(
                                label = "USING NOW",
                                value = "$onlineCount",
                                color = OptimalGreen,
                                modifier = Modifier.weight(1f),
                            )
                            MiniSpecChip(
                                label = "TOTAL USERS",
                                value = "${allUsers.size}",
                                color = CyanTelemetry,
                                modifier = Modifier.weight(1f),
                            )
                            MiniSpecChip(
                                label = "ALLOWED / BLOCKED",
                                value = "$allowedCount / $blockedCount",
                                color = if (blockedCount > 0) ApexOrange else OptimalGreen,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                // Search & Filter Controls
                item {
                    ResponsiveScreenContainer {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_user_search"),
                            placeholder = { Text("Search user name, email, or device…", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = CyanTelemetry) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Rounded.Clear, contentDescription = "Clear", tint = TextSecondary)
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanTelemetry,
                                unfocusedBorderColor = BorderSubtle,
                                focusedContainerColor = TacticalCard,
                                unfocusedContainerColor = TacticalCard,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                            ),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(AdminUserFilter.entries) { filter ->
                                val selected = filter == selectedFilter
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedFilter = filter },
                                    label = { Text(filter.label) },
                                    modifier = Modifier.testTag("admin_filter_${filter.name}"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyanTelemetry.copy(alpha = 0.22f),
                                        selectedLabelColor = CyanTelemetry,
                                        containerColor = TacticalCard,
                                        labelColor = TextSecondary,
                                    ),
                                )
                            }
                        }
                    }
                }

                val q = searchQuery.trim().lowercase()
                val filteredUsers = allUsers.filter { session ->
                    val matchesFilter = when (selectedFilter) {
                        AdminUserFilter.ALL -> true
                        AdminUserFilter.ACTIVE_NOW -> isCurrentlyActive(session, nowMillis)
                        AdminUserFilter.ALLOWED -> session.accessEnabled
                        AdminUserFilter.BLOCKED -> !session.accessEnabled
                        AdminUserFilter.ADMINS -> session.role == "admin"
                    }
                    val matchesQuery = q.isEmpty() ||
                        session.displayName.lowercase().contains(q) ||
                        session.email.lowercase().contains(q) ||
                        session.deviceModel.lowercase().contains(q)
                    matchesFilter && matchesQuery
                }

                if (filteredUsers.isEmpty()) {
                    item {
                        ResponsiveScreenContainer {
                            TacticalCardSurface {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Icon(Icons.Rounded.Group, contentDescription = null, tint = TextMuted)
                                    Text(
                                        text = "No matching logged-in users yet.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                    )
                                }
                            }
                        }
                    }
                } else {
                    items(filteredUsers, key = { it.userId.ifBlank { it.id } }) { session ->
                        ResponsiveScreenContainer {
                            AdminUserSessionCard(
                                session = session,
                                nowMillis = nowMillis,
                                isSelf = (session.userId == currentUserId || session.id == currentUserId),
                                onToggleAccessQuick = { enabled ->
                                    if (enabled) {
                                        onToggleUserAccess(session, true, "")
                                    } else {
                                        blockDialogUser = session
                                    }
                                },
                                onToggleAdminRole = { makeAdmin ->
                                    onToggleAdminRole(session, makeAdmin)
                                },
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    val targetToBlock = blockDialogUser
    if (targetToBlock != null) {
        BlockReasonDialog(
            session = targetToBlock,
            onConfirmBlock = { reason ->
                onToggleUserAccess(targetToBlock, false, reason)
                blockDialogUser = null
            },
            onDismiss = { blockDialogUser = null },
        )
    }
}

@Composable
private fun AdminUserSessionCard(
    session: UserSession,
    nowMillis: Long,
    isSelf: Boolean,
    onToggleAccessQuick: (Boolean) -> Unit,
    onToggleAdminRole: (Boolean) -> Unit,
) {
    val activeNow = isCurrentlyActive(session, nowMillis)
    val isUserAdmin = session.role == "admin"
    val targetUid = session.userId.ifBlank { session.id }

    TacticalCardSurface(
        borderColor = when {
            !session.accessEnabled -> CriticalRed.copy(alpha = 0.55f)
            activeNow -> OptimalGreen.copy(alpha = 0.45f)
            else -> BorderSubtle
        },
        modifier = Modifier.testTag("admin_user_card_$targetUid"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (activeNow) OptimalGreen else TextMuted),
                )
                StatusPill(
                    text = if (activeNow) "USING NOW" else "OFFLINE / IDLE",
                    color = if (activeNow) OptimalGreen else TextMuted,
                )
                StatusPill(
                    text = if (isUserAdmin) "ADMIN" else "CUSTOMER",
                    color = if (isUserAdmin) CyanTelemetry else ApexOrange,
                )
            }
            if (!session.accessEnabled) {
                StatusPill(text = "BLOCKED", color = CriticalRed)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = buildString {
                append(session.displayName.ifBlank { "Unnamed Player" })
                if (isSelf) append(" (You)")
            },
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = session.email,
            style = MaterialTheme.typography.bodyMedium,
            color = CyanTelemetry,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MiniSpecChip(
                label = "CURRENT SCREEN",
                value = session.currentScreen.ifBlank { "Boost Hub" },
                color = if (activeNow) OptimalGreen else TextSecondary,
                modifier = Modifier.weight(1f),
            )
            MiniSpecChip(
                label = "LAST SEEN",
                value = formatRelativeSeen(session, nowMillis),
                color = ThermalAmber,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Devices,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(15.dp),
            )
            val portalLabel = if (session.loginMode == "ADMIN") "Admin Login" else "Customer Login"
            Text(
                text = "${session.deviceModel.ifBlank { "Android" }} • $portalLabel • ${session.activePresetName.ifBlank { "Auto Preset" }}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (!session.accessEnabled && session.blockedReason.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = CriticalRed.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, CriticalRed.copy(alpha = 0.4f)),
            ) {
                Text(
                    text = "Block Reason: ${session.blockedReason}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                )
            }
        }

        if (!isSelf) {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (session.accessEnabled) "App Access Allowed" else "App Access Suspended",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (session.accessEnabled) OptimalGreen else CriticalRed,
                    )
                    Text(
                        text = "Toggle to immediately allow or block this user",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                }
                Switch(
                    checked = session.accessEnabled,
                    onCheckedChange = onToggleAccessQuick,
                    modifier = Modifier.testTag("admin_access_switch_$targetUid"),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TextPrimary,
                        checkedTrackColor = OptimalGreen,
                        uncheckedThumbColor = TextPrimary,
                        uncheckedTrackColor = CriticalRed,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = { onToggleAccessQuick(!session.accessEnabled) },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("admin_block_btn_$targetUid"),
                    border = BorderStroke(
                        1.dp,
                        if (session.accessEnabled) CriticalRed.copy(alpha = 0.6f) else OptimalGreen.copy(alpha = 0.6f),
                    ),
                ) {
                    Icon(
                        imageVector = if (session.accessEnabled) Icons.Rounded.Block else Icons.Rounded.LockOpen,
                        contentDescription = null,
                        tint = if (session.accessEnabled) CriticalRed else OptimalGreen,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (session.accessEnabled) "Suspend User" else "Restore Access",
                        color = if (session.accessEnabled) CriticalRed else OptimalGreen,
                    )
                }
                OutlinedButton(
                    onClick = { onToggleAdminRole(!isUserAdmin) },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("admin_role_btn_$targetUid"),
                    border = BorderStroke(1.dp, CyanTelemetry.copy(alpha = 0.55f)),
                ) {
                    Icon(
                        imageVector = if (isUserAdmin) Icons.Rounded.CheckCircle else Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = CyanTelemetry,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isUserAdmin) "Set Customer" else "Make Admin",
                        color = CyanTelemetry,
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockReasonDialog(
    session: UserSession,
    onConfirmBlock: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var reason by remember { mutableStateOf("Access suspended by administrator.") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TacticalCard,
        titleContentColor = TextPrimary,
        textContentColor = TextSecondary,
        title = { Text("Suspend ${session.displayName.ifBlank { session.email }}?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "This user will be locked out of FF ApexBoost immediately until you re-enable their access.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason shown to user") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CriticalRed,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                    ),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirmBlock(reason) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CriticalRed,
                    contentColor = TextPrimary,
                ),
            ) {
                Text("Suspend Access")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = CyanTelemetry)
            }
        },
    )
}

private fun isCurrentlyActive(session: UserSession, nowMillis: Long): Boolean {
    if (!session.sessionActive) return false
    val lastMs = (session.lastActiveAt ?: session.updatedAt)?.toDate()?.time ?: return true
    return (nowMillis - lastMs) <= TimeUnit.MINUTES.toMillis(3)
}

private fun formatRelativeSeen(session: UserSession, nowMillis: Long): String {
    val ts = (session.lastActiveAt ?: session.updatedAt)?.toDate()?.time ?: return "Active"
    val diffSec = ((nowMillis - ts) / 1000L).coerceAtLeast(0L)
    return when {
        session.sessionActive && diffSec < 45 -> "Live now"
        diffSec < 60 -> "${diffSec}s ago"
        diffSec < 3600 -> "${diffSec / 60}m ago"
        diffSec < 86400 -> "${diffSec / 3600}h ago"
        else -> "${diffSec / 86400}d ago"
    }
}
