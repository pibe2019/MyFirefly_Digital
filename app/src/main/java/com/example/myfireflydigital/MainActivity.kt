package com.example.myfireflydigital

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation3.runtime.rememberNavBackStack
import com.example.myfireflydigital.ui.core.navigation.MainModalDrawerScreen
import com.example.myfireflydigital.ui.core.navigation.RoutesScreens
import com.example.myfireflydigital.ui.theme.MyFireflyDigitalTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyFireflyDigitalTheme {
                val backStack = rememberNavBackStack(RoutesScreens.MainModalDrawerScreen)
                MainModalDrawerScreen(backStack)
            }
        }
    }
}

