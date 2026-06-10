package com.autominder.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.autominder.app.R

enum class SaveButtonState { Idle, Saving, Success }

/**
 * Save button that morphs Idle -> Saving (spinner) -> Success (check),
 * so the user always knows where their data is.
 */
@Composable
fun SaveButton(
    state: SaveButtonState,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled && state == SaveButtonState.Idle
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.8f)).togetherWith(fadeOut()) },
            label = "save_button"
        ) { target ->
            when (target) {
                SaveButtonState.Idle -> Text(text)
                SaveButtonState.Saving -> CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                SaveButtonState.Success -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.form_saved))
                }
            }
        }
    }
}
