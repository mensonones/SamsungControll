package com.example.samsungcontroll.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.abs
import kotlin.math.sign
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.example.samsungcontroll.QuickAction
import com.example.samsungcontroll.ShortcutPinner
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.samsungcontroll.ConnectionState
import com.example.samsungcontroll.R
import com.example.samsungcontroll.TvApp
import com.example.samsungcontroll.ui.components.DPad
import com.example.samsungcontroll.ui.components.DPadLabels
import com.example.samsungcontroll.ui.components.RemoteIconKey
import com.example.samsungcontroll.ui.components.RemoteKey
import com.example.samsungcontroll.ui.haptics.LocalHapticsManager
import com.example.samsungcontroll.ui.theme.RemoteTokens
import com.example.samsungcontroll.ui.theme.RemoteType

/** Apps shown (with their Samsung app ids) until the TV reports its installed list. */
private val FeaturedApps = listOf(
    TvApp("3201907018807", "Netflix"),
    TvApp("111299001912", "YouTube"),
    TvApp("3201910019365", "Prime Video"),
    TvApp("3201907018784", "Disney+"),
    TvApp("3202103023447", "Max"),
    TvApp("3201606009684", "Spotify"),
    TvApp("3201601007250", "Globoplay"),
    TvApp("3201807016597", "Apple TV")
)

/**
 * The remote itself: three pages (control, keypad, apps) with a tab rail at the bottom,
 * inside the thumb's reach. There is no device-shaped frame; the screen is the remote.
 */
@Composable
fun RemoteBody(
    state: ConnectionState,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onSendKey: (String) -> Unit,
    onLaunchApp: (String) -> Unit,
    installedApps: List<TvApp>,
    favoriteAppIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onRefreshApps: () -> Unit,
    onShowKeyboard: () -> Unit,
    onRunScene: (QuickAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val isConnected = state == ConnectionState.CONNECTED
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTab) {
                0 -> ControlPage(isConnected, isMuted, onToggleMute, onSendKey)
                1 -> TouchpadPage(isConnected, onSendKey)
                2 -> KeypadPage(isConnected, onSendKey, onShowKeyboard, onRunScene)
                else -> AppsPage(
                    isConnected, installedApps, favoriteAppIds, onToggleFavorite,
                    onLaunchApp, onRefreshApps, onSendKey, onRunScene
                )
            }
        }
        RemoteTabRail(selectedTab) { selectedTab = it }
    }
}

@Composable
private fun ControlPage(
    isConnected: Boolean,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onSendKey: (String) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(horizontal = RemoteTokens.Space4),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DPad(
                enabled = isConnected,
                labels = DPadLabels(
                    pad = stringResource(R.string.cd_dpad),
                    up = stringResource(R.string.cd_nav_up),
                    down = stringResource(R.string.cd_nav_down),
                    left = stringResource(R.string.cd_nav_left),
                    right = stringResource(R.string.cd_nav_right),
                    ok = stringResource(R.string.cd_ok)
                ),
                onUp = { onSendKey("KEY_UP") },
                onDown = { onSendKey("KEY_DOWN") },
                onLeft = { onSendKey("KEY_LEFT") },
                onRight = { onSendKey("KEY_RIGHT") },
                onOk = { onSendKey("KEY_ENTER") },
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .fillMaxWidth(0.82f)
                    .aspectRatio(1f)
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(RemoteTokens.Space2)) {
                RemoteKey("BACK", Modifier.weight(1f), isConnected) { onSendKey("KEY_RETURN") }
                RemoteKey("HOME", Modifier.weight(1f), isConnected) { onSendKey("KEY_HOME") }
                RemoteKey("EXIT", Modifier.weight(1f), isConnected) { onSendKey("KEY_EXIT") }
            }

            Row(
                Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(RemoteTokens.Space2)
            ) {
                Rocker(
                    legend = "VOL",
                    plusIcon = Icons.Default.Add,
                    minusIcon = Icons.Default.Remove,
                    plusDescription = stringResource(R.string.cd_volume_up),
                    minusDescription = stringResource(R.string.cd_volume_down),
                    enabled = isConnected,
                    modifier = Modifier.weight(1f),
                    onPlus = { onSendKey("KEY_VOLUP") },
                    onMinus = { onSendKey("KEY_VOLDOWN") }
                )
                Column(
                    modifier = Modifier.weight(0.7f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(RemoteTokens.Space1),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    RemoteIconKey(
                        icon = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isMuted) stringResource(R.string.cd_muted) else stringResource(R.string.cd_unmuted),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        enabled = isConnected,
                        onClick = onToggleMute
                    )
                    Text("MUTE", style = RemoteType.Legend, color = RemoteTokens.TextSecondary)
                }
                Rocker(
                    legend = "CH",
                    plusIcon = Icons.Default.KeyboardArrowUp,
                    minusIcon = Icons.Default.KeyboardArrowDown,
                    plusDescription = stringResource(R.string.cd_channel_up),
                    minusDescription = stringResource(R.string.cd_channel_down),
                    enabled = isConnected,
                    modifier = Modifier.weight(1f),
                    onPlus = { onSendKey("KEY_CHUP") },
                    onMinus = { onSendKey("KEY_CHDOWN") }
                )
            }
        }
    }
}

