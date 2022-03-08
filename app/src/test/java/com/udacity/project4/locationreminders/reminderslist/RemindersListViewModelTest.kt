package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    // TODO: provide testing to the RemindersListViewModel and its live data objects
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersListViewModel: RemindersListViewModel

    private lateinit var fakeRemindersDataSource: FakeDataSource

    @Before
    fun before() {
        fakeRemindersDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeRemindersDataSource
        )
        stopKoin()
    }

    @Test
    fun reminders_showLoading() {
        mainCoroutineRule.pauseDispatcher()

        // Load the reminders
        remindersListViewModel.loadReminders()

        // loading indicator is shown
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        // Then progress indicator is hidden.
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun load_reminders_Success() = mainCoroutineRule.runBlockingTest {
        // when
        fakeRemindersDataSource.saveReminder(
            ReminderDTO(
                "title",
                "desc",
                "loc",
                20.0,
                20.0
            )
        )
        remindersListViewModel.loadReminders()

        // then
        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().size, `is`(1))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun load_reminders_showError() {
        // when
        fakeRemindersDataSource.setReturnError(true)
        remindersListViewModel.loadReminders()

        // then
        assertThat(
            remindersListViewModel.showSnackBar.getOrAwaitValue(),
            `is`(FakeDataSource.mockErrorMessage)
        )
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }
}