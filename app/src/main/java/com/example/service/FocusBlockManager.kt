package com.example.service

import android.accessibilityservice.AccessibilityService
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import com.example.data.local.database.StudyDatabase
import com.example.domain.model.PomodoroPhase
import com.example.domain.model.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean

data class FocusBlockRuntimeState(
    val isFocusActive: Boolean = false,
    val phase: PomodoroPhase = PomodoroPhase.FOCUS,
    val remainingSeconds: Int = 0,
    val activeSubjectName: String = "",
    val activeTopicName: String = "",
    val isBlockingEnabled: Boolean = true,
    val blockDuringBreaks: Boolean = false,
    val strictMode: Boolean = false,
    val blockedPackages: Set<String> = emptySet(),
    val blockedDomains: Set<String> = emptySet(),
    val lastBlockedEvent: BlockedEventInfo? = null
)

data class BlockedEventInfo(
    val type: BlockedType,
    val identifier: String,
    val displayName: String,
    val timestampMs: Long = System.currentTimeMillis()
)

enum class BlockedType {
    APP,
    WEBSITE
}

object FocusBlockManager {

    private val _runtimeState = MutableStateFlow(FocusBlockRuntimeState())
    val runtimeState: StateFlow<FocusBlockRuntimeState> = _runtimeState.asStateFlow()

    private val managerScope = CoroutineScope(Dispatchers.IO)
    private var isInitialized = false

    // Pacotes do sistema que NUNCA devem ser bloqueados
    private val systemWhitelistedPackages = setOf(
        "com.android.systemui",
        "com.android.launcher",
        "com.google.android.apps.nexuslauncher",
        "com.android.settings",
        "com.android.phone",
        "com.android.dialer",
        "com.google.android.dialer",
        "com.android.server.telecom",
        "com.android.emergency",
        "com.google.android.apps.safetyhub",
        "android"
    )

    // Navegadores populares suportados para extração e bloqueio de sites
    val supportedBrowserPackages = setOf(
        "com.android.chrome",
        "com.google.android.apps.chrome",
        "com.sec.android.app.sbrowser",
        "org.mozilla.firefox",
        "com.microsoft.emmx",
        "com.brave.browser",
        "com.opera.browser",
        "com.opera.mini.native",
        "com.duckduckgo.mobile.android",
        "com.vivaldi.browser"
    )

    // Aliases conhecidos para pacotes (ex: variações de YouTube e Facebook)
    private val packageFamilyAliases = mapOf(
        "com.google.android.youtube" to setOf("com.google.android.youtube", "com.google.android.apps.youtube.music", "com.google.android.youtube.tv"),
        "com.facebook.katana" to setOf("com.facebook.katana", "com.facebook.lite", "com.facebook.orca"),
        "com.twitter.android" to setOf("com.twitter.android", "com.twitter.android.lite")
    )

    // Aliases de domínios conhecidos
    private val domainAliases = mapOf(
        "youtube.com" to setOf("youtube.com", "m.youtube.com", "youtu.be", "youtube"),
        "facebook.com" to setOf("facebook.com", "m.facebook.com", "fb.com", "facebook"),
        "instagram.com" to setOf("instagram.com", "instagr.am", "instagram"),
        "twitter.com" to setOf("twitter.com", "x.com", "t.co", "twitter"),
        "x.com" to setOf("x.com", "twitter.com", "t.co"),
        "tiktok.com" to setOf("tiktok.com", "m.tiktok.com", "tiktok")
    )

    /**
     * Inicializa a sincronização contínua com o banco de dados Room.
     */
    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true

