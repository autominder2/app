package com.autominder.app.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.autominder.app.R
import com.autominder.app.ui.theme.Motion
import kotlinx.coroutines.launch
import kotlin.math.abs

private data class OnboardingPage(
    val icon: ImageVector,
    val titleRes: Int,
    val subtitleRes: Int
)

private val pages = listOf(
    OnboardingPage(Icons.Default.DirectionsCar, R.string.onboarding_welcome_title, R.string.onboarding_welcome_subtitle),
    OnboardingPage(Icons.Default.Build, R.string.onboarding_track_title, R.string.onboarding_track_subtitle),
    OnboardingPage(Icons.Default.NotificationsActive, R.string.onboarding_notify_title, R.string.onboarding_notify_subtitle)
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val haptic = LocalHapticFeedback.current

    val notificationLauncher = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
        ) { _ ->
            viewModel.completeOnboarding()
            onFinished()
        }
    } else null

    fun finishOnboarding() {
        haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU && notificationLauncher != null) {
            val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        viewModel.completeOnboarding()
        onFinished()
    }

    // Soft tick each time a page settles.
    LaunchedEffect(pagerState.currentPage) {
        haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
    }

    // Real-time gradient that follows the swipe rather than snapping on settle.
    val accents = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.tertiaryContainer
    )
    val current = pagerState.currentPage
    val offset = pagerState.currentPageOffsetFraction
    val topColor = run {
        val from = accents[current]
        val toIndex = if (offset > 0) (current + 1).coerceAtMost(accents.lastIndex)
        else (current - 1).coerceAtLeast(0)
        lerp(from, accents[toIndex], abs(offset))
    }

    val isLastPage = pagerState.currentPage == pages.size - 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(topColor, MaterialTheme.colorScheme.background)
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top row: segmented progress + Skip ──────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 12.dp, top = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(pages.size) { index ->
                        val active = index <= pagerState.currentPage
                        val barWidth by animateDpAsState(
                            targetValue = if (index == pagerState.currentPage) 28.dp else 8.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
                            label = "progress$index"
                        )
                        val barColor by animateColorAsState(
                            targetValue = if (active) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant,
                            label = "progressColor$index"
                        )
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(barWidth)
                                .clip(CircleShape)
                                .background(barColor)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = !isLastPage,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    TextButton(onClick = {
                        scope.launch { pagerState.animateScrollToPage(pages.size - 1) }
                    }) {
                        Text(stringResource(R.string.onboarding_skip))
                    }
                }
            }

            // ── Pager with parallax content ─────────────────────────────────
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                OnboardingPageContent(pages[page], pageOffset)
            }

            // ── Full-width morphing primary action ──────────────────────────
            Button(
                onClick = {
                    if (isLastPage) {
                        finishOnboarding()
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding
            ) {
                AnimatedContent(
                    targetState = isLastPage,
                    transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.9f)).togetherWith(fadeOut()) },
                    label = "cta"
                ) { last ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (last) stringResource(R.string.onboarding_get_started)
                            else stringResource(R.string.onboarding_next),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = stringResource(R.string.cd_onboarding_next),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage, pageOffset: Float) {
    val clampedDistance = abs(pageOffset).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Layered-glow hero that scales up and drifts as its page centers.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.graphicsLayer {
                alpha = 1f - clampedDistance
                // Parallax drift + scale-in are the nausea-prone parts, so they
                // scale by Motion.amplitude (0 when "Remove animations" is on).
                val scale = 1f - (0.18f * clampedDistance * Motion.amplitude)
                scaleX = scale
                scaleY = scale
                translationX = pageOffset * size.width * 0.25f * Motion.amplitude
            }
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.06f))
            )
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
            )
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Text lags the hero slightly for a layered, parallax feel.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                alpha = 1f - clampedDistance
                translationX = pageOffset * size.width * 0.12f * Motion.amplitude
            }
        ) {
            Text(
                text = stringResource(page.titleRes),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(page.subtitleRes),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
