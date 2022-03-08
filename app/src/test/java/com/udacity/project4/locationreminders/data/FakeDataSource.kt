package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    //    TODO: Create a fake data source to act as a double to the real data source
    private var remindersData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()
    private var shouldReturnError = false

    companion object {
        const val mockErrorMessage = "Could not find Reminder item"
    }

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) return Result.Error(mockErrorMessage)
        return Result.Success(remindersData.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersData[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = remindersData[id] ?: return Result.Error(mockErrorMessage)
        return Result.Success(reminder)
    }

    override suspend fun deleteAllReminders() {
        remindersData.clear()
    }
}