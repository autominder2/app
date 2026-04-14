package com.autominder.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.autominder.app.MainActivity
import com.autominder.app.R
import com.autominder.app.data.local.dao.ReminderDao
import com.autominder.app.ui.util.DateFormatUtil
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint for Glance widgets — widgets cannot use @Inject directly.
 * Exposes the Hilt-managed singleton ReminderDao so the widget shares the
 * same Room database instance as the rest of the app.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun reminderDao(): ReminderDao
}

class AutoMinderWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Use EntryPointAccessors to retrieve the Hilt-managed singleton DAO
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )
        val dao = entryPoint.reminderDao()

        val urgentReminder = try {
            dao.getMostUrgentReminder()
        } catch (e: Exception) {
            null
        }

        val serviceLabel = urgentReminder?.let { r ->
            r.customLabel ?: r.serviceType.label
        }
        val dueDateLabel = urgentReminder?.nextDueDate?.let { DateFormatUtil.formatDate(it) }

        provideContent {
            GlanceTheme {
                WidgetContent(
                    serviceLabel = serviceLabel,
                    dueDateLabel = dueDateLabel,
                    context = context
                )
            }
        }
    }
}

@Composable
private fun WidgetContent(
    serviceLabel: String?,
    dueDateLabel: String?,
    context: Context
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .clickable(actionStartActivity<MainActivity>())
            .padding(16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                text = context.getString(R.string.widget_next_service),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
            Spacer(GlanceModifier.padding(top = 4.dp))
            Text(
                text = serviceLabel ?: context.getString(R.string.widget_all_caught_up),
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            if (dueDateLabel != null) {
                Spacer(GlanceModifier.padding(top = 2.dp))
                Text(
                    text = context.getString(R.string.widget_due, dueDateLabel),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

class AutoMinderWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AutoMinderWidget()
}
