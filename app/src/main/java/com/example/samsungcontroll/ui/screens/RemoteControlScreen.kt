package com.example.samsungcontroll.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tv
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.samsungcontroll.AppSettings
import com.example.samsungcontroll.ConnectionState
import com.example.samsungcontroll.DiscoveredTv
import com.example.samsungcontroll.R
import com.example.samsungcontroll.RemoteViewModel
import com.example.samsungcontroll.SavedTv
import com.example.samsungcontroll.TvConnectionException
import com.example.samsungcontroll.TvDeviceInfo
import com.example.samsungcontroll.ui.theme.RemoteTokens
import com.example.samsungcontroll.ui.haptics.LocalHapticsManager
import androidx.compose.foundation.Canvas
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.platform.LocalContext
import android.provider.Settings
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import com.example.samsungcontroll.ui.haptics.ToggleableHapticsManager
import com.example.samsungcontroll.ui.haptics.rememberHapticsManager
import kotlinx.coroutines.delay
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun RemoteControlScreen(viewModel: RemoteViewModel) {
    val baseHaptics = rememberHapticsManager()
    val hapticsManager = remember(baseHaptics) {
        ToggleableHapticsManager(baseHaptics) { viewModel.hapticsEnabled }
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    CompositionLocalProvider(LocalHapticsManager provides hapticsManager) {
        var showSplash by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            viewModel.initialize()
            delay(SPLASH_DURATION_MS.milliseconds)
            showSplash = false
        }

        if (showSplash) {
            BrandedSplash()
        } else {
            RemoteControlContent(viewModel)
        }
    }
}