/** Two stacked keys around a legend: the physical volume/channel rocker. */
@Composable
private fun Rocker(
    legend: String,
    plusIcon: androidx.compose.ui.graphics.vector.ImageVector,
    minusIcon: androidx.compose.ui.graphics.vector.ImageVector,
    plusDescription: String,
    minusDescription: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onPlus: () -> Unit,
    onMinus: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(RemoteTokens.Space1),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RemoteIconKey(plusIcon, plusDescription, Modifier.fillMaxWidth().height(64.dp), enabled, onRepeat = onPlus, onClick = onPlus)
        Text(legend, style = RemoteType.Legend, color = RemoteTokens.TextSecondary)
        RemoteIconKey(minusIcon, minusDescription, Modifier.fillMaxWidth().height(64.dp), enabled, onRepeat = onMinus, onClick = onMinus)
    }
}

/**
 * Gesture pad, like the Google TV remote: swipe to move (one key per ~56 dp travelled),
 * tap for OK, double-tap for Home, long-press for Back. Uses only plain remote keys.
 */
@Composable
private fun TouchpadPage(
    isConnected: Boolean,
    onSendKey: (String) -> Unit
) {
    val haptics = LocalHapticsManager.current
    val currentSend = rememberUpdatedState(onSendKey)
    var pressed by remember { mutableStateOf(false) }
    val stepPx = with(LocalDensity.current) { 56.dp.toPx() }

    Column(
        modifier = Modifier.fillMaxSize().padding(RemoteTokens.Space4),
        verticalArrangement = Arrangement.spacedBy(RemoteTokens.Space3)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(RemoteTokens.RadiusSurface))
                .background(if (isConnected) RemoteTokens.Surface1 else RemoteTokens.SurfaceDisabled)
                .border(
                    1.dp,
                    if (pressed) RemoteTokens.Accent else RemoteTokens.Border,
                    RoundedCornerShape(RemoteTokens.RadiusSurface)
                )
                .pointerInput(isConnected) {
                    if (!isConnected) return@pointerInput
                    detectTapGestures(
                        onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                        onTap = { haptics.performKeypress(); currentSend.value("KEY_ENTER") },
                        onDoubleTap = { haptics.performClick(); currentSend.value("KEY_HOME") },
                        onLongPress = { haptics.performToggle(); currentSend.value("KEY_RETURN") }
                    )
                }
                .pointerInput(isConnected) {
                    if (!isConnected) return@pointerInput
                    var accX = 0f
                    var accY = 0f
                    detectDragGestures(
                        onDragStart = { accX = 0f; accY = 0f },
                        onDrag = { change, drag ->
                            change.consume()
                            accX += drag.x
                            accY += drag.y
                            while (abs(accX) >= stepPx || abs(accY) >= stepPx) {
                                if (abs(accX) >= abs(accY)) {
                                    currentSend.value(if (accX > 0) "KEY_RIGHT" else "KEY_LEFT")
                                    accX -= stepPx * sign(accX)
                                    accY = 0f
                                } else {
                                    currentSend.value(if (accY > 0) "KEY_DOWN" else "KEY_UP")
                                    accY -= stepPx * sign(accY)
                                    accX = 0f
                                }
                                haptics.performKeypress()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.touchpad_hint),
                style = RemoteType.Caption,
                color = RemoteTokens.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(RemoteTokens.Space6)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(RemoteTokens.Space2)) {
            RemoteKey("BACK", Modifier.weight(1f), isConnected) { onSendKey("KEY_RETURN") }
            RemoteKey("HOME", Modifier.weight(1f), isConnected) { onSendKey("KEY_HOME") }
        }
    }
}

@Composable
private fun KeypadPage(
    isConnected: Boolean,
    onSendKey: (String) -> Unit,
    onShowKeyboard: () -> Unit,
    onRunScene: (QuickAction) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = RemoteTokens.Space4),
        verticalArrangement = Arrangement.spacedBy(RemoteTokens.Space2)
    ) {
        RemoteKey(stringResource(R.string.keyboard_open), Modifier.fillMaxWidth(), isConnected) { onShowKeyboard() }

        Spacer(Modifier.height(RemoteTokens.Space2))

        val rows = listOf(
            listOf("1" to "KEY_1", "2" to "KEY_2", "3" to "KEY_3"),
            listOf("4" to "KEY_4", "5" to "KEY_5", "6" to "KEY_6"),
            listOf("7" to "KEY_7", "8" to "KEY_8", "9" to "KEY_9"),
            listOf("PRE-CH" to "KEY_PRECH", "0" to "KEY_0", "123" to "KEY_123")
        )
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(RemoteTokens.Space2)) {
                row.forEach { (label, key) ->
                    val isDigit = label.length == 1
                    RemoteKey(
                        label = label,
                        modifier = Modifier.weight(1f),
                        enabled = isConnected,
                        textStyle = if (isDigit) RemoteType.Digit else RemoteType.Legend,
                        minHeight = 64.dp
                    ) { onSendKey(key) }
                }
            }
        }

        Spacer(Modifier.height(RemoteTokens.Space3))
        SectionLabel(stringResource(R.string.section_inputs))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(RemoteTokens.Space2)) {
            listOf("HDMI 1" to "KEY_HDMI1", "HDMI 2" to "KEY_HDMI2", "HDMI 3" to "KEY_HDMI3").forEach { (label, key) ->
                var menuOpen by remember { mutableStateOf(false) }
                val shortcutLabel = stringResource(R.string.shortcut_label_input, label)
                Box(Modifier.weight(1f)) {
                    // Long-press works while disconnected too: the scene wakes the TV first.
                    RemoteKey(
                        label,
                        Modifier.fillMaxWidth(),
                        enabled = true,
                        onLongClick = { menuOpen = true }
                    ) { if (isConnected) onSendKey(key) }
                    SceneMenu(
                        expanded = menuOpen,
                        onDismiss = { menuOpen = false },
                        entries = listOf(
                            stringResource(R.string.scene_wake_input) to { onRunScene(QuickAction.WakeAndKey(key)) },
                            stringResource(R.string.scene_pin_shortcut) to {
                                ShortcutPinner.pin(context, shortcutLabel, QuickAction.WakeAndKey(key))
                            }
                        )
                    )
                }
            }
            RemoteKey("TV", Modifier.weight(1f), isConnected) { onSendKey("KEY_TV") }
        }

        Spacer(Modifier.height(RemoteTokens.Space3))
        var showMore by rememberSaveable { mutableStateOf(false) }
        RemoteKey(
            label = stringResource(if (showMore) R.string.less_keys else R.string.more_keys),
            modifier = Modifier.fillMaxWidth(),
            enabled = true
        ) { showMore = !showMore }
        if (showMore) {
            SectionLabel(stringResource(R.string.section_advanced))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(RemoteTokens.Space2)) {
                RemoteKey("GUIDE", Modifier.weight(1f), isConnected) { onSendKey("KEY_GUIDE") }
                RemoteKey("INFO", Modifier.weight(1f), isConnected) { onSendKey("KEY_INFO") }
                RemoteKey("SOURCE", Modifier.weight(1f), isConnected) { onSendKey("KEY_SOURCE") }
                RemoteKey("MENU", Modifier.weight(1f), isConnected) { onSendKey("KEY_MENU_HOME") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(RemoteTokens.Space2)) {
                RemoteKey(stringResource(R.string.key_subtitles), Modifier.weight(1f), isConnected) { onSendKey("KEY_SUBTITLE") }
                RemoteKey(stringResource(R.string.key_picture_size), Modifier.weight(1f), isConnected) { onSendKey("KEY_PICTURE_SIZE") }
                RemoteKey("TOOLS", Modifier.weight(1f), isConnected) { onSendKey("KEY_TOOLS") }
            }
        }
        Spacer(Modifier.height(RemoteTokens.Space4))
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = RemoteType.Section, color = RemoteTokens.TextSecondary)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppsPage(
    isConnected: Boolean,
    installedApps: List<TvApp>,
    favoriteAppIds: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onLaunchApp: (String) -> Unit,
    onRefreshApps: () -> Unit,
    onSendKey: (String) -> Unit,
    onRunScene: (QuickAction) -> Unit
) {
    val context = LocalContext.current
    val apps = (installedApps.ifEmpty { FeaturedApps })
        .sortedByDescending { it.appId in favoriteAppIds }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = RemoteTokens.Space4, end = RemoteTokens.Space2),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                SectionLabel(stringResource(R.string.section_all_apps))
                if (favoriteAppIds.isEmpty()) {
                    Text(stringResource(R.string.apps_pin_hint), style = RemoteType.Caption, color = RemoteTokens.TextSecondary)
                }
            }
            IconButton(onClick = onRefreshApps, enabled = isConnected) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.cd_refresh_apps),
                    tint = if (isConnected) RemoteTokens.TextPrimary else RemoteTokens.TextDisabled
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(RemoteTokens.Space4),
            horizontalArrangement = Arrangement.spacedBy(RemoteTokens.Space2),
            verticalArrangement = Arrangement.spacedBy(RemoteTokens.Space2)
        ) {
            items(apps, key = { it.appId }) { app ->
                val shortcutLabel = stringResource(R.string.shortcut_label_open, app.name)
                AppTile(
                    app = app,
                    pinned = app.appId in favoriteAppIds,
                    enabled = isConnected,
                    onClick = { onLaunchApp(app.appId) },
                    onTogglePin = { onToggleFavorite(app.appId) },
                    onWakeAndOpen = { onRunScene(QuickAction.WakeAndApp(app.appId)) },
                    onAddShortcut = {
                        ShortcutPinner.pin(context, shortcutLabel, QuickAction.WakeAndApp(app.appId))
                    }
                )
            }
        }

        TransportBar(isConnected, onSendKey)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppTile(
    app: TvApp,
    pinned: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onWakeAndOpen: () -> Unit,
    onAddShortcut: () -> Unit
) {
    val haptics = LocalHapticsManager.current
    var menuOpen by remember { mutableStateOf(false) }
    val moreLabel = stringResource(R.string.more_actions)

    Box {
        Column(
            modifier = Modifier
                .heightIn(min = 96.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(RemoteTokens.RadiusKey))
                .background(if (pinned) RemoteTokens.AccentMuted else RemoteTokens.Surface2)
                .border(
                    1.dp,
                    if (pinned) RemoteTokens.Accent else RemoteTokens.Border,
                    RoundedCornerShape(RemoteTokens.RadiusKey)
                )
                .semantics { onLongClick(moreLabel) { menuOpen = true; true } }
                // The tile stays long-pressable while disconnected: scenes can wake the TV first.
                .combinedClickable(
                    onClick = { if (enabled) { haptics.performKeypress(); onClick() } },
                    onLongClick = { haptics.performToggle(); menuOpen = true }
                )
                .padding(RemoteTokens.Space3),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = app.name.firstOrNull()?.uppercase() ?: "?",
                style = RemoteType.Digit,
                color = if (enabled) (if (pinned) RemoteTokens.Accent else RemoteTokens.TextPrimary) else RemoteTokens.TextDisabled
            )
            Text(
                text = app.name,
                style = RemoteType.Caption,
                color = if (enabled) RemoteTokens.TextPrimary else RemoteTokens.TextDisabled,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        SceneMenu(
            expanded = menuOpen,
            onDismiss = { menuOpen = false },
            entries = listOf(
                (if (pinned) stringResource(R.string.app_unpin) else stringResource(R.string.app_pin)) to onTogglePin,
                stringResource(R.string.scene_wake_open) to onWakeAndOpen,
                stringResource(R.string.scene_pin_shortcut) to onAddShortcut
            )
        )
    }
}

/** Long-press menu shared by app tiles and input keys. */
@Composable
private fun SceneMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    entries: List<Pair<String, () -> Unit>>
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        entries.forEach { (label, action) ->
            DropdownMenuItem(
                text = { Text(label, style = RemoteType.Body, color = RemoteTokens.TextPrimary) },
                onClick = { onDismiss(); action() }
            )
        }
    }
}

