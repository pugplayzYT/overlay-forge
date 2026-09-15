package com.pugplayz.overlayforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pugplayz.overlayforge.ui.theme.OverlayForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OverlayForgeTheme {
                OverlayForgeApp()
            }
        }
    }
}