@Composable
private fun RemoteControlContent(viewModel: RemoteViewModel) {
    var showNicknameDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showKeyboardDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var tvToForget by remember { mutableStateOf<SavedTv?>(null) }

    // The TV opened a text field (search box, password…): bring up our keyboard dialog.
    LaunchedEffect(viewModel.tvTextInputActive) {
        if (viewModel.tvTextInputActive) showKeyboardDialog = true
    }

    tvToForget?.let { tv ->
        AlertDialog(
            onDismissRequest = { tvToForget = null },
            containerColor = Color(0xFF111827),
            title = { Text(stringResource(R.string.forget_tv_title), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Text(
                    stringResource(R.string.forget_tv_message, viewModel.displayName(tv)),
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.forgetTv(tv); tvToForget = null }) {
                    Text(stringResource(R.string.action_forget), color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { tvToForget = null }) {
                    Text(stringResource(R.string.action_cancel), color = Color(0xFF94A3B8), fontSize = 13.sp)
                }
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            hapticsEnabled = viewModel.hapticsEnabled,
            volumeKeysEnabled = viewModel.volumeKeysEnabled,
            idleTimeoutMinutes = viewModel.idleTimeoutMinutes,
            onHapticsChange = viewModel::updateHapticsEnabled,
            onVolumeKeysChange = viewModel::updateVolumeKeysEnabled,
            onIdleTimeoutChange = viewModel::updateIdleTimeoutMinutes,
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showKeyboardDialog) {
        TvKeyboardDialog(
            onSend = { viewModel.sendText(it) },
            onEnter = { viewModel.sendKey("KEY_ENTER") },
            onDismiss = { showKeyboardDialog = false }
        )
    }

    if (showNicknameDialog) {
        NicknameDialog(
            currentNickname = viewModel.tvNickname,
            onDismiss = { showNicknameDialog = false },
            onSave = { newNickname ->
                viewModel.saveTvNickname(newNickname)
                showNicknameDialog = false
            }
        )
    }

    if (showDetailsDialog) {
        TvDetailsDialog(
            deviceInfo = viewModel.tvDeviceInfo,
            ipAddress = viewModel.ipAddress,
            macAddress = viewModel.macAddress,
            onDismiss = { showDetailsDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RemoteTokens.Background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        RemoteHeader(
            connectionState = viewModel.connectionState,
            isDiscoveryActive = viewModel.showDiscovery,
            isSearching = viewModel.isSearching,
            tvNickname = viewModel.tvNickname.ifBlank {
                viewModel.tvDeviceInfo?.name?.takeIf { it != "Smart TV" }.orEmpty()
            },
            ipAddress = viewModel.ipAddress,
            onToggleDiscovery = {
                // Tapping the magnifier starts a scan (results are revealed only when
                // it finishes); tapping again while results are shown hides them.
                if (viewModel.showDiscovery) {
                    viewModel.toggleDiscovery()
                } else {
                    viewModel.searchTvs()
                }
            },
            onReconnect = { viewModel.reconnect() },
            onPower = { viewModel.togglePower() },
            onEditNickname = { showNicknameDialog = true },
            onShowDetails = { showDetailsDialog = true },
            onShowSettings = { showSettingsDialog = true },
            savedTvs = viewModel.savedTvs,
            displayName = viewModel::displayName,
            isCurrentTv = viewModel::isCurrentTv,
            onSwitchTv = viewModel::switchToTv,
            onForgetTv = { tvToForget = it }
        )

        ConnectionStatusBanner(
            connectionState = viewModel.connectionState,
            certificateChanged = viewModel.connectionError is TvConnectionException.CertificateMismatch,
            onRepair = { viewModel.forgetPairingAndReconnect() }
        )

        if (viewModel.showDiscovery) {
            ConnectionSheet(
                discoveredTvs = viewModel.discoveredTvs,
                currentConnectedIp = viewModel.ipAddress,
                connectionState = viewModel.connectionState,
                onSearch = { viewModel.searchTvs() },
                onSelect = { tv -> viewModel.connectToTv(tv) },
                onConnectManualIp = { manualIp -> viewModel.connectToTv(manualIp) },
                onDismiss = { viewModel.toggleDiscovery() }
            )
        }

        RemoteBody(
            state = viewModel.connectionState,
            isMuted = viewModel.isMuted,
            onToggleMute = { viewModel.toggleMute() },
            onSendKey = { viewModel.sendKey(it) },
            onLaunchApp = { viewModel.launchApp(it) },
            installedApps = viewModel.installedApps,
            favoriteAppIds = viewModel.favoriteAppIds,
            onToggleFavorite = viewModel::toggleFavoriteApp,
            onRefreshApps = { viewModel.refreshInstalledApps() },
            onShowKeyboard = { showKeyboardDialog = true },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TvDetailsDialog(
    deviceInfo: TvDeviceInfo?,
    ipAddress: String,
    macAddress: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Tv,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.tv_info_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRow(stringResource(R.string.tv_info_model), deviceInfo?.modelName ?: "Samsung Smart TV")
                DetailRow(stringResource(R.string.tv_info_os), deviceInfo?.os ?: "Tizen OS")
                if (!deviceInfo?.firmwareVersion.isNullOrBlank()) {
                    DetailRow(stringResource(R.string.tv_info_firmware), deviceInfo.firmwareVersion)
                }
                DetailRow(stringResource(R.string.tv_info_network), deviceInfo?.networkType ?: "Wi-Fi")
                DetailRow(stringResource(R.string.tv_info_ip), ipAddress.ifBlank { stringResource(R.string.tv_info_unknown) })
                if (macAddress.isNotBlank()) {
                    DetailRow(stringResource(R.string.tv_info_mac), macAddress)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = CircleShape
            ) {
                Text(stringResource(R.string.action_close), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    )
}

@Composable
private fun SettingsDialog(
    hapticsEnabled: Boolean,
    volumeKeysEnabled: Boolean,
    idleTimeoutMinutes: Int,
    onHapticsChange: (Boolean) -> Unit,
    onVolumeKeysChange: (Boolean) -> Unit,
    onIdleTimeoutChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF111827),
        title = { Text(stringResource(R.string.settings_title), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SettingSwitchRow(stringResource(R.string.settings_haptics), hapticsEnabled, onHapticsChange)
                SettingSwitchRow(
                    stringResource(R.string.settings_volume_keys),
                    volumeKeysEnabled,
                    onVolumeKeysChange
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.settings_idle_timeout), color = Color.White, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AppSettings.IDLE_TIMEOUT_OPTIONS.forEach { minutes ->
                            FilterChip(
                                selected = minutes == idleTimeoutMinutes,
                                onClick = { onIdleTimeoutChange(minutes) },
                                label = { Text(if (minutes == 0) stringResource(R.string.settings_idle_never) else stringResource(R.string.settings_idle_minutes, minutes), fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    )
}

@Composable
private fun SettingSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun TvKeyboardDialog(
    onSend: (String) -> Unit,
    onEnter: () -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF111827),
        title = { Text(stringResource(R.string.keyboard_title), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.keyboard_placeholder), fontSize = 13.sp, color = Color(0xFF64748B)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    onSend(text)
                    onEnter()
                    onDismiss()
                }),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
            )
        },
        confirmButton = {
            TextButton(onClick = { onSend(text); onEnter(); onDismiss() }) {
                Text(stringResource(R.string.action_send), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = { onSend(text) }) {
                Text(stringResource(R.string.keyboard_type_only), color = Color(0xFF94A3B8), fontSize = 13.sp)
            }
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close), color = Color(0xFF94A3B8), fontSize = 13.sp)
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label, color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun NicknameDialog(
    currentNickname: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var textValue by remember { mutableStateOf(currentNickname) }
    val presets = listOf(stringResource(R.string.preset_living_room), stringResource(R.string.preset_bedroom), stringResource(R.string.preset_balcony), stringResource(R.string.preset_kitchen))

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1E293B),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.EditNote,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.nickname_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.nickname_message),
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    placeholder = { Text("Ex: TV da Sala", fontSize = 13.sp, color = Color(0xFF64748B)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text(stringResource(R.string.nickname_suggestions), color = Color(0xFFCBD5E1), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.take(2).forEach { preset ->
                        AssistChip(
                            onClick = { textValue = preset },
                            label = { Text(preset, fontSize = 11.sp, color = Color.White) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF334155))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.drop(2).forEach { preset ->
                        AssistChip(
                            onClick = { textValue = preset },
                            label = { Text(preset, fontSize = 11.sp, color = Color.White) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF334155))
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(textValue) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = CircleShape
            ) {
                Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = Color(0xFF94A3B8), fontSize = 13.sp)
            }
        }
    )
}

@Composable
private fun BrandedSplash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RemoteTokens.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.app_logo_mark),
                contentDescription = stringResource(R.string.cd_logo_control),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SAMSUNG",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.5.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CONTROL",
                    color = Color(0xFF38BDF8),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.5.sp
                )
            }
        }
    }
}

/** True when the system has animations turned off (accessibility / developer setting). */
@Composable
private fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
}

/**
 * A comet of light travelling along the icon's border to signal an in-flight scan:
 * a bright white head with a soft glow, trailed by a fading accent-blue tail.
 * Collapses to an in-place opacity pulse when the system disables animations.
 */
@Composable
internal fun SearchOrbitIndicator(buttonSize: Dp, modifier: Modifier = Modifier) {
    val comet = Color(0xFF38BDF8)
    val transition = rememberInfiniteTransition(label = "search-orbit")

    if (rememberReducedMotion()) {
        val alpha by transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 700, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
        Canvas(modifier = modifier.requiredSize(buttonSize + 14.dp)) {
            val orbitRadius = buttonSize.toPx() / 2f - 1.dp.toPx()
            val head = Offset(center.x, center.y - orbitRadius)
            drawCircle(comet.copy(alpha = 0.3f * alpha), 6.dp.toPx(), head)
            drawCircle(Color.White.copy(alpha = alpha), 2.4.dp.toPx(), head)
        }
        return
    }

    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing)
        ),
        label = "orbit-angle"
    )
    // Canvas is larger than the button so the head's glow can breathe past the ring.
    Canvas(modifier = modifier.requiredSize(buttonSize + 14.dp)) {
        // Ride right on the button's border ring (independent of the padded canvas).
        val orbitRadius = buttonSize.toPx() / 2f - 1.dp.toPx()

        fun pointAt(deg: Float): Offset {
            val rad = ((deg - 90f) * PI / 180f).toFloat()
            return Offset(center.x + orbitRadius * cos(rad), center.y + orbitRadius * sin(rad))
        }

        // Tail: dots trailing the head, shrinking and fading into nothing.
        val tailCount = 16
        val tailSpacingDeg = 5.5f
        for (i in tailCount downTo 1) {
            val t = 1f - i / tailCount.toFloat() // 0 at the far tail, ~1 near the head
            val p = pointAt(angle - i * tailSpacingDeg)
            drawCircle(
                color = comet.copy(alpha = t * t * 0.9f),
                radius = (0.6f + 2.0f * t).dp.toPx(),
                center = p
            )
        }

        // Head: soft glow halo, then a bright white core.
        val head = pointAt(angle)
        drawCircle(color = comet.copy(alpha = 0.35f), radius = 6.dp.toPx(), center = head)
        drawCircle(color = Color.White.copy(alpha = 0.9f), radius = 4.dp.toPx(), center = head)
        drawCircle(color = Color.White, radius = 2.4.dp.toPx(), center = head)
    }
}

