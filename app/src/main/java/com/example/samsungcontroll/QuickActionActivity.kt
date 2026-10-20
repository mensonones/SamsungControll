package com.example.samsungcontroll

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Invisible activity behind launcher shortcuts and "wake and open" scenes. It stays alive
 * (translucent, so the process keeps foreground priority) only while the action runs, then
 * reports the outcome in a toast and finishes.
 */
class QuickActionActivity : ComponentActivity() {

    private val quickActions: QuickTvActions by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val action = QuickAction.decode(intent.getStringExtra(EXTRA_ACTION))
        if (action == null) {
            finish()
            return
        }
        if (action is QuickAction.WakeAndApp || action is QuickAction.WakeAndKey || action == QuickAction.Power) {
            Toast.makeText(this, R.string.quick_working, Toast.LENGTH_SHORT).show()
        }
        lifecycleScope.launch {
            val result = quickActions.run(action)
            val message = when (result) {
                QuickResult.DONE, QuickResult.WOKE_TV -> null
                QuickResult.NEEDS_PAIRING -> R.string.quick_needs_pairing
                QuickResult.UNREACHABLE -> R.string.quick_unreachable
                QuickResult.NO_TV -> R.string.quick_no_tv
            }
            if (message != null) Toast.makeText(this@QuickActionActivity, message, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    companion object {
        const val EXTRA_ACTION = "quick_action"

        fun intent(context: Context, action: QuickAction): Intent =
            Intent(context, QuickActionActivity::class.java)
                .setAction("com.example.samsungcontroll.action.QUICK")
                .putExtra(EXTRA_ACTION, action.encode())
    }
}