        managerScope.launch {
            try {
                val db = StudyDatabase.getInstance(context)
                launch {
                    db.blockSettingsDao().getSettings().collectLatest { settings ->
                        val current = settings ?: return@collectLatest
                        _runtimeState.update {
                            it.copy(
                                isBlockingEnabled = current.isBlockingEnabled,
                                blockDuringBreaks = current.blockDuringBreaks,
                                strictMode = current.strictMode
                            )
                        }
                    }
                }
                launch {
                    db.blockedAppDao().getActiveBlockedApps().collectLatest { apps ->
                        val pkgSet = apps.map { it.packageName }.toSet()
                        _runtimeState.update { it.copy(blockedPackages = pkgSet) }
                    }
                }
                launch {
                    db.blockedWebsiteDao().getActiveBlockedWebsites().collectLatest { websites ->
                        val domainSet = websites.map { it.domain }.toSet()
                        _runtimeState.update { it.copy(blockedDomains = domainSet) }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun refreshFromDatabase(context: Context) {
        managerScope.launch {
            try {
                val db = StudyDatabase.getInstance(context)
                val settings = db.blockSettingsDao().getSettingsSync()
                val apps = db.blockedAppDao().getActiveBlockedAppsList()
                val websites = db.blockedWebsiteDao().getActiveBlockedWebsitesList()

                _runtimeState.update {
                    it.copy(
                        isBlockingEnabled = settings?.isBlockingEnabled ?: true,
                        blockDuringBreaks = settings?.blockDuringBreaks ?: false,
                        strictMode = settings?.strictMode ?: false,
                        blockedPackages = apps.map { a -> a.packageName }.toSet(),
                        blockedDomains = websites.map { w -> w.domain }.toSet()
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun updatePomodoroState(
        state: TimerState,
        phase: PomodoroPhase,
        remainingSeconds: Int,
        subjectName: String,
        topicName: String
    ) {
        val currentSettings = _runtimeState.value
        val shouldBlockForPhase = when (phase) {
            PomodoroPhase.FOCUS -> state == TimerState.RUNNING
            PomodoroPhase.SHORT_BREAK, PomodoroPhase.LONG_BREAK ->
                state == TimerState.RUNNING && currentSettings.blockDuringBreaks
        }

        _runtimeState.update {
            it.copy(
                isFocusActive = shouldBlockForPhase,
                phase = phase,
                remainingSeconds = remainingSeconds,
                activeSubjectName = subjectName,
                activeTopicName = topicName
            )
        }
    }

    fun updateBlockerData(
        enabled: Boolean,
        blockDuringBreaks: Boolean,
        strictMode: Boolean,
        blockedPackages: Set<String>,
        blockedDomains: Set<String>
    ) {
        _runtimeState.update {
            it.copy(
                isBlockingEnabled = enabled,
                blockDuringBreaks = blockDuringBreaks,
                strictMode = strictMode,
                blockedPackages = blockedPackages,
                blockedDomains = blockedDomains
            )
        }
    }

    /**
     * Verifica se o pacote em primeiro plano deve ser bloqueado.
     */
    fun shouldBlockPackage(packageName: String?, myPackageName: String): Boolean {
        if (packageName.isNullOrBlank()) return false
        val state = _runtimeState.value
        if (!state.isBlockingEnabled || !state.isFocusActive) return false

        // Nunca bloquear o próprio app
        if (packageName == myPackageName) return false

        // Nunca bloquear pacotes do sistema essenciais
        if (systemWhitelistedPackages.contains(packageName)) return false

        // 1. Verificação direta do pacote
        if (state.blockedPackages.contains(packageName)) return true

        // 2. Verificação de pacotes da mesma família (ex: YouTube Music, Facebook Lite)
        for (blockedPkg in state.blockedPackages) {
            val family = packageFamilyAliases[blockedPkg]
            if (family != null && family.contains(packageName)) {
                return true
            }
        }

        return false
    }

    /**
     * Verifica se uma URL, host ou texto de página corresponde a um site bloqueado.
     */
    fun shouldBlockUrl(rawUrlOrHost: String?): String? {
        if (rawUrlOrHost.isNullOrBlank()) return null
        val state = _runtimeState.value
        if (!state.isBlockingEnabled || !state.isFocusActive) return null

        val rawClean = rawUrlOrHost.lowercase().trim()
        val host = extractDomain(rawClean) ?: rawClean

        for (domain in state.blockedDomains) {
            val target = domain.lowercase().trim()
            val targetNoWww = target.removePrefix("www.")

            // 1. Match direto no host
            if (host.equals(targetNoWww, ignoreCase = true) || host.endsWith(".$targetNoWww")) {
                return domain
            }

            // 2. Se a URL ou barra de endereço contém a string do domínio
            if (rawClean.contains(targetNoWww)) {
                return domain
            }

            // 3. Aliases conhecidos (ex: m.youtube.com, youtu.be, fb.com)
            val aliases = domainAliases[targetNoWww]
            if (aliases != null) {
                for (alias in aliases) {
                    if (host.equals(alias, ignoreCase = true) || host.endsWith(".$alias") || rawClean.contains(alias)) {
                        return domain
                    }
                }
            }
        }

        return null
    }

    fun extractDomain(raw: String): String? {
        val trimmed = raw.trim().lowercase()
        return try {
            val withScheme = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                "https://$trimmed"
            } else {
                trimmed
            }
            val uri = URI(withScheme)
            val host = uri.host ?: trimmed.split("/").firstOrNull() ?: trimmed
            host.removePrefix("www.")
        } catch (_: Exception) {
            trimmed.substringBefore("/").substringBefore("?").removePrefix("www.")
        }
    }

    fun setLastBlockedEvent(event: BlockedEventInfo) {
        _runtimeState.update { it.copy(lastBlockedEvent = event) }
    }

    // ==========================================
    // Checagens de Permissões e Diagnóstico
    // ==========================================

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun isAccessibilityServiceEnabled(
        context: Context,
        serviceClass: Class<out AccessibilityService>
    ): Boolean {
        val expectedComponentName = "${context.packageName}/${serviceClass.name}"
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServicesSetting.split(':').any {
            it.equals(expectedComponentName, ignoreCase = true)
        }
    }

    fun canPostNotifications(context: Context): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openUsageAccessSettings(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun openNotificationSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
        context.startActivity(intent)
    }
}
