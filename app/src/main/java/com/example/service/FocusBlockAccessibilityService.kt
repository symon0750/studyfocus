package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.ui.blocker.BlockOverlayActivity

class FocusBlockAccessibilityService : AccessibilityService() {

    private var lastUrlCheckTimestamp: Long = 0
    private var lastBlockedPackage: String? = null
    private var lastBlockTimestamp: Long = 0

    // IDs de barras de endereço conhecidas em navegadores Android
    private val browserUrlNodeIds = listOf(
        "com.android.chrome:id/url_bar",
        "com.android.chrome:id/search_box_text",
        "org.chromium.chrome:id/url_bar",
        "com.sec.android.app.sbrowser:id/location_bar_edit_text",
        "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
        "org.mozilla.firefox:id/url_bar_title",
        "com.microsoft.emmx:id/url_bar",
        "com.brave.browser:id/url_bar",
        "com.opera.browser:id/url_field",
        "com.duckduckgo.mobile.android:id/omnibarTextInput"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Inicializa o FocusBlockManager com o contexto para garantir cache populado
        FocusBlockManager.init(applicationContext)

        // IMPORTANTE: Preserva as configurações do XML (canRetrieveWindowContent)
        val config = serviceInfo ?: AccessibilityServiceInfo()
        config.flags = config.flags or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        config.notificationTimeout = 100
        serviceInfo = config
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return
        val currentPackage = packageName.trim()

        // Ignorar o próprio app do StudyFocus para não gerar loop
        if (currentPackage == this.packageName) return

        // 1. Verificação de Aplicativo Bloqueado (ex: YouTube, Facebook)
        if (FocusBlockManager.shouldBlockPackage(currentPackage, this.packageName)) {
            val now = System.currentTimeMillis()
            if (currentPackage != lastBlockedPackage || (now - lastBlockTimestamp) > 1000) {
                lastBlockedPackage = currentPackage
                lastBlockTimestamp = now

                val appName = try {
                    val pm = packageManager
                    val appInfo = pm.getApplicationInfo(currentPackage, 0)
                    pm.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    currentPackage
                }

                FocusBlockManager.setLastBlockedEvent(
                    BlockedEventInfo(
                        type = BlockedType.APP,
                        identifier = currentPackage,
                        displayName = appName
                    )
                )

                // 1. Envia imediatamente para a Home para fechar o aplicativo proibido
                performGlobalAction(GLOBAL_ACTION_HOME)

                // 2. Lança a tela de bloqueio e foco do StudyFocus
                launchBlockOverlay(
                    type = BlockedType.APP,
                    identifier = currentPackage,
                    displayName = appName
                )
            }
            return
        }

        // 2. Verificação de Site Bloqueado se estiver em um navegador suportado
        if (FocusBlockManager.supportedBrowserPackages.contains(currentPackage)) {
            val now = System.currentTimeMillis()
            if (now - lastUrlCheckTimestamp < 300) return
            lastUrlCheckTimestamp = now

            val detectedUrl = extractUrlFromBrowser(event)

            if (!detectedUrl.isNullOrBlank()) {
                val blockedDomain = FocusBlockManager.shouldBlockUrl(detectedUrl)
                if (blockedDomain != null) {
                    if ((now - lastBlockTimestamp) > 1000) {
                        lastBlockTimestamp = now
                        FocusBlockManager.setLastBlockedEvent(
                            BlockedEventInfo(
                                type = BlockedType.WEBSITE,
                                identifier = blockedDomain,
                                displayName = blockedDomain
                            )
                        )

                        // Retorna para a home fechando o site e abre o overlay
                        performGlobalAction(GLOBAL_ACTION_HOME)

                        launchBlockOverlay(
                            type = BlockedType.WEBSITE,
                            identifier = blockedDomain,
                            displayName = blockedDomain
                        )
                    }
                }
            }
        }
    }

    private fun extractUrlFromBrowser(event: AccessibilityEvent): String? {
        val rootNode = rootInActiveWindow
        val sourceNode = event.source

        // 1. Tenta buscar por viewIds conhecidos diretamente na janela raiz
        if (rootNode != null) {
            for (viewId in browserUrlNodeIds) {
                try {
                    val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
                    for (node in nodes) {
                        val text = node.text?.toString()
                        if (!text.isNullOrBlank()) {
                            return text
                        }
                    }
                } catch (_: Exception) {}
            }
        }

        // 2. Inspeciona o sourceNode direto do evento
        if (sourceNode != null) {
            val sourceText = sourceNode.text?.toString()
            if (!sourceText.isNullOrBlank() && (sourceText.contains(".") || sourceText.contains("/"))) {
                return sourceText
            }
        }

        // 3. Varredura recursiva na árvore procurando campos de endereço ou URLs
        if (rootNode != null) {
            val found = scanTreeForUrl(rootNode, 0)
            if (found != null) return found
        }

        // 4. Inspeciona textos do evento (títulos de aba / descrições)
        for (item in event.text) {
            val str = item?.toString() ?: continue
            if (str.isNotBlank() && (str.contains(".com") || str.contains(".org") || str.contains(".net") || str.contains(".tv") || str.contains("youtube") || str.contains("facebook"))) {
                return str
            }
        }

        return null
    }

    private fun scanTreeForUrl(node: AccessibilityNodeInfo, depth: Int): String? {
        if (depth > 8) return null

        val viewId = node.viewIdResourceName?.lowercase() ?: ""
        val text = node.text?.toString() ?: ""

        val isAddressLike = viewId.contains("url") ||
                viewId.contains("address") ||
                viewId.contains("search") ||
                viewId.contains("location") ||
                viewId.contains("omnibox")

        if (isAddressLike && text.isNotBlank()) {
            return text
        }

        if (text.isNotBlank() && (text.contains(".com") || text.contains(".org") || text.contains(".net") || text.contains(".tv") || text.contains("http"))) {
            return text
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = scanTreeForUrl(child, depth + 1)
            child.recycle()
            if (found != null) return found
        }

        return null
    }

    private fun launchBlockOverlay(
        type: BlockedType,
        identifier: String,
        displayName: String
    ) {
        val intent = Intent(this, BlockOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BlockOverlayActivity.EXTRA_BLOCKED_TYPE, type.name)
            putExtra(BlockOverlayActivity.EXTRA_BLOCKED_IDENTIFIER, identifier)
            putExtra(BlockOverlayActivity.EXTRA_BLOCKED_NAME, displayName)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        // Callback obrigatório
    }
}
