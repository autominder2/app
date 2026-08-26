package com.autominder.app.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.autominder.app.MainActivity
import com.autominder.app.R

object NotificationHelper {
    const val EXTRA_VEHICLE_ID = "vehicleId"
    const val EXTRA_OPEN_MILEAGE_SHEET = "openMileageSheet"
    const val EXTRA_MILEAGE_REQUEST_ID = "mileageRequestId"

    private const val CHANNEL_ID = "autominder_reminders"
    private const val CHANNEL_NAME = "Maintenance Reminders"

    // Notification action contract (handled by NotificationActionReceiver)
    const val ACTION_MARK_DONE = "com.autominder.app.action.NOTIF_MARK_DONE"
    const val ACTION_SNOOZE = "com.autominder.app.action.NOTIF_SNOOZE"
    const val EXTRA_REMINDER_ID = "extra_reminder_id"

    // Weekly digest gets its own quieter channel: it's a planning summary, not
    // an alert, so users can tune each independently (Tesla's two-tier model).
    private const val DIGEST_CHANNEL_ID = "autominder_digest"
    private const val DIGEST_CHANNEL_NAME = "Weekly Summary"
    private const val DIGEST_NOTIFICATION_ID = -1000

    /**
     * Dismisses every notification this app has posted.
     *
     * Called when the user erases their data. Without it, a reminder for a
     * vehicle that no longer exists stays in the shade, and tapping it opens a
     * detail screen for a deleted row.
     */
    fun cancelAll(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancelAll()
    }

    fun createChannel(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for upcoming and overdue vehicle maintenance"
            vibrationPattern = longArrayOf(0L, 300L, 200L, 300L)
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)

        val digestChannel = NotificationChannel(
            DIGEST_CHANNEL_ID,
            DIGEST_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "One weekly roll-up of everything due on your vehicles"
        }
        manager.createNotificationChannel(digestChannel)
    }

    /**
     * One grouped weekly notification listing every item that needs attention.
     * Fixes the classic complaint "the app only told me about one of the three
     * services due" — all lines ship in a single InboxStyle card.
     */
    fun showWeeklyDigest(
        context: Context,
        title: String,
        summary: String,
        lines: List<String>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            DIGEST_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val style = NotificationCompat.InboxStyle().setSummaryText(summary)
        lines.forEach { style.addLine(it) }

        val notification = NotificationCompat.Builder(context, DIGEST_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(summary)
            .setStyle(style)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(DIGEST_NOTIFICATION_ID, notification)
    }

    fun showReminderNotification(
        context: Context,
        reminderId: Long,
        vehicleId: Long,
        title: String,
        body: String
    ) {
        // Android 13+ requires POST_NOTIFICATIONS runtime permission — verify before posting
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("vehicleId", vehicleId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Inline actions: acting on a reminder must never require opening
        // the app. Request codes are namespaced per reminder to avoid
        // PendingIntent collisions (id*10 + action ordinal).
        val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        val donePi = PendingIntent.getBroadcast(
            context,
            (reminderId * 10 + 1).toInt(),
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        val snoozePi = PendingIntent.getBroadcast(
            context,
            (reminderId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        // "Update mileage" rides the existing vehicleId deep link — lands on
        // the vehicle's detail screen where the odometer instrument lives.
        val mileageIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_VEHICLE_ID, vehicleId)
            putExtra(EXTRA_OPEN_MILEAGE_SHEET, true)
            putExtra(EXTRA_MILEAGE_REQUEST_ID, System.currentTimeMillis())
        }
        val mileagePi = PendingIntent.getActivity(
            context,
            (reminderId * 10 + 3).toInt(),
            mileageIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(0, context.getString(R.string.action_done), donePi)
            .addAction(0, context.getString(R.string.notif_action_snooze_3d), snoozePi)
            .addAction(0, context.getString(R.string.notif_action_update_mileage), mileagePi)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(reminderId.toInt(), notification)
    }
}
