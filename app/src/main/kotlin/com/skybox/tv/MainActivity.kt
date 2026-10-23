package com.skybox.tv

import android.os.Bundle
import androidx.activity.compose.setContent
import com.skybox.tv.SkyBoxApp

/**
 * MainActivity for SkyBox TV.
 * Sets up the Compose content using the SkyBoxApp composable.
 * Authentication state and navigation are managed within SkyBoxApp.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkyBoxApp()
        }
    }
}