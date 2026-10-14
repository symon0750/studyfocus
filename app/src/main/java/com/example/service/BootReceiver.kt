package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.database.StudyDatabase
import com.example.domain.model.PomodoroPhase
import com.example.domain.model.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            // Garante que o estado do timer e bloqueio seja inicializado com segurança
            FocusBlockManager.updatePomodoroState(
                state = TimerState.IDLE,
                phase = PomodoroPhase.FOCUS,
                remainingSeconds = 0,
                subjectName = "",
                topicName = ""
            )

            // Carrega as configurações do Room para manter as listas de bloqueio atualizadas
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = StudyDatabase.getInstance(context)
                    val settings = db.blockSettingsDao().getSettingsSync()
                    val blockedApps = db.blockedAppDao().getActiveBlockedAppsList()
                    val blockedWebsites = db.blockedWebsiteDao().getActiveBlockedWebsitesList()

                    FocusBlockManager.updateBlockerData(
                        enabled = settings?.isBlockingEnabled ?: true,
                        blockDuringBreaks = settings?.blockDuringBreaks ?: false,
                        strictMode = settings?.strictMode ?: false,
                        blockedPackages = blockedApps.map { it.packageName }.toSet(),
                        blockedDomains = blockedWebsites.map { it.domain }.toSet()
                    )
                } catch (_: Exception) {}
            }
        }
    }
}
