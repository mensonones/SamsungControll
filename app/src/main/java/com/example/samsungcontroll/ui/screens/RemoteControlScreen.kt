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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.samsungcontroll.ConnectionState
import com.example.samsungcontroll.DiscoveredTv
import com.example.samsungcontroll.R
import com.example.samsungcontroll.RemoteViewModel
import com.example.samsungcontroll.TvDeviceInfo
import com.example.samsungcontroll.ui.animation.pressScale
import com.example.samsungcontroll.ui.components.AppLaunchButton
import com.example.samsungcontroll.ui.components.RemoteButton
import com.example.samsungcontroll.ui.components.RemoteIconButton
import com.example.samsungcontroll.ui.components.RemoteSmallButton
import com.example.samsungcontroll.ui.components.getEnabledColor
import com.example.samsungcontroll.ui.haptics.LocalHapticsManager
import com.example.samsungcontroll.ui.haptics.rememberHapticsManager
import kotlinx.coroutines.delay

@Composable
fun RemoteControlScreen(viewModel: RemoteViewModel) {
    val hapticsManager = rememberHapticsManager()

    CompositionLocalProvider(LocalHapticsManager provides hapticsManager) {
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
}

@Composable
private fun RemoteControlContent(viewModel: RemoteViewModel) {
    var showNicknameDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }

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
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RemoteHeader(
                connectionState = viewModel.connectionState,
                isDiscoveryActive = viewModel.showDiscovery,
                tvNickname = viewModel.tvNickname,
                ipAddress = viewModel.ipAddress,
                onToggleDiscovery = { viewModel.toggleDiscovery() },
                onReconnect = { viewModel.reconnect() },
                onEditNickname = { showNicknameDialog = true },
                onShowDetails = { showDetailsDialog = true }
            )

            ConnectionStatusBanner(
                connectionState = viewModel.connectionState,
                onReconnect = { viewModel.reconnect() }
            )

            if (viewModel.showDiscovery) {
                DiscoveryPanel(
                    isSearching = viewModel.isSearching,
                    discoveredTvs = viewModel.discoveredTvs,
                    currentConnectedIp = viewModel.ipAddress,
                    connectionState = viewModel.connectionState,
                    onSearch = { viewModel.searchTvs() },
                    onSelect = { tv -> viewModel.connectToTv(tv) },
                    onConnectManualIp = { manualIp -> viewModel.connectToTv(manualIp) }
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
                    text = "Informações da TV",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRow("Modelo da TV:", deviceInfo?.modelName ?: "Samsung Smart TV")
                DetailRow("Sistema Operacional:", deviceInfo?.os ?: "Tizen OS")
                if (!deviceInfo?.firmwareVersion.isNullOrBlank()) {
                    DetailRow("Firmware:", deviceInfo.firmwareVersion)
                }
                DetailRow("Tipo de Rede:", deviceInfo?.networkType ?: "Wi-Fi")
                DetailRow("Endereço IP:", ipAddress.ifBlank { "Não informado" })
                if (macAddress.isNotBlank()) {
                    DetailRow("Endereço MAC:", macAddress)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = CircleShape
            ) {
                Text("Fechar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
    val presets = listOf("TV da Sala", "TV do Quarto", "Varanda", "Cozinha")

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
                    text = "Apelidar esta TV",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Defina um nome personalizado para identificar esta TV:",
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
                Text("Sugestões rápidas:", color = Color(0xFFCBD5E1), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
                Text("Salvar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color(0xFF94A3B8), fontSize = 13.sp)
            }
        }
    )
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.app_logo_mark),
                contentDescription = "Samsung Control Logo",
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

@Composable
private fun RemoteHeader(
    connectionState: ConnectionState,
    isDiscoveryActive: Boolean,
    tvNickname: String,
    ipAddress: String,
    onToggleDiscovery: () -> Unit,
    onReconnect: () -> Unit,
    onEditNickname: () -> Unit,
    onShowDetails: () -> Unit
) {
    val haptics = LocalHapticsManager.current
    var showMenu by remember { mutableStateOf(false) }

    val (statusText, statusColor) = when (connectionState) {
        ConnectionState.DISCONNECTED -> "Desconectado" to Color(0xFF94A3B8)
        ConnectionState.CONNECTING -> "Conectando..." to Color(0xFF38BDF8)
        ConnectionState.WAITING_FOR_PERMISSION -> "Permita na TV" to Color(0xFFF59E0B)
        ConnectionState.CONNECTED -> "Conectado" to Color(0xFF22C55E)
        ConnectionState.FAILED -> "Falha na conexão" to Color(0xFFEF4444)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xEE111827)),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF38BDF8).copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF38BDF8).copy(alpha = 0.20f),
                                    Color(0xFF0284C7).copy(alpha = 0.08f)
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                Color(0xFF38BDF8).copy(alpha = 0.40f)
                            ),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.app_logo_mark),
                        contentDescription = "Samsung Logo",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    val titleText = if (tvNickname.isNotBlank()) tvNickname else "Samsung Control"
                    Text(
                        text = titleText,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        haptics.performClick()
                        onToggleDiscovery()
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDiscoveryActive) Color(0xFF0284C7).copy(alpha = 0.4f)
                            else Color.White.copy(alpha = 0.08f)
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                if (isDiscoveryActive) Color(0xFF38BDF8) else Color.Transparent
                            ),
                            CircleShape
                        )
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Buscar TV na rede",
                        tint = if (isDiscoveryActive) Color(0xFF38BDF8) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = {
                            haptics.performClick()
                            showMenu = true
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu de Opções",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .background(Color(0xFF1E293B))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(12.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Apelidar TV", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(Icons.Default.EditNote, contentDescription = null, tint = Color(0xFF38BDF8))
                            },
                            onClick = {
                                showMenu = false
                                onEditNickname()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Detalhes da TV", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF38BDF8))
                            },
                            onClick = {
                                showMenu = false
                                onShowDetails()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Reconectar TV", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF22C55E))
                            },
                            onClick = {
                                showMenu = false
                                onReconnect()
                            }
                        )
                        if (ipAddress.isNotBlank()) {
                            DropdownMenuItem(
                                text = { Text("IP: $ipAddress", color = Color(0xFF94A3B8), fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(Icons.Default.Tv, contentDescription = null, tint = Color(0xFF94A3B8))
                                },
                                onClick = { showMenu = false },
                                enabled = false
                            )
                        }
                    }
                }
            }
        }
    }
}

