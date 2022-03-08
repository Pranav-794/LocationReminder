package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    //TODO: provide testing to the SaveReminderView and its live data objects
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeRemindersDataSource: FakeDataSource
    private lateinit var application: Application

    @Before
    fun before() {
        fakeRemindersDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            fakeRemindersDataSource
        )
        application = ApplicationProvider.getApplicationContext()
        stopKoin()
    }

    @Test
    fun saveReminder_loading_navigation() {
        // when
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.saveReminder(
            ReminderDataItem(
                "title",
                "desc",
                "loc",
                20.0,
                20.0
            )
        )
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))
        // then
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
        assertThat(
            saveReminderViewModel.showToast.getOrAwaitValue(), `is`(
                application.getString(
                    R.string.reminder_saved
                )
            )
        )
        Assert.assertEquals(
            saveReminderViewModel.navigationCommand.getOrAwaitValue(),
            NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToReminderListFragment())
        )
    }

    @Test
    fun validateData_emptyTitle() {
        saveReminderViewModel.validateAndSaveReminder(
            ReminderDataItem(
                null,
                "desc",
                "loc",
                20.0,
                20.0
            )
        )
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_enter_title)
        )
        assertThat(saveReminderViewModel.savedReminder, notNullValue())
    }

    @Test
    fun validateData_emptyLocation() {
        saveReminderViewModel.validateAndSaveReminder(
            ReminderDataItem(
                "title",
                "desc",
                null,
                20.0,
                20.0
            )
        )
        assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_select_location)
        )
        assertThat(saveReminderViewModel.savedReminder, notNullValue())
    }

    @Test
    fun savePOI_Location() {
        saveReminderViewModel.savePOILocation(
            PointOfInterest(
                LatLng(20.0, 20.0),
                "123",
                "Place Name"
            )
        )
        assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue(), notNullValue())
        assertThat(
            saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(),
            notNullValue()
        )
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), notNullValue())
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), notNullValue())

    }
}