package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.AppDatabase
import com.example.data.SpeedRepository
import com.example.ui.screens.SettingsNavHost
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.SpeedViewModel
import com.example.viewmodel.SpeedViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = SpeedRepository(database.speedDao())
        val factory = SpeedViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[SpeedViewModel::class.java]

        setContent {
            MyApplicationTheme {
                SettingsNavHost(viewModel = viewModel)
            }
        }
    }
}
