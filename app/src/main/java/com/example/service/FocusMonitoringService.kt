package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.ui.blocker.BlockOverlayActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FocusMonitoringService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default)
    private var monitorJob: Job? = null
    private var lastCheckedPackage: String? = null
    private var lastBlockTimestamp: Long = 0

    companion object {
        const val CHANNEL_ID = "study_focus_protection_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_MONITORING = "ACTION_START_MONITORING"
        const val ACTION_STOP_MONITORING = "ACTION_STOP_MONITORING"

        fun start(context: Context) {
            val intent = Intent(context, FocusMonitoringService::class.java).apply {
                action = ACTION_START_MONITORING
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FocusMonitoringService::class.java).apply {
                action = ACTION_STOP_MONITORING
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_MONITORING -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                val notification = buildForegroundNotification()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }
                startUsageStatsFallbackMonitoring()
            }
        }
        return START_STICKY
    }

    private fun startUsageStatsFallbackMonitoring() {
        monitorJob?.cancel()
        monitorJob = serviceScope.launch {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            while (isActive) {
                delay(800L)

                val state = FocusBlockManager.runtimeState.value
                if (!state.isFocusActive || !state.isBlockingEnabled) {
                    continue
                }

                if (usageStatsManager != null && FocusBlockManager.hasUsageStatsPermission(applicationContext)) {
                    val foregroundPackage = getForegroundPackage(usageStatsManager)
                    if (foregroundPackage != null && foregroundPackage != packageName) {
                        if (FocusBlockManager.shouldBlockPackage(foregroundPackage, packageName)) {
                            val now = System.currentTimeMillis()
                            if (foregroundPackage != lastCheckedPackage || (now - lastBlockTimestamp) > 1500) {
                                lastCheckedPackage = foregroundPackage
                                lastBlockTimestamp = now

                                val appName = try {
                                    val pm = packageManager
                                    val appInfo = pm.getApplicationInfo(foregroundPackage, 0)
                                    pm.getApplicationLabel(appInfo).toString()
                                } catch (_: Exception) {
                                    foregroundPackage
                                }

                                FocusBlockManager.setLastBlockedEvent(
                                    BlockedEventInfo(
                                        type = BlockedType.APP,
                                        identifier = foregroundPackage,
                                        displayName = appName
                                    )
                                )

                                val blockIntent = Intent(applicationContext, BlockOverlayActivity::class.java).apply {
                                    this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                                    putExtra(BlockOverlayActivity.EXTRA_BLOCKED_TYPE, BlockedType.APP.name)
                                    putExtra(BlockOverlayActivity.EXTRA_BLOCKED_IDENTIFIER, foregroundPackage)
                                    putExtra(BlockOverlayActivity.EXTRA_BLOCKED_NAME, appName)
                                }
                                startActivity(blockIntent)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getForegroundPackage(usageStatsManager: UsageStatsManager): String? {
        val now = System.currentTimeMillis()
        try {
            val events = usageStatsManager.queryEvents(now - 12000, now)
            val event = UsageEvents.Event()
            var lastForegroundApp: String? = null
            var lastTimestamp: Long = 0

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if ((event.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                     event.eventType == 1 /* ACTIVITY_RESUMED */) &&
                    event.timeStamp > lastTimestamp
                ) {
                    lastForegroundApp = event.packageName
                    lastTimestamp = event.timeStamp
                }
            }
            if (!lastForegroundApp.isNullOrBlank()) return lastForegroundApp

            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 60000, now)
            return stats.maxByOrNull { it.lastTimeUsed }?.packageName
        } catch (_: Exception) {
            return null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Proteção de Foco do StudyFocus",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notificação ativa durante sessões de foco protegidas contra distrações"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val state = FocusBlockManager.runtimeState.value
        val subjectInfo = if (state.activeSubjectName.isNotBlank()) " em ${state.activeSubjectName}" else ""

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Modo Foco Ativo")
            .setContentText("Sessão Pomodoro em andamento$subjectInfo. Distrações bloqueadas.")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        super.onDestroy()
    }
}
