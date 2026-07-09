package com.example.samsungcontroll.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.samsungcontroll.ConnectionState
import com.example.samsungcontroll.DiscoveredTv
import com.example.samsungcontroll.R
import com.example.samsungcontroll.RemoteViewModel
import com.example.samsungcontroll.ui.components.AppLaunchButton
import com.example.samsungcontroll.ui.components.RemoteButton
import com.example.samsungcontroll.ui.components.RemoteIconButton
import com.example.samsungcontroll.ui.components.RemoteSmallButton
import com.example.samsungcontroll.ui.components.getEnabledColor
import kotlinx.coroutines.delay

@Composable
fun RemoteControlScreen(viewModel: RemoteViewModel) {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.initialize()
        delay(SPLASH_DURATION_MS)
        showSplash = false
    }

    if (showSplash) {
        BrandedSplash()
    } else {
        RemoteControlContent(viewModel)
    }
}

@Composable
private fun RemoteControlContent(viewModel: RemoteViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF101A33), Color(0xFF0B1020), Color(0xFF080A0F))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RemoteHeader(
                connectionState = viewModel.connectionState,
                onToggleDiscovery = { viewModel.toggleDiscovery() },
                onReconnect = { viewModel.reconnect() }
            )

            if (viewModel.showDiscovery) {
                DiscoveryPanel(
                    isSearching = viewModel.isSearching,
                    discoveredTvs = viewModel.discoveredTvs,
                    onSearch = { viewModel.searchTvs() },
                    onSelect = { tv -> viewModel.connectToTv(tv) }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            RemoteBody(
                state = viewModel.connectionState,
                isMuted = viewModel.isMuted,
                onPower = { viewModel.togglePower() },
                onToggleMute = { viewModel.toggleMute() },
                onSendKey = { viewModel.sendKey(it) },
                onLaunchApp = { viewModel.launchApp(it) }
            )
        }
    }
}

@Composable
private fun BrandedSplash() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF101A33), Color(0xFF07101F), Color(0xFF03060D))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.no_bg_logo),
            contentDescription = "Samsung Control",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .height(220.dp)
        )
    }
}

@Composable
private fun RemoteHeader(
    connectionState: ConnectionState,
    onToggleDiscovery: () -> Unit,
    onReconnect: () -> Unit
) {
    val (statusText, statusColor) = when (connectionState) {
        ConnectionState.DISCONNECTED -> "Desconectado" to Color(0xFF94A3B8)
        ConnectionState.CONNECTING -> "Conectando..." to Color(0xFF38BDF8)
        ConnectionState.WAITING_FOR_PERMISSION -> "Permita na TV" to Color(0xFFF59E0B)
        ConnectionState.CONNECTED -> "Conectado" to Color(0xFF22C55E)
        ConnectionState.FAILED -> "Falha na conexão" to Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xCC111827)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onToggleDiscovery,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Icon(Icons.Default.Search, contentDescription = "Buscar TV", tint = Color.White)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.no_bg_logo),
                    contentDescription = "Samsung Control",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .width(178.dp)
                        .height(46.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusChip(text = statusText, color = statusColor)
            }

            IconButton(
                onClick = onReconnect,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reconectar", tint = Color.White)
            }
        }
    }
}

private const val SPLASH_DURATION_MS = 900L

