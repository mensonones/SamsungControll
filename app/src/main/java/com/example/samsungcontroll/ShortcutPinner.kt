package com.example.samsungcontroll

import android.content.Context
import android.widget.Toast
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

/** Asks the launcher to pin a one-tap shortcut that runs a [QuickAction] (e.g. wake the TV and open Netflix). */
object ShortcutPinner {
    fun pin(context: Context, label: String, action: QuickAction) {
        if (!ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            Toast.makeText(context, R.string.shortcut_pin_unsupported, Toast.LENGTH_LONG).show()
            return
        }
        val shortcut = ShortcutInfoCompat.Builder(context, "quick_${action.encode()}")
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(QuickActionActivity.intent(context, action))
            .build()
        ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    }
}
