package com.abdulghni490.carreminder.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    val tasks by viewModel.tasks.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Car Maintenance Tracker") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                viewModel.addTask("My Car", "Oil Change", 50000, System.currentTimeMillis() + 86400000L * 30) 
            }) { Text("+") }
        }
    ) { padding ->
        LazyColumn(contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            items(tasks) { task ->
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(task.taskName, style = MaterialTheme.typography.titleMedium)
                            Text(task.carName, style = MaterialTheme.typography.bodyMedium)
                            Text("Due at: \${task.mileageDue} miles", style = MaterialTheme.typography.bodySmall)
                        }
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { viewModel.toggleTaskCompletion(task) }
                        )
                    }
                }
            }
        }
    }
}
