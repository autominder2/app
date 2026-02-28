package com.abdulghni490.carreminder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abdulghni490.carreminder.data.MaintenanceTask
import com.abdulghni490.carreminder.data.TaskDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val taskDao: TaskDao
) : ViewModel() {

    val tasks = taskDao.getAllTasks().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun addTask(carName: String, taskName: String, mileageDue: Int, dateDue: Long) {
        viewModelScope.launch {
            taskDao.insertTask(MaintenanceTask(carName = carName, taskName = taskName, mileageDue = mileageDue, dateDue = dateDue))
        }
    }

    fun toggleTaskCompletion(task: MaintenanceTask) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: MaintenanceTask) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }
}
