package com.example.ui.blocker

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BlockSettingsEntity
import com.example.data.local.entity.BlockedAppEntity
import com.example.data.local.entity.BlockedWebsiteEntity
import com.example.data.repository.StudyRepository
import com.example.service.FocusBlockAccessibilityService
import com.example.service.FocusBlockManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean
)

data class BlockerUiState(
    val hasAccessibilityPermission: Boolean = false,
    val hasUsageStatsPermission: Boolean = false,
    val hasNotificationPermission: Boolean = false,
    val installedApps: List<InstalledAppItem> = emptyList(),
    val isInstalledAppsLoading: Boolean = false,
    val searchQuery: String = "",
    val websiteInput: String = "",
    val websiteInputError: String? = null,
    val selectedTab: Int = 0, // 0: Apps, 1: Sites, 2: Configurações & Diagnóstico
    val showAddAppDialog: Boolean = false
)

class BlockerViewModel(
    application: Application,
    private val repository: StudyRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(BlockerUiState())
    val uiState: StateFlow<BlockerUiState> = _uiState.asStateFlow()

    val blockSettings: StateFlow<BlockSettingsEntity> = repository.blockSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BlockSettingsEntity()
        )

    val blockedApps: StateFlow<List<BlockedAppEntity>> = repository.allBlockedApps
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val blockedWebsites: StateFlow<List<BlockedWebsiteEntity>> = repository.allBlockedWebsites
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        checkPermissions()
        loadInstalledApps()

        // Sincroniza dados com o FocusBlockManager em tempo real
        viewModelScope.launch {
            launch {
                blockSettings.collectLatest { syncToRuntime() }
            }
            launch {
                blockedApps.collectLatest { syncToRuntime() }
            }
            launch {
                blockedWebsites.collectLatest { syncToRuntime() }
            }
        }
    }

    private fun syncToRuntime() {
        val settings = blockSettings.value
        val activeApps = blockedApps.value.filter { it.isBlocked }.map { it.packageName }.toSet()
        val activeSites = blockedWebsites.value.filter { it.isBlocked }.map { it.domain }.toSet()

        FocusBlockManager.updateBlockerData(
            enabled = settings.isBlockingEnabled,
            blockDuringBreaks = settings.blockDuringBreaks,
            strictMode = settings.strictMode,
            blockedPackages = activeApps,
            blockedDomains = activeSites
        )
    }

    fun checkPermissions() {
        val context = getApplication<Application>()
        _uiState.update {
            it.copy(
                hasAccessibilityPermission = FocusBlockManager.isAccessibilityServiceEnabled(
                    context,
                    FocusBlockAccessibilityService::class.java
                ),
                hasUsageStatsPermission = FocusBlockManager.hasUsageStatsPermission(context),
                hasNotificationPermission = FocusBlockManager.canPostNotifications(context)
            )
        }
    }

    fun selectTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedTab = tabIndex) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onWebsiteInputChange(input: String) {
        _uiState.update { it.copy(websiteInput = input, websiteInputError = null) }
    }

    fun toggleAppBlock(packageName: String, currentBlocked: Boolean) {
        viewModelScope.launch {
            repository.setAppBlockedStatus(packageName, !currentBlocked)
        }
    }

    fun removeBlockedApp(packageName: String) {
        viewModelScope.launch {
            repository.removeBlockedApp(packageName)
        }
    }

    fun addInstalledAppToBlocklist(app: InstalledAppItem) {
        viewModelScope.launch {
            repository.saveBlockedApp(
                BlockedAppEntity(
                    packageName = app.packageName,
                    appName = app.appName,
                    isBlocked = true,
                    category = if (app.isSystemApp) "Sistema" else "Instalado"
                )
            )
            _uiState.update { it.copy(showAddAppDialog = false) }
        }
    }

    fun toggleWebsiteBlock(domain: String, currentBlocked: Boolean) {
        viewModelScope.launch {
            repository.setWebsiteBlockedStatus(domain, !currentBlocked)
        }
    }

    fun removeBlockedWebsite(domain: String) {
        viewModelScope.launch {
            repository.removeBlockedWebsite(domain)
        }
    }

    fun addWebsiteDomain() {
        val raw = _uiState.value.websiteInput.trim()
        if (raw.isBlank()) {
            _uiState.update { it.copy(websiteInputError = "Digite um domínio válido (ex: instagram.com)") }
            return
        }

        val domain = FocusBlockManager.extractDomain(raw)
        if (domain.isNullOrBlank() || !domain.contains(".")) {
            _uiState.update { it.copy(websiteInputError = "Formato de domínio inválido (ex: youtube.com)") }
            return
        }

        viewModelScope.launch {
            repository.saveBlockedWebsite(
                BlockedWebsiteEntity(
                    domain = domain,
                    isBlocked = true,
                    category = "Personalizado"
                )
            )
            _uiState.update { it.copy(websiteInput = "", websiteInputError = null) }
        }
    }

    fun updateSettings(
        enabled: Boolean = blockSettings.value.isBlockingEnabled,
        blockDuringBreaks: Boolean = blockSettings.value.blockDuringBreaks,
        strictMode: Boolean = blockSettings.value.strictMode
    ) {
        viewModelScope.launch {
            val updated = blockSettings.value.copy(
                isBlockingEnabled = enabled,
                blockDuringBreaks = blockDuringBreaks,
                strictMode = strictMode
            )
            repository.updateBlockSettings(updated)
        }
    }

    fun setShowAddAppDialog(show: Boolean) {
        _uiState.update { it.copy(showAddAppDialog = show) }
    }

    private fun loadInstalledApps() {
        _uiState.update { it.copy(isInstalledAppsLoading = true) }
        viewModelScope.launch {
            val context = getApplication<Application>()
            val pm = context.packageManager
            val apps = withContext(Dispatchers.IO) {
                try {
                    val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                    packages.mapNotNull { appInfo ->
                        // Ignorar o próprio app
                        if (appInfo.packageName == context.packageName) return@mapNotNull null

                        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        val name = pm.getApplicationLabel(appInfo).toString()
                        InstalledAppItem(
                            packageName = appInfo.packageName,
                            appName = name,
                            isSystemApp = isSystem
                        )
                    }.sortedBy { it.appName.lowercase() }
                } catch (_: Exception) {
                    emptyList()
                }
            }
            _uiState.update {
                it.copy(
                    installedApps = apps,
                    isInstalledAppsLoading = false
                )
            }
        }
    }

    fun requestAccessibilityPermission(context: Context) {
        FocusBlockManager.openAccessibilitySettings(context)
    }

    fun requestUsageStatsPermission(context: Context) {
        FocusBlockManager.openUsageAccessSettings(context)
    }

    fun requestNotificationPermission(context: Context) {
        FocusBlockManager.openNotificationSettings(context)
    }

    fun testBlockOverlay(context: Context) {
        val intent = android.content.Intent(context, BlockOverlayActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra(BlockOverlayActivity.EXTRA_BLOCKED_TYPE, com.example.service.BlockedType.APP.name)
            putExtra(BlockOverlayActivity.EXTRA_BLOCKED_IDENTIFIER, "com.google.android.youtube")
            putExtra(BlockOverlayActivity.EXTRA_BLOCKED_NAME, "YouTube (Simulação de Bloqueio)")
        }
        context.startActivity(intent)
    }
}

class BlockerViewModelFactory(
    private val application: Application,
    private val repository: StudyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BlockerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BlockerViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
