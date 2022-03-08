package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        val reminder = ReminderDTO(
            "New Reminder",
            "New Description",
            "Home Depot",
            20.0,
            25.0
        )
        database.reminderDao().saveReminder(reminder)

        val loaded = database.reminderDao().getReminderById(reminder.id)

        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertRemindersMatch(loaded, reminder)
    }

    @Test
    fun reminderNotFoundById() = runBlockingTest {
        val loaded = database.reminderDao().getReminderById("random_id")
        assertThat(loaded, nullValue())
    }

    @Test
    fun getAllReminders() = runBlockingTest {
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
        database.reminderDao().saveReminder(reminder)
        database.reminderDao().saveReminder(reminder2)

        val loaded = database.reminderDao().getReminders()
        assertThat<List<ReminderDTO>>(loaded, notNullValue())
        assertThat(loaded.size, `is`(2))
        assertRemindersMatch(loaded[0], reminder)
        assertRemindersMatch(loaded[1], reminder2)
    }

    private fun assertRemindersMatch(loaded: ReminderDTO, reminder: ReminderDTO) {
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteReminders() = runBlockingTest {
        val reminder = ReminderDTO(
            "New Reminder",
            "New Description",
            "Home Depot",
            20.0,
            25.0
        )
        database.reminderDao().saveReminder(reminder)
        database.reminderDao().deleteAllReminders()

        val loaded = database.reminderDao().getReminders()
        assertThat<List<ReminderDTO>>(loaded, notNullValue())
        assertThat(loaded.size, `is`(0))
    }
}