@Composable
private fun StatusChip(text: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DiscoveryPanel(
    isSearching: Boolean,
    discoveredTvs: List<DiscoveredTv>,
    onSearch: () -> Unit,
    onSelect: (DiscoveredTv) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xEE111827)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Buscar TV na Rede",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = onSearch, enabled = !isSearching, shape = CircleShape) {
                    if (isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("Buscar")
                    }
                }
            }
            if (discoveredTvs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 180.dp)) {
                    items(discoveredTvs) { tv ->
                        ListItem(
                            headlineContent = { Text(tv.name) },
                            supportingContent = { Text(tv.ip) },
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSelect(tv) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoteBody(
    state: ConnectionState,
    isMuted: Boolean,
    onPower: () -> Unit,
    onToggleMute: () -> Unit,
    onSendKey: (String) -> Unit,
    onLaunchApp: (String) -> Unit
) {
    val isConnected = state == ConnectionState.CONNECTED

    Card(
        modifier = Modifier
            .widthIn(max = 390.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(44.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.10f))
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(Color(0xFF182235), Color(0xFF0F172A), Color(0xFF090E18))))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RemoteButton(
                    icon = Icons.Default.PowerSettingsNew,
                    contentDescription = "Ligar ou desligar TV",
                    color = Color(0xFFE11D48),
                    onClick = onPower,
                    size = 60.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("SMART TV", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Remote Control", color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
                RemoteRoundTextButton(label = "123", enabled = isConnected, onClick = { onSendKey("KEY_123") })
            }

            Spacer(modifier = Modifier.height(22.dp))

            RemoteSection(title = "Navegação", horizontalAlignment = Alignment.CenterHorizontally) {
                DPad(
                    enabled = isConnected,
                    onUp = { onSendKey("KEY_UP") },
                    onDown = { onSendKey("KEY_DOWN") },
                    onLeft = { onSendKey("KEY_LEFT") },
                    onRight = { onSendKey("KEY_RIGHT") },
                    onOk = { onSendKey("KEY_ENTER") }
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RemoteSmallButton("BACK", enabled = isConnected, modifier = Modifier.weight(1f)) { onSendKey("KEY_RETURN") }
                RemoteSmallButton("HOME", enabled = isConnected, modifier = Modifier.weight(1f)) { onSendKey("KEY_HOME") }
                RemoteSmallButton("EXIT", enabled = isConnected, modifier = Modifier.weight(1f)) { onSendKey("KEY_EXIT") }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                RemoteVerticalControl(
                    plusIcon = Icons.Default.Add,
                    minusIcon = Icons.Default.Remove,
                    label = "VOL",
                    enabled = isConnected,
                    modifier = Modifier.weight(1f),
                    plusContentDescription = "Aumentar volume",
                    minusContentDescription = "Diminuir volume",
                    onPlus = { onSendKey("KEY_VOLUP") },
                    onMinus = { onSendKey("KEY_VOLDOWN") }
                )
                RemoteActionCard(
                    label = "MUTE",
                    enabled = isConnected,
                    isActive = isMuted,
                    modifier = Modifier.weight(0.9f),
                    onClick = onToggleMute
                ) { active ->
                    Icon(
                        if (active) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (active) "Som mutado" else "Som ligado",
                        tint = if (active) Color(0xFFF59E0B) else getEnabledColor(isConnected)
                    )
                }
                RemoteVerticalControl(
                    plusIcon = Icons.Default.KeyboardArrowUp,
                    minusIcon = Icons.Default.KeyboardArrowDown,
                    label = "CH",
                    enabled = isConnected,
                    modifier = Modifier.weight(1f),
                    plusContentDescription = "Próximo canal",
                    minusContentDescription = "Canal anterior",
                    onPlus = { onSendKey("KEY_CHUP") },
                    onMinus = { onSendKey("KEY_CHDOWN") }
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            RemoteSection(title = "Apps") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AppLaunchButton("Netflix", Color(0xFFE50914), isConnected, modifier = Modifier.weight(1f)) {
                        onLaunchApp("3201907018807")
                    }
                    AppLaunchButton("YouTube", Color.White, isConnected, textColor = Color.Black, modifier = Modifier.weight(1f)) {
                        onLaunchApp("111299001912")
                    }
                    AppLaunchButton("Prime", Color(0xFF00A8E1), isConnected, modifier = Modifier.weight(1f)) {
                        onLaunchApp("3201910019365")
                    }
                }
            }
        }
    }
}

@Composable
private fun DPad(
    enabled: Boolean,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onOk: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(200.dp)
            .clip(CircleShape)
            .background(Color(0xFF1F2937)),
        contentAlignment = Alignment.Center
    ) {
        RemoteIconButton(Icons.Default.ArrowDropUp, "Navegar para cima", Modifier.align(Alignment.TopCenter), enabled, onUp)
        RemoteIconButton(Icons.Default.ArrowDropDown, "Navegar para baixo", Modifier.align(Alignment.BottomCenter), enabled, onDown)
        RemoteIconButton(Icons.AutoMirrored.Filled.ArrowLeft, "Navegar para esquerda", Modifier.align(Alignment.CenterStart), enabled, onLeft)
        RemoteIconButton(Icons.AutoMirrored.Filled.ArrowRight, "Navegar para direita", Modifier.align(Alignment.CenterEnd), enabled, onRight)
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(if (enabled) Color(0xFF334155) else Color(0xFF1F2937))
                .clickable(enabled) { onOk() },
            contentAlignment = Alignment.Center
        ) {
            Text("OK", color = getEnabledColor(enabled), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RemoteSection(
    title: String,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = horizontalAlignment) {
        Text(text = title.uppercase(), color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun RemoteActionCard(
    label: String,
    enabled: Boolean,
    isActive: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    icon: @Composable (Boolean) -> Unit
) {
    Column(
        modifier = modifier
            .height(110.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) Color(0xFF1D283A) else Color(0xFF1F2937))
            .clickable(enabled) { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon(isActive)
        Text(label, color = if (isActive) Color(0xFFF59E0B) else getEnabledColor(enabled), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RemoteRoundTextButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    Box(
        modifier = Modifier
            .size(54.dp)
            .graphicsLayer(scaleX = if (isPressed) 0.92f else 1f, scaleY = if (isPressed) 0.92f else 1f)
            .clip(CircleShape)
            .background(if (enabled) Color(0xFF1F2937) else Color(0xFF111827))
            .clickable(interactionSource = interactionSource, indication = ripple(), enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = getEnabledColor(enabled), fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun RemoteVerticalControl(
    plusIcon: ImageVector,
    minusIcon: ImageVector,
    label: String,
    enabled: Boolean,
    modifier: Modifier,
    plusContentDescription: String,
    minusContentDescription: String,
    onPlus: () -> Unit,
    onMinus: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(if (enabled) Color(0xFF1F2937) else Color(0xFF111827))
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val plusInter = remember { MutableInteractionSource() }
        val plusPressed by plusInter.collectIsPressedAsState()
        IconButton(onClick = onPlus, enabled = enabled, interactionSource = plusInter) {
            Icon(plusIcon, plusContentDescription, tint = if (plusPressed) Color.White else getEnabledColor(enabled))
        }
        Text(label, color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        val minusInter = remember { MutableInteractionSource() }
        val minusPressed by minusInter.collectIsPressedAsState()
        IconButton(onClick = onMinus, enabled = enabled, interactionSource = minusInter) {
            Icon(minusIcon, minusContentDescription, tint = if (minusPressed) Color.White else getEnabledColor(enabled))
        }
    }
}
