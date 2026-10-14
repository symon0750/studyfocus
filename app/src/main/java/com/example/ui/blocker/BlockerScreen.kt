package com.example.ui.blocker

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.local.entity.BlockSettingsEntity
import com.example.data.local.entity.BlockedAppEntity
import com.example.data.local.entity.BlockedWebsiteEntity
import com.example.ui.theme.BreakGreen
import com.example.ui.theme.PomodoroRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BlockerScreen(
    viewModel: BlockerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.blockSettings.collectAsState()
    val blockedApps by viewModel.blockedApps.collectAsState()
    val blockedWebsites by viewModel.blockedWebsites.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Re-checar permissões quando o usuário volta das configurações do Android
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(com.example.ui.theme.BackgroundDark)
    ) {
        // Cabeçalho da Tela com Botão de Teste
        BlockerHeader(
            isBlockingActive = settings.isBlockingEnabled,
            hasMissingPermission = !uiState.hasAccessibilityPermission || !uiState.hasUsageStatsPermission,
            onRefreshPermissions = { viewModel.checkPermissions() },
            onTestBlock = { viewModel.testBlockOverlay(context) }
        )

        // Banner de Alerta se Acessibilidade não estiver ativada
        if (!uiState.hasAccessibilityPermission) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("missing_accessibility_banner"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = PomodoroRed.copy(alpha = 0.12f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, PomodoroRed)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = PomodoroRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Acessibilidade necessária para bloqueio",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    Button(
                        onClick = { viewModel.requestAccessibilityPermission(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = PomodoroRed, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("activate_accessibility_banner_btn")
                    ) {
                        Text("Ativar", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // Seletor de Abas
        val tabs = listOf("Apps", "Sites", "Regras")
        ScrollableTabRow(
            selectedTabIndex = uiState.selectedTab,
            containerColor = SurfaceDark,
            contentColor = TextPrimary,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[uiState.selectedTab]),
                    color = PomodoroRed,
                    height = 2.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                val selected = uiState.selectedTab == index
                Tab(
                    selected = selected,
                    onClick = { viewModel.selectTab(index) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected) PomodoroRed else TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("blocker_tab_$index")
                )
            }
        }

        // Conteúdo da Aba Selecionada
        Box(modifier = Modifier.fillMaxSize()) {
            when (uiState.selectedTab) {
                0 -> AppsTabContent(
                    blockedApps = blockedApps,
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                    onToggleApp = { pkg, blocked -> viewModel.toggleAppBlock(pkg, blocked) },
                    onDeleteApp = { pkg -> viewModel.removeBlockedApp(pkg) },
                    onOpenAddDialog = { viewModel.setShowAddAppDialog(true) }
                )
                1 -> WebsitesTabContent(
                    blockedWebsites = blockedWebsites,
                    websiteInput = uiState.websiteInput,
                    websiteError = uiState.websiteInputError,
                    onWebsiteInputChange = { viewModel.onWebsiteInputChange(it) },
                    onAddWebsite = { viewModel.addWebsiteDomain() },
                    onToggleWebsite = { domain, blocked -> viewModel.toggleWebsiteBlock(domain, blocked) },
                    onDeleteWebsite = { domain -> viewModel.removeBlockedWebsite(domain) }
                )
                2 -> SettingsAndDiagnosticsTabContent(
                    settings = settings,
                    uiState = uiState,
                    onToggleBlocking = { viewModel.updateSettings(enabled = it) },
                    onToggleBreaks = { viewModel.updateSettings(blockDuringBreaks = it) },
                    onToggleStrict = { viewModel.updateSettings(strictMode = it) },
                    onRequestAccessibility = { viewModel.requestAccessibilityPermission(context) },
                    onRequestUsageStats = { viewModel.requestUsageStatsPermission(context) },
                    onRequestNotifications = { viewModel.requestNotificationPermission(context) }
                )
            }
        }
    }

    // Diálogo para Adicionar App Instalado
    if (uiState.showAddAppDialog) {
        AddInstalledAppDialog(
            installedApps = uiState.installedApps,
            alreadyBlockedPackages = blockedApps.map { it.packageName }.toSet(),
            isLoading = uiState.isInstalledAppsLoading,
            onDismiss = { viewModel.setShowAddAppDialog(false) },
            onSelectApp = { app -> viewModel.addInstalledAppToBlocklist(app) }
        )
    }
}

@Composable
private fun BlockerHeader(
    isBlockingActive: Boolean,
    hasMissingPermission: Boolean,
    onRefreshPermissions: () -> Unit,
    onTestBlock: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Foco",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isBlockingActive) BreakGreen else Color.Gray)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isBlockingActive) "Ativo" else "Pausado",
                    fontSize = 11.sp,
                    color = if (isBlockingActive) BreakGreen else TextMuted
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = onTestBlock,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("test_block_overlay_btn")
            ) {
                Icon(imageVector = Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Testar Tela", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onRefreshPermissions,
                modifier = Modifier.testTag("refresh_permissions_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Atualizar permissões",
                    tint = if (hasMissingPermission) Color(0xFFF59E0B) else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun AppsTabContent(
    blockedApps: List<BlockedAppEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onToggleApp: (String, Boolean) -> Unit,
    onDeleteApp: (String) -> Unit,
    onOpenAddDialog: () -> Unit
) {
    val filteredApps = remember(blockedApps, searchQuery) {
        if (searchQuery.isBlank()) blockedApps
        else blockedApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Ações do topo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Apps Bloqueados (${blockedApps.count { it.isBlocked }}/${blockedApps.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )

                Button(
                    onClick = onOpenAddDialog,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceCard,
                        contentColor = PomodoroRed
                    ),
                    modifier = Modifier.testTag("add_installed_app_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Adicionar App", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Buscar nos apps cadastrados...", color = TextMuted) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_blocked_apps_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PomodoroRed,
                    unfocusedBorderColor = SurfaceCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = SurfaceDark,
                    unfocusedContainerColor = SurfaceDark
                )
            )
        }

        if (filteredApps.isEmpty()) {
            item {
                EmptyStateCard(
                    message = if (searchQuery.isBlank()) "Nenhum aplicativo na lista de bloqueio." else "Nenhum app corresponde à busca."
                )
            }
        } else {
            items(filteredApps, key = { it.packageName }) { app ->
                BlockedAppCard(
                    app = app,
                    onToggle = { onToggleApp(app.packageName, app.isBlocked) },
                    onDelete = { onDeleteApp(app.packageName) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun BlockedAppCard(
    app: BlockedAppEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("blocked_app_item_${app.packageName}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (app.isBlocked) PomodoroRed.copy(alpha = 0.15f) else Color.DarkGray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = null,
                    tint = if (app.isBlocked) PomodoroRed else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${app.category} • ${app.packageName}",
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Switch(
                checked = app.isBlocked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PomodoroRed,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = SurfaceDark
                ),
                modifier = Modifier.testTag("app_switch_${app.packageName}")
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_app_${app.packageName}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remover app",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun WebsitesTabContent(
    blockedWebsites: List<BlockedWebsiteEntity>,
    websiteInput: String,
    websiteError: String?,
    onWebsiteInputChange: (String) -> Unit,
    onAddWebsite: () -> Unit,
    onToggleWebsite: (String, Boolean) -> Unit,
    onDeleteWebsite: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Sites restritos",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Formulário para adicionar domínio
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = websiteInput,
                            onValueChange = onWebsiteInputChange,
                            placeholder = { Text("ex: instagram.com", color = TextMuted, fontSize = 12.sp) },
                            singleLine = true,
                            isError = websiteError != null,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("website_domain_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PomodoroRed,
                                unfocusedBorderColor = SurfaceCardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = SurfaceDark,
                                unfocusedContainerColor = SurfaceDark
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onAddWebsite,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PomodoroRed,
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("add_website_button")
                        ) {
                            Text("Adicionar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (websiteError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = websiteError, color = PomodoroRed, fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Text(
                text = "Domínios Restritos (${blockedWebsites.count { it.isBlocked }})",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
        }

        if (blockedWebsites.isEmpty()) {
            item {
                EmptyStateCard(message = "Nenhum site cadastrado para bloqueio.")
            }
        } else {
            items(blockedWebsites, key = { it.domain }) { site ->
                BlockedWebsiteCard(
                    website = site,
                    onToggle = { onToggleWebsite(site.domain, site.isBlocked) },
                    onDelete = { onDeleteWebsite(site.domain) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun BlockedWebsiteCard(
    website: BlockedWebsiteEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("blocked_website_item_${website.domain}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (website.isBlocked) PomodoroRed.copy(alpha = 0.15f) else Color.DarkGray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = if (website.isBlocked) PomodoroRed else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = website.domain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = website.category,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Switch(
                checked = website.isBlocked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = PomodoroRed,
                    uncheckedThumbColor = TextMuted,
                    uncheckedTrackColor = SurfaceDark
                ),
                modifier = Modifier.testTag("website_switch_${website.domain}")
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag("delete_website_${website.domain}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remover domínio",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsAndDiagnosticsTabContent(
    settings: BlockSettingsEntity,
    uiState: BlockerUiState,
    onToggleBlocking: (Boolean) -> Unit,
    onToggleBreaks: (Boolean) -> Unit,
    onToggleStrict: (Boolean) -> Unit,
    onRequestAccessibility: () -> Unit,
    onRequestUsageStats: () -> Unit,
    onRequestNotifications: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Seção 1: Configurações Gerais
        item {
            Text(
                text = "Regras",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    SettingRow(
                        title = "Bloquear distrações",
                        checked = settings.isBlockingEnabled,
                        onCheckedChange = onToggleBlocking,
                        testTag = "toggle_main_blocking"
                    )

                    Divider(color = SurfaceCardBorder, modifier = Modifier.padding(vertical = 8.dp))

                    SettingRow(
                        title = "Bloquear nas pausas",
                        checked = settings.blockDuringBreaks,
                        onCheckedChange = onToggleBreaks,
                        testTag = "toggle_breaks_blocking"
                    )

                    Divider(color = SurfaceCardBorder, modifier = Modifier.padding(vertical = 8.dp))

                    SettingRow(
                        title = "Modo estrito",
                        checked = settings.strictMode,
                        onCheckedChange = onToggleStrict,
                        testTag = "toggle_strict_mode"
                    )
                }
            }
        }

        // Seção 2: Permissões
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Permissões",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            PermissionCard(
                title = "Acessibilidade",
                isGranted = uiState.hasAccessibilityPermission,
                actionLabel = "Ativar",
                onAction = onRequestAccessibility,
                testTag = "action_accessibility_perm"
            )
        }

        item {
            PermissionCard(
                title = "Acesso de uso",
                isGranted = uiState.hasUsageStatsPermission,
                actionLabel = "Ativar",
                onAction = onRequestUsageStats,
                testTag = "action_usage_stats_perm"
            )
        }

        item {
            PermissionCard(
                title = "Notificações",
                isGranted = uiState.hasNotificationPermission,
                actionLabel = "Habilitar",
                onAction = onRequestNotifications,
                testTag = "action_notification_perm"
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PomodoroRed,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = SurfaceDark
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String? = null,
    isGranted: Boolean,
    actionLabel: String,
    onAction: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGranted) BreakGreen.copy(alpha = 0.4f) else Color(0xFFF59E0B).copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.Check else Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = if (isGranted) BreakGreen else Color(0xFFF59E0B),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isGranted) BreakGreen.copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isGranted) "ATIVO" else "PENDENTE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isGranted) BreakGreen else Color(0xFFF59E0B)
                    )
                }
            }

            if (!description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, fontSize = 11.sp, color = TextMuted)
            }

            if (!isGranted) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(testTag)
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = actionLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = message, color = TextMuted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun AddInstalledAppDialog(
    installedApps: List<InstalledAppItem>,
    alreadyBlockedPackages: Set<String>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSelectApp: (InstalledAppItem) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val availableApps = remember(installedApps, alreadyBlockedPackages, query) {
        installedApps.filter { app ->
            !alreadyBlockedPackages.contains(app.packageName) &&
                    (query.isBlank() || app.appName.contains(query, ignoreCase = true) || app.packageName.contains(query, ignoreCase = true))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Selecionar Aplicativo Instalado", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Filtrar apps...", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PomodoroRed,
                        unfocusedBorderColor = SurfaceCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceDark,
                        unfocusedContainerColor = SurfaceDark
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PomodoroRed)
                    }
                } else if (availableApps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text(text = "Nenhum app disponível para adicionar.", color = TextMuted, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(modifier = Modifier.height(280.dp)) {
                        items(availableApps, key = { it.packageName }) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectApp(app) }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Apps, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = app.appName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(text = app.packageName, fontSize = 10.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            Divider(color = SurfaceCardBorder.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar", color = TextSecondary)
            }
        },
        containerColor = SurfaceCard,
        shape = RoundedCornerShape(16.dp)
    )
}
