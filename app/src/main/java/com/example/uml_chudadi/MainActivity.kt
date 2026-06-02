package com.example.uml_chudadi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.uml_chudadi.ui.theme.Uml_chudadiTheme
import com.example.uml_chudadi.view.ChudadiApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Uml_chudadiTheme {
                ChudadiApp()
            }
        }
    }
}
