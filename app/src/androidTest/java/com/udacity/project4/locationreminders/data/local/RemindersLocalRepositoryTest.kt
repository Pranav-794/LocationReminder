package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var remindersRepository: RemindersLocalRepository

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        remindersRepository = RemindersLocalRepository(
            database.reminderDao(),
            Dispatchers.Main
        )
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun saveReminder_retrieveReminder() = runBlocking {
        val reminder = ReminderDTO(
            "New Reminder",
            "New Description",
            "Home Depot",
            20.0,
            25.0
        )
        remindersRepository.saveReminder(reminder)

        val result = remindersRepository.getReminder(reminder.id)

        // THEN - Same task is returned.
        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat<ReminderDTO>(result.data, notNullValue())
        assertRemindersMatch(result.data, reminder)
    }

    @Test
    fun retrieveReminder_notFoundById() = runBlocking {

        val result = remindersRepository.getReminder("random_id!")

        assertThat(result.succeeded, `is`(false))
        result as Result.Error
        assertThat(result.message, notNullValue())
        assertThat(result.message, `is`("Reminder not found!"))
    }


    @Test
    fun saveReminders_delete_getAlLReminders() = runBlocking {
        val reminder = ReminderDTO(
            "New Reminder",
            "New Description",
            "Home Depot",
            20.0,
            25.0
        )
        val reminder2 = ReminderDTO(
            "Updated Reminder",
            "Updated Description",
            "Updated Home Depot",
            30.0,
            35.0
        )
        remindersRepository.saveReminder(reminder)
        remindersRepository.saveReminder(reminder2)

        val results = remindersRepository.getReminders()
        // THEN - Same task is returned.
        assertThat(results.succeeded, `is`(true))
        results as Result.Success
        assertThat(results.data, notNullValue())
        assertThat(results.data.size, `is`(2))

        remindersRepository.deleteAllReminders()
        val deletedResults = remindersRepository.getReminders()
        deletedResults as Result.Success
        assertThat(deletedResults.data, notNullValue())
        assertThat(deletedResults.data.size, `is`(0))
    }

    private fun assertRemindersMatch(loaded: ReminderDTO, reminder: ReminderDTO) {
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }
}