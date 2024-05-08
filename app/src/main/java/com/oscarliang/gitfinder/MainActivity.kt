package com.oscarliang.gitfinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalConfiguration
import com.oscarliang.gitfinder.ui.GithubApp
import com.oscarliang.gitfinder.ui.theme.GitFinderTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            GitFinderTheme {
                val configuration = LocalConfiguration.current
                GithubApp(configuration.orientation)
            }
        }
    }

}

