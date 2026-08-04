package com.autominder.app

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/** Keeps isolated database tests independent from production app startup services. */
class AutoMinderTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader,
        className: String,
        context: Context
    ): Application = super.newApplication(cl, Application::class.java.name, context)
}
