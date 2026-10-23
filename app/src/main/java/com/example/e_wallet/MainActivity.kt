package com.example.e_wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.e_wallet.ui.navigation.AppNavGraph
import com.example.e_wallet.ui.theme.EWalletTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EWalletTheme {
                AppNavGraph()
            }
        }
    }
}