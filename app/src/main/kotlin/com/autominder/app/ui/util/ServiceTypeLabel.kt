package com.autominder.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.autominder.app.core.util.labelRes
import com.autominder.app.domain.model.ServiceType

/**
 * UI-layer localization for [ServiceType]. The enum's own `label` is
 * English-only legacy/storage text — never render it directly in UI.
 *
 * The enum→resource mapping itself lives in `core/util/ServiceTypeLabels.kt`
 * so that non-composable callers (notifications, the widget) share exactly the
 * same table rather than keeping a second, drifting copy.
 */
@Composable
fun ServiceType.localizedLabel(): String = stringResource(labelRes())