private const val SPLASH_DURATION_MS = 900L

@Composable
private fun ConnectionStatusBanner(
    connectionState: ConnectionState,
    certificateChanged: Boolean,
    onRepair: () -> Unit
) {
    val haptics = LocalHapticsManager.current

    when (connectionState) {
        ConnectionState.FAILED -> if (certificateChanged) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3B0D0D)),
                border = BorderStroke(1.dp, Color(0xFFEF4444))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        stringResource(R.string.cert_changed_title),
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        stringResource(R.string.cert_changed_message),
                        color = Color(0xFFFECACA),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { haptics.performClick(); onRepair() }) {
                        Text(stringResource(R.string.cert_repair), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
        ConnectionState.WAITING_FOR_PERMISSION -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF381E08)),
                border = BorderStroke(1.dp, Color(0xFFF59E0B))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠️", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stringResource(R.string.permission_title),
                            color = Color(0xFFF59E0B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            stringResource(R.string.permission_message),
                            color = Color(0xFFFDE68A),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
        // Other failures are surfaced by the header status; tapping it retries.
        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectionSheet(
    discoveredTvs: List<DiscoveredTv>,
    currentConnectedIp: String,
    connectionState: ConnectionState,
    onSearch: () -> Unit,
    onSelect: (DiscoveredTv) -> Unit,
    onConnectManualIp: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val haptics = LocalHapticsManager.current
    var manualIpText by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = Color(0xFF121A28)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    stringResource(R.string.connect_title),
                    color = RemoteTokens.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                TextButton(onClick = { haptics.performClick(); onSearch() }) {
                    Icon(Icons.Default.Refresh, null, tint = RemoteTokens.Accent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.search_again), color = RemoteTokens.Accent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Spacer(Modifier.height(14.dp))

            if (discoveredTvs.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(RemoteTokens.Surface1)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(stringResource(R.string.no_tv_found), color = RemoteTokens.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.no_tv_found_hint),
                        color = RemoteTokens.TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(Modifier.height(14.dp))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    discoveredTvs.forEach { tv ->
                        ConnectionTvRow(tv, currentConnectedIp, connectionState, onSelect)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (!showManualInput) {
                OutlinedButton(
                    onClick = { showManualInput = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(RemoteTokens.RadiusButton),
                    border = BorderStroke(1.dp, RemoteTokens.BorderStrong)
                ) {
                    Icon(Icons.Default.EditNote, null, tint = RemoteTokens.Accent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.enter_ip_manually), color = RemoteTokens.Accent, fontSize = 13.sp)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = manualIpText,
                        onValueChange = { manualIpText = it },
                        placeholder = { Text(stringResource(R.string.ip_placeholder), fontSize = 12.sp, color = RemoteTokens.TextDisabled) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RemoteTokens.Accent,
                            unfocusedBorderColor = RemoteTokens.BorderStrong,
                            focusedTextColor = RemoteTokens.TextPrimary,
                            unfocusedTextColor = RemoteTokens.TextPrimary
                        )
                    )
                    Button(
                        onClick = {
                            if (manualIpText.isNotBlank()) {
                                haptics.performClick()
                                onConnectManualIp(manualIpText.trim())
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RemoteTokens.Accent),
                        modifier = Modifier.height(54.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Text(stringResource(R.string.cd_connect), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF06121F))
                    }
                }
            }
        }
    }
}

/** One discovered-TV row with an inline connection state (idle / connecting / connected / failed). */
@Composable
private fun ConnectionTvRow(
    tv: DiscoveredTv,
    currentConnectedIp: String,
    connectionState: ConnectionState,
    onSelect: (DiscoveredTv) -> Unit
) {
    val haptics = LocalHapticsManager.current
    val isThis = tv.ip == currentConnectedIp
    val isConnected = isThis && connectionState == ConnectionState.CONNECTED
    val isConnecting = isThis && connectionState == ConnectionState.CONNECTING
    val isFailed = isThis && connectionState == ConnectionState.FAILED
    val displayName = if (tv.name.startsWith("(") || tv.name.isBlank()) "Smart TV" else tv.name

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isConnected) Color(0xFF14301F) else RemoteTokens.Surface2)
            .border(
                BorderStroke(1.dp, if (isConnected) Color(0xFF22C55E) else RemoteTokens.Border),
                RoundedCornerShape(16.dp)
            )
            .clickable { haptics.performClick(); onSelect(tv) }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) Color(0xFF22C55E).copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Tv,
                    null,
                    tint = if (isConnected) Color(0xFF22C55E) else RemoteTokens.TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(displayName, color = RemoteTokens.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                Text(tv.ip, color = RemoteTokens.TextSecondary, fontSize = 12.sp, maxLines = 1)
            }
        }

        when {
            isConnected -> Text(stringResource(R.string.connected_check), color = Color(0xFF22C55E), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            isConnecting -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = RemoteTokens.Accent)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.connecting_short), color = RemoteTokens.Accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            isFailed -> Text(stringResource(R.string.failed_retry), color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            else -> Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, stringResource(R.string.cd_connect), tint = RemoteTokens.Accent, modifier = Modifier.size(22.dp))
        }
    }
}