/** Playback keys pinned under the app grid (sticky bar). */
@Composable
private fun TransportBar(isConnected: Boolean, onSendKey: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RemoteTokens.Surface1)
            .padding(RemoteTokens.Space3),
        horizontalArrangement = Arrangement.spacedBy(RemoteTokens.Space2)
    ) {
        val keyModifier = Modifier.weight(1f)
        RemoteIconKey(Icons.Default.SkipPrevious, stringResource(R.string.cd_previous), keyModifier, isConnected) { onSendKey("KEY_PREV") }
        RemoteIconKey(Icons.Default.FastRewind, stringResource(R.string.cd_rewind), keyModifier, isConnected, onRepeat = { onSendKey("KEY_REWIND") }) { onSendKey("KEY_REWIND") }
        RemoteIconKey(Icons.Default.PlayArrow, stringResource(R.string.cd_play_pause), keyModifier, isConnected) { onSendKey("KEY_PLAY_PAUSE") }
        RemoteIconKey(Icons.Default.FastForward, stringResource(R.string.cd_fast_forward), keyModifier, isConnected, onRepeat = { onSendKey("KEY_FF") }) { onSendKey("KEY_FF") }
        RemoteIconKey(Icons.Default.SkipNext, stringResource(R.string.cd_next), keyModifier, isConnected) { onSendKey("KEY_NEXT") }
        RemoteIconKey(Icons.Default.Stop, stringResource(R.string.cd_stop), keyModifier, isConnected) { onSendKey("KEY_STOP") }
    }
}

@Composable
private fun RemoteTabRail(selected: Int, onSelect: (Int) -> Unit) {
    val haptics = LocalHapticsManager.current
    val titles = listOf(
        stringResource(R.string.tab_control),
        stringResource(R.string.tab_touch),
        stringResource(R.string.tab_numbers),
        stringResource(R.string.tab_apps)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RemoteTokens.Background)
            .border(1.dp, RemoteTokens.Border)
    ) {
        titles.forEachIndexed { index, title ->
            val isSelected = index == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clickable { haptics.performClick(); onSelect(index) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(if (isSelected) RemoteTokens.Accent else Color.Transparent)
                )
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = title.uppercase(),
                        style = RemoteType.Legend,
                        color = if (isSelected) RemoteTokens.TextPrimary else RemoteTokens.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
