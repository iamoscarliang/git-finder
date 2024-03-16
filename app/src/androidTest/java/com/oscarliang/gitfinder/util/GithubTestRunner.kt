package com.oscarliang.gitfinder.util

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.oscarliang.gitfinder.TestApp

class GithubTestRunner : AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application {
        return super.newApplication(cl, TestApp::class.java.name, context)
    }

}