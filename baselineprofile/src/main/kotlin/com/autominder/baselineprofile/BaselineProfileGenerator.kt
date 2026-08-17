package com.autominder.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

/**
 * Generates the Baseline Profile that ships inside :app.
 *
 * A Baseline Profile is a list of the classes and methods on the critical
 * startup path. AOT-compiling them removes the interpreter/JIT warm-up on a
 * cold start, which is worth roughly 20–30% on mid-range hardware — the
 * devices most AutoMinder users actually own.
 *
 * The journey below deliberately mirrors what a real user does in their first
 * seconds: cold launch, wait for the maintenance verdict to be rendered, then
 * scroll the dashboard. Anything the profile does not visit stays un-optimised,
 * so this must stay honest to real usage rather than clicking through every
 * screen for the sake of coverage.
 *
 * Run with:
 *   gradlew :baselineprofile:generateBaselineProfile
 * on a rooted emulator or a userdebug build — a locked production device cannot
 * capture a profile. Output lands in app/src/main/generated/baselineProfiles/.
 */
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = PACKAGE_NAME,
        // Several iterations, each from a genuinely cold process, so the
        // captured profile reflects steady-state startup rather than one
        // lucky run.
        maxIterations = 12,
        stableIterations = 3,
        includeInStartupProfile = true
    ) {
        pressHome()
        startActivityAndWait()

        // Wait for real content, not just for the window to appear. Waiting on
        // the window alone would profile the splash screen and stop before the
        // Room query and first composition — exactly the work we most want
        // compiled ahead of time.
        device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), CONTENT_TIMEOUT_MS)

        // Scroll the dashboard so the lazy list's item composables, the
        // status/verdict components and the vehicle rows are all reached.
        device.findObject(By.scrollable(true))?.let { list ->
            list.setGestureMargin(device.displayWidth / 5)
            list.fling(Direction.DOWN)
            device.waitForIdle()
            list.fling(Direction.UP)
            device.waitForIdle()
        }
    }

    private companion object {
        const val PACKAGE_NAME = "com.autominder.app"
        const val CONTENT_TIMEOUT_MS = 10_000L
    }
}
