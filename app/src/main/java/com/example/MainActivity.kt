package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.data.SkitmDatabase
import com.example.data.SkitmRepository
import com.example.ui.screens.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val database = Room.databaseBuilder(
        applicationContext,
        SkitmDatabase::class.java,
        "skitm_database"
    )
    .fallbackToDestructiveMigration()
    .build()
    
    val repository = SkitmRepository(database.skitmDao())

    setContent {
      val factory = object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
              return MainViewModel(repository) as T
          }
      }
      val mainViewModel: MainViewModel = viewModel(factory = factory)

      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          AppNavigation(viewModel = mainViewModel, modifier = Modifier.padding(innerPadding))
        }
      }
    }
  }
}
