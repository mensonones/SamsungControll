package com.example.samsungcontroll.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.samsungcontroll.ConnectionState
import com.example.samsungcontroll.R
import com.example.samsungcontroll.SavedTv
import com.example.samsungcontroll.TvDeviceInfo
import com.example.samsungcontroll.ui.components.PowerKey
import com.example.samsungcontroll.ui.haptics.LocalHapticsManager
import com.example.samsungcontroll.ui.theme.RemoteTokens
import com.example.samsungcontroll.ui.theme.RemoteType

/**
 * One-line header: the active TV (tap for the TV menu), its connection state, search and power.
 * Switching, nicknaming, details, reconnecting and settings all live in the TV menu.
 */
@Composable
fun RemoteHeader(
    connectionState: ConnectionState,
    isDiscoveryActive: Boolean,
    isSearching: Boolean,
    tvNickname: String,
    ipAddress: String,
    onToggleDiscovery: () -> Unit,
    onReconnect: () -> Unit,
    onPower: () -> Unit,
    onEditNickname: () -> Unit,
    deviceInfo: TvDeviceInfo?,
    macAddress: String,
    onShowSettings: () -> Unit,
    savedTvs: List<SavedTv>,
    displayName: (SavedTv) -> String,
    isCurrentTv: (SavedTv) -> Boolean,
    onSwitchTv: (SavedTv) -> Unit,
    onForgetTv: (SavedTv) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticsManager.current
    var showMenu by remember { mutableStateOf(false) }

    val (statusText, statusColor) = when (connectionState) {
        ConnectionState.DISCONNECTED -> stringResource(R.string.status_disconnected) to RemoteTokens.TextDisabled
        ConnectionState.CONNECTING -> stringResource(R.string.status_connecting) to RemoteTokens.TextSecondary
        ConnectionState.WAITING_FOR_PERMISSION -> stringResource(R.string.status_permission) to RemoteTokens.Warn
        ConnectionState.CONNECTED -> stringResource(R.string.status_connected) to RemoteTokens.Accent
        ConnectionState.FAILED -> stringResource(R.string.status_failed) to RemoteTokens.Power
    }
    val titleText = tvNickname.ifBlank { stringResource(R.string.app_name) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = RemoteTokens.Space4, vertical = RemoteTokens.Space2),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f)) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(RemoteTokens.RadiusKey))
                    .heightIn(min = RemoteTokens.MinTouch)
                    .clickable(onClickLabel = stringResource(R.string.cd_open_tv_menu)) {
                        haptics.performClick()
                        showMenu = true
                    }
                    .padding(horizontal = RemoteTokens.Space2),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(8.dp).background(statusColor, CircleShape))
                Spacer(Modifier.width(RemoteTokens.Space3))
                Column(Modifier.weight(1f, fill = false)) {
                    Text(
                        titleText,
                        style = RemoteType.Title.copy(fontSize = 16.sp),
                        color = RemoteTokens.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (connectionState == ConnectionState.FAILED) stringResource(R.string.failed_retry) else statusText,
                        style = RemoteType.Caption,
                        color = statusColor,
                        modifier = if (connectionState == ConnectionState.FAILED) {
                            Modifier.clickable { haptics.performClick(); onReconnect() }
                        } else Modifier
                    )
                }
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = RemoteTokens.TextSecondary
                )
            }

            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                savedTvs.forEach { tv ->
                    val current = isCurrentTv(tv)
                    DropdownMenuItem(
                        text = {
                            Text(
                                displayName(tv),
                                style = RemoteType.Body,
                                color = if (current) RemoteTokens.Accent else RemoteTokens.TextPrimary,
                                maxLines = 1
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Tv,
                                contentDescription = null,
                                tint = if (current) RemoteTokens.Accent else RemoteTokens.TextSecondary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showMenu = false; onForgetTv(tv) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.cd_forget_tv, displayName(tv)),
                                    tint = RemoteTokens.TextSecondary
                                )
                            }
                        },
                        onClick = {
                            showMenu = false
                            if (!current) onSwitchTv(tv)
                        }
                    )
                }
                if (savedTvs.isNotEmpty()) HorizontalDivider(color = RemoteTokens.Border)
                menuItem(Icons.Default.EditNote, stringResource(R.string.menu_nickname)) { showMenu = false; onEditNickname() }
                menuItem(Icons.Default.Refresh, stringResource(R.string.menu_reconnect)) { showMenu = false; onReconnect() }
                menuItem(Icons.Default.Settings, stringResource(R.string.settings_title)) { showMenu = false; onShowSettings() }
                val details = listOfNotNull(
                    deviceInfo?.modelName?.takeIf { it.isNotBlank() }?.let { stringResource(R.string.tv_info_model) to it },
                    deviceInfo?.firmwareVersion?.takeIf { it.isNotBlank() }?.let { stringResource(R.string.tv_info_firmware) to it },
                    ipAddress.takeIf { it.isNotBlank() }?.let { stringResource(R.string.tv_info_ip) to it },
                    macAddress.takeIf { it.isNotBlank() }?.let { stringResource(R.string.tv_info_mac) to it }
                )
                if (details.isNotEmpty()) HorizontalDivider(color = RemoteTokens.Border)
                details.forEach { (label, value) ->
                    DropdownMenuItem(
                        text = { Text("$label $value", style = RemoteType.Caption, color = RemoteTokens.TextSecondary) },
                        onClick = {},
                        enabled = false
                    )
                }
            }
        }

        Box(contentAlignment = Alignment.Center) {
            IconButton(
                onClick = { haptics.performClick(); onToggleDiscovery() },
                modifier = Modifier
                    .size(RemoteTokens.MinTouch)
                    .border(
                        1.dp,
                        if (isDiscoveryActive) RemoteTokens.Accent else Color.Transparent,
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = stringResource(R.string.cd_search_tv),
                    tint = if (isDiscoveryActive || isSearching) RemoteTokens.Accent else RemoteTokens.TextPrimary
                )
            }
            if (isSearching) SearchOrbitIndicator(buttonSize = RemoteTokens.MinTouch)
        }

        Spacer(Modifier.width(RemoteTokens.Space2))

        PowerKey(
            contentDescription = stringResource(R.string.cd_power),
            icon = Icons.Default.PowerSettingsNew,
            tvIsOn = connectionState == ConnectionState.CONNECTED,
            onClick = onPower
        )
    }
}

@Composable
private fun menuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(label, style = RemoteType.Body, color = RemoteTokens.TextPrimary) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = RemoteTokens.TextSecondary) },
        onClick = onClick
    )
}
