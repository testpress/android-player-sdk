package com.tpstream.player.views

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tpstream.player.R
import com.tpstream.player.TestFragment
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class DownloadResolutionSelectionSheetTest {

    private lateinit var scenario: FragmentScenario<TestFragment>

    @Before
    fun setup() {
        scenario = launchFragmentInContainer()
    }

    @Test
    fun launchUi() {
        onView(withId(R.id.bottom_sheet_button)).perform(click())
        Thread.sleep(5000)
        onView(withText("Download Quality")).check(matches(isDisplayed()))
    }

}