package com.example.samsungcontroll

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Runs short key actions (volume, mute) from the home-screen widget without any UI. */
class QuickActionReceiver : BroadcastReceiver(), KoinComponent {

    private val quickActions: QuickTvActions by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val action = QuickAction.decode(intent.getStringExtra(QuickActionActivity.EXTRA_ACTION)) ?: return
        val pending = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                quickActions.run(action)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION = "com.example.samsungcontroll.action.QUICK_BROADCAST"

        fun intent(context: Context, action: QuickAction): Intent =
            Intent(context, QuickActionReceiver::class.java)
                .setAction(ACTION)
                .putExtra(QuickActionActivity.EXTRA_ACTION, action.encode())
    }
}