private const val SPLASH_DURATION_MS = 900L

@Composable
private fun ConnectionStatusBanner(
    connectionState: ConnectionState,
    onReconnect: () -> Unit
) {
    val haptics = LocalHapticsManager.current

    when (connectionState) {
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
                            "Permissão Requerida na TV",
                            color = Color(0xFFF59E0B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Por favor, aceite a solicitação de controle que apareceu na tela da sua TV Samsung.",
                            color = Color(0xFFFDE68A),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
        ConnectionState.FAILED -> {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF3B0707)),
                border = BorderStroke(1.dp, Color(0xFFEF4444))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text("❌", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Falha de Conexão",
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Verifique se a TV está ligada na mesma rede Wi-Fi.",
                                color = Color(0xFFFCA5A5),
                                fontSize = 11.sp
                            )
                        }
                    }
                    Button(
                        onClick = {
                            haptics.performClick()
                            onReconnect()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Tentar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
private fun DiscoveryPanel(
    isSearching: Boolean,
    discoveredTvs: List<DiscoveredTv>,
    currentConnectedIp: String,
    connectionState: ConnectionState,
    onSearch: () -> Unit,
    onSelect: (DiscoveredTv) -> Unit,
    onConnectManualIp: (String) -> Unit
) {
    val haptics = LocalHapticsManager.current
    var manualIpText by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xEE111827)),
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF38BDF8).copy(alpha = 0.4f),
                    Color.White.copy(alpha = 0.08f)
                )
            )
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Tv,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Buscar TV na Rede",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Button(
                    onClick = {
                        haptics.performClick()
                        onSearch()
                    },
                    enabled = !isSearching,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text(
                        text = if (isSearching) "Buscando..." else "Buscar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Varrendo a rede em busca de Smart TVs...",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (discoveredTvs.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B).copy(alpha = 0.6f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nenhuma TV encontrada automaticamente.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Verifique se a TV está ligada na mesma rede Wi-Fi.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!showManualInput) {
                        OutlinedButton(
                            onClick = { showManualInput = true },
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f))
                        ) {
                            Text("Digitar IP da TV manualmente", color = Color(0xFF38BDF8), fontSize = 12.sp)
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
                                placeholder = { Text("Ex: 192.168.1.100", fontSize = 12.sp, color = Color(0xFF64748B)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
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
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                modifier = Modifier.height(54.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text("Conectar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 220.dp),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(discoveredTvs) { tv ->
                        val isThisTvConnected = tv.ip == currentConnectedIp && connectionState == ConnectionState.CONNECTED
                        val isThisTvConnecting = tv.ip == currentConnectedIp && connectionState == ConnectionState.CONNECTING

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptics.performClick()
                                    onSelect(tv)
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isThisTvConnected) Color(0xFF1E3A2B) else Color(0xFF1E293B)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isThisTvConnected) Color(0xFF22C55E) else Color.White.copy(alpha = 0.12f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isThisTvConnected) Color(0xFF22C55E).copy(alpha = 0.2f)
                                                else Color.White.copy(alpha = 0.08f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Tv,
                                            contentDescription = null,
                                            tint = if (isThisTvConnected) Color(0xFF22C55E) else Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        val displayName = if (tv.name.startsWith("(") || tv.name.isBlank()) "Smart TV" else tv.name
                                        Text(
                                            displayName,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            tv.ip,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                }

                                when {
                                    isThisTvConnected -> {
                                        Text(
                                            "Conectado ✓",
                                            color = Color(0xFF22C55E),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    isThisTvConnecting -> {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(12.dp),
                                                strokeWidth = 1.5.dp,
                                                color = Color(0xFF38BDF8)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                "Conectando...",
                                                color = Color(0xFF38BDF8),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    else -> {
                                        Text(
                                            "Conectar",
                                            color = Color(0xFF38BDF8),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
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
    val haptics = LocalHapticsManager.current
    val isConnected = state == ConnectionState.CONNECTED
    var isTransmitting by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    fun triggerAction(action: () -> Unit) {
        isTransmitting = true
        action()
    }

    LaunchedEffect(isTransmitting) {
        if (isTransmitting) {
            delay(150)
            isTransmitting = false
        }
    }

    Card(
        modifier = Modifier
            .widthIn(max = 390.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(44.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        border = BorderStroke(
            1.5.dp,
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.22f),
                    Color.White.copy(alpha = 0.04f)
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(Color(0xFF182235), Color(0xFF0F172A), Color(0xFF090E18))))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Signal LED Emitter Indicator
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(
                        if (isTransmitting) Color(0xFF38BDF8) else Color(0xFF1E293B)
                    )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Switcher Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F172A))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(20.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val tabs = listOf("Controle", "Números", "Mídia & Apps")
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) Color(0xFF0284C7) else Color.Transparent
                            )
                            .clickable {
                                haptics.performClick()
                                selectedTab = index
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            when (selectedTab) {
                0 -> {
                    // TAB 0: Main Remote Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RemoteButton(
                            icon = Icons.Default.PowerSettingsNew,
                            contentDescription = "Ligar ou desligar TV",
                            color = Color(0xFFE11D48),
                            onClick = { triggerAction(onPower) },
                            size = 56.dp
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SMART TV", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp)
                            Text("Remote Control", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        RemoteSmallButton("123", enabled = isConnected) {
                            triggerAction { onSendKey("KEY_123") }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    RemoteSection(title = "Navegação", horizontalAlignment = Alignment.CenterHorizontally) {
                        DPad(
                            enabled = isConnected,
                            onUp = { triggerAction { onSendKey("KEY_UP") } },
                            onDown = { triggerAction { onSendKey("KEY_DOWN") } },
                            onLeft = { triggerAction { onSendKey("KEY_LEFT") } },
                            onRight = { triggerAction { onSendKey("KEY_RIGHT") } },
                            onOk = { triggerAction { onSendKey("KEY_ENTER") } }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RemoteSmallButton("BACK", enabled = isConnected, modifier = Modifier.weight(1f)) { triggerAction { onSendKey("KEY_RETURN") } }
                        RemoteSmallButton("HOME", enabled = isConnected, modifier = Modifier.weight(1f)) { triggerAction { onSendKey("KEY_HOME") } }
                        RemoteSmallButton("EXIT", enabled = isConnected, modifier = Modifier.weight(1f)) { triggerAction { onSendKey("KEY_EXIT") } }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        RemoteVerticalControl(
                            plusIcon = Icons.Default.Add,
                            minusIcon = Icons.Default.Remove,
                            label = "VOL",
                            enabled = isConnected,
                            modifier = Modifier.weight(1f),
                            plusContentDescription = "Aumentar volume",
                            minusContentDescription = "Diminuir volume",
                            onPlus = { triggerAction { onSendKey("KEY_VOLUP") } },
                            onMinus = { triggerAction { onSendKey("KEY_VOLDOWN") } }
                        )
                        RemoteActionCard(
                            label = "MUTE",
                            enabled = isConnected,
                            isActive = isMuted,
                            modifier = Modifier.weight(0.9f),
                            onClick = { triggerAction(onToggleMute) }
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
                            onPlus = { triggerAction { onSendKey("KEY_CHUP") } },
                            onMinus = { triggerAction { onSendKey("KEY_CHDOWN") } }
                        )
                    }
                }
                1 -> {
                    // TAB 1: Keypad 0-9 & Guide / Source / HDMI / Utility Keys
                    RemoteSection(title = "Teclado Numérico", horizontalAlignment = Alignment.CenterHorizontally) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            val keypadRows = listOf(
                                listOf("1" to "KEY_1", "2" to "KEY_2", "3" to "KEY_3"),
                                listOf("4" to "KEY_4", "5" to "KEY_5", "6" to "KEY_6"),
                                listOf("7" to "KEY_7", "8" to "KEY_8", "9" to "KEY_9"),
                                listOf("Pre-Ch" to "KEY_PRECH", "0" to "KEY_0", "123" to "KEY_123")
                            )

                            keypadRows.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    row.forEach { (label, key) ->
                                        RemoteRoundTextButton(
                                            label = label,
                                            enabled = isConnected,
                                            onClick = { triggerAction { onSendKey(key) } },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    RemoteSection(title = "Entradas / HDMI") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RemoteSmallButton("HDMI 1", enabled = isConnected, modifier = Modifier.weight(1f)) { triggerAction { onSendKey("KEY_HDMI1") } }
                            RemoteSmallButton("HDMI 2", enabled = isConnected, modifier = Modifier.weight(1f)) { triggerAction { onSendKey("KEY_HDMI2") } }
                            RemoteSmallButton("HDMI 3", enabled = isConnected, modifier = Modifier.weight(1f)) { triggerAction { onSendKey("KEY_HDMI3") } }
                            RemoteSmallButton("TV", enabled = isConnected, modifier = Modifier.weight(1f)) { triggerAction { onSendKey("KEY_TV") } }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    RemoteSection(title = "Funções Avançadas da TV") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RemoteSmallButton("GUIDE", enabled = isConnected, modifier = Modifier.weight(1f)) { triggerAction { onSendKey("KEY_GUIDE") } }
                                RemoteSmallButton("INFO", enabled = isConnected, modifier = Modifier.weight(1f)) { triggerAction { onSendKey("KEY_INFO") } }
                                RemoteSmallButton("SOURCE", enabled = isConnected, modifier = Modifier.weight(1f)) { triggerAction { onSendKey("KEY_SOURCE") } }
                                RemoteSmallButton("MENU", enabled = isConnected, modifier = Modifier.weight(1f)) { triggerAction { onSendKey("KEY_MENU_HOME") } }
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RemoteSmallButton("LEGENDA", enabled = isConnected, modifier = Modifier.weight(1f)) { triggerAction { onSendKey("KEY_SUBTITLE") } }
                                RemoteSmallButton("TELA 16:9", enabled = isConnected, modifier = Modifier.weight(1f)) { triggerAction { onSendKey("KEY_PICTURE_SIZE") } }
                                RemoteSmallButton("TOOLS", enabled = isConnected, modifier = Modifier.weight(1f)) { triggerAction { onSendKey("KEY_TOOLS") } }
                            }
                        }
                    }
                }
                2 -> {
                    // TAB 2: Media Controls & Expanded Apps
                    RemoteSection(title = "Controle de Mídia") {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { triggerAction { onSendKey("KEY_PREV") } },
                                enabled = isConnected,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isConnected) Color(0xFF1F2937) else Color(0xFF111827))
                            ) {
                                Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = getEnabledColor(isConnected))
                            }
                            IconButton(
                                onClick = { triggerAction { onSendKey("KEY_REWIND") } },
                                enabled = isConnected,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isConnected) Color(0xFF1F2937) else Color(0xFF111827))
                            ) {
                                Icon(Icons.Default.FastRewind, contentDescription = "Retroceder", tint = getEnabledColor(isConnected))
                            }
                            IconButton(
                                onClick = { triggerAction { onSendKey("KEY_PLAY_PAUSE") } },
                                enabled = isConnected,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isConnected) Color(0xFF0284C7) else Color(0xFF111827))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Icon(Icons.Default.Pause, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                            IconButton(
                                onClick = { triggerAction { onSendKey("KEY_FF") } },
                                enabled = isConnected,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isConnected) Color(0xFF1F2937) else Color(0xFF111827))
                            ) {
                                Icon(Icons.Default.FastForward, contentDescription = "Avançar", tint = getEnabledColor(isConnected))
                            }
                            IconButton(
                                onClick = { triggerAction { onSendKey("KEY_NEXT") } },
                                enabled = isConnected,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isConnected) Color(0xFF1F2937) else Color(0xFF111827))
                            ) {
                                Icon(Icons.Default.SkipNext, contentDescription = "Próximo", tint = getEnabledColor(isConnected))
                            }
                            IconButton(
                                onClick = { triggerAction { onSendKey("KEY_STOP") } },
                                enabled = isConnected,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isConnected) Color(0xFF1F2937) else Color(0xFF111827))
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Parar", tint = getEnabledColor(isConnected))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    RemoteSection(title = "Aplicativos Expandidos") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                AppLaunchButton("Netflix", Color(0xFFE50914), isConnected, modifier = Modifier.weight(1f)) {
                                    triggerAction { onLaunchApp("3201907018807") }
                                }
                                AppLaunchButton("YouTube", Color.White, isConnected, textColor = Color.Black, modifier = Modifier.weight(1f)) {
                                    triggerAction { onLaunchApp("111299001912") }
                                }
                                AppLaunchButton("Prime", Color(0xFF00A8E1), isConnected, modifier = Modifier.weight(1f)) {
                                    triggerAction { onLaunchApp("3201910019365") }
                                }
                                AppLaunchButton("Disney+", Color(0xFF113CCF), isConnected, modifier = Modifier.weight(1f)) {
                                    triggerAction { onLaunchApp("3201907018784") }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                AppLaunchButton("Max", Color(0xFF002BE7), isConnected, modifier = Modifier.weight(1f)) {
                                    triggerAction { onLaunchApp("3202103023447") }
                                }
                                AppLaunchButton("Spotify", Color(0xFF1DB954), isConnected, modifier = Modifier.weight(1f)) {
                                    triggerAction { onLaunchApp("3201606009684") }
                                }
                                AppLaunchButton("Globo", Color(0xFFFF0000), isConnected, modifier = Modifier.weight(1f)) {
                                    triggerAction { onLaunchApp("3201601007250") }
                                }
                                AppLaunchButton("AppleTV", Color(0xFF333333), isConnected, modifier = Modifier.weight(1f)) {
                                    triggerAction { onLaunchApp("3201807016597") }
                                }
                            }
                        }
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
    val haptics = LocalHapticsManager.current
    val okInter = remember { MutableInteractionSource() }
    val okPressed by okInter.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .size(210.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF263347), Color(0xFF161E2E), Color(0xFF0F172A))
                )
            )
            .border(
                BorderStroke(
                    1.5.dp,
                    Brush.linearGradient(
                        listOf(Color(0xFF38BDF8).copy(alpha = 0.4f), Color.White.copy(alpha = 0.12f), Color(0xFF38BDF8).copy(alpha = 0.4f))
                    )
                ),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        RemoteIconButton(Icons.Default.ArrowDropUp, "Navegar para cima", Modifier.align(Alignment.TopCenter).padding(top = 4.dp), enabled, onUp)
        RemoteIconButton(Icons.Default.ArrowDropDown, "Navegar para baixo", Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp), enabled, onDown)
        RemoteIconButton(Icons.AutoMirrored.Filled.ArrowLeft, "Navegar para esquerda", Modifier.align(Alignment.CenterStart).padding(start = 4.dp), enabled, onLeft)
        RemoteIconButton(Icons.AutoMirrored.Filled.ArrowRight, "Navegar para direita", Modifier.align(Alignment.CenterEnd).padding(end = 4.dp), enabled, onRight)
        Box(
            modifier = Modifier
                .size(76.dp)
                .pressScale(isPressed = okPressed, pressedScale = 0.90f)
                .clip(CircleShape)
                .background(
                    if (enabled) {
                        if (okPressed) Brush.radialGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1)))
                        else Brush.radialGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                    } else Brush.radialGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                )
                .border(
                    BorderStroke(
                        1.dp,
                        if (okPressed) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.12f)
                    ),
                    CircleShape
                )
                .clickable(
                    enabled = enabled,
                    interactionSource = okInter,
                    indication = ripple()
                ) {
                    haptics.performClick()
                    onOk()
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "OK",
                color = getEnabledColor(enabled),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = 1.sp
            )
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
    val haptics = LocalHapticsManager.current
    val inter = remember { MutableInteractionSource() }
    val isPressed by inter.collectIsPressedAsState()

    Column(
        modifier = modifier
            .height(110.dp)
            .pressScale(isPressed = isPressed, pressedScale = 0.95f)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) Color(0xFF1D283A) else Color(0xFF1F2937))
            .border(
                BorderStroke(1.dp, if (isActive) Color(0xFFF59E0B).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f)),
                RoundedCornerShape(20.dp)
            )
            .clickable(
                enabled = enabled,
                interactionSource = inter,
                indication = ripple()
            ) {
                haptics.performToggle()
                onClick()
            }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon(isActive)
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = if (isActive) Color(0xFFF59E0B) else getEnabledColor(enabled), fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RemoteRoundTextButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticsManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .size(54.dp)
            .pressScale(isPressed = isPressed, pressedScale = 0.92f)
            .clip(CircleShape)
            .background(if (enabled) Color(0xFF1F2937) else Color(0xFF111827))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                enabled = enabled
            ) {
                haptics.performKeypress()
                onClick()
            },
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
    val haptics = LocalHapticsManager.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(
                if (enabled) Brush.verticalGradient(listOf(Color(0xFF1F2937), Color(0xFF111827)))
                else Brush.verticalGradient(listOf(Color(0xFF111827), Color(0xFF0B101D)))
            )
            .border(
                BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
                RoundedCornerShape(32.dp)
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val plusInter = remember { MutableInteractionSource() }
        val plusPressed by plusInter.collectIsPressedAsState()
        IconButton(
            onClick = {
                haptics.performKeypress()
                onPlus()
            },
            enabled = enabled,
            interactionSource = plusInter,
            modifier = Modifier.pressScale(isPressed = plusPressed, pressedScale = 0.85f)
        ) {
            Icon(plusIcon, plusContentDescription, tint = if (plusPressed) Color(0xFF38BDF8) else getEnabledColor(enabled))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.08f))
        )

        Spacer(modifier = Modifier.height(2.dp))
        Text(label, color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.08f))
        )

        val minusInter = remember { MutableInteractionSource() }
        val minusPressed by minusInter.collectIsPressedAsState()
        IconButton(
            onClick = {
                haptics.performKeypress()
                onMinus()
            },
            enabled = enabled,
            interactionSource = minusInter,
            modifier = Modifier.pressScale(isPressed = minusPressed, pressedScale = 0.85f)
        ) {
            Icon(minusIcon, minusContentDescription, tint = if (minusPressed) Color(0xFF38BDF8) else getEnabledColor(enabled))
        }
    }
}
