package com.example.samsungcontroll

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

/** Home-screen widget with power, mute and volume keys. */
class RemoteWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val views = RemoteViews(context.packageName, R.layout.widget_remote).apply {
            // Power may have to wake the TV (tens of seconds), so it gets an activity that
            // keeps the process alive; the short keys use a broadcast and stay invisible.
            setOnClickPendingIntent(R.id.widget_power, activity(context, 0, QuickAction.Power))
            setOnClickPendingIntent(R.id.widget_mute, broadcast(context, 1, QuickAction.Key("KEY_MUTE")))
            setOnClickPendingIntent(R.id.widget_vol_down, broadcast(context, 2, QuickAction.Key("KEY_VOLDOWN")))
            setOnClickPendingIntent(R.id.widget_vol_up, broadcast(context, 3, QuickAction.Key("KEY_VOLUP")))
        }
        appWidgetIds.forEach { manager.updateAppWidget(it, views) }
    }

    private fun activity(context: Context, code: Int, action: QuickAction): PendingIntent =
        PendingIntent.getActivity(
            context, code, QuickActionActivity.intent(context, action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    private fun broadcast(context: Context, code: Int, action: QuickAction): PendingIntent =
        PendingIntent.getBroadcast(
            context, code, QuickActionReceiver.intent(context, action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
}
