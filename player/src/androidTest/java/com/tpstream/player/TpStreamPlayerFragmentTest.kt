package com.tpstream.player


import android.os.Bundle
import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class TpStreamPlayerFragmentTest {

    @Test
    fun testTpStreamPlayerFragmentLaunch() {
        launchFragment<TpStreamPlayerFragment>(Bundle())
    }

    @Test
    fun testTpStreamPlayerFragmentLaunchInContainer() {
        launchFragmentInContainer<TpStreamPlayerFragment>(Bundle())
        onView(withId(R.id.video_view)).check(matches(isDisplayed()))
    }

    @Test
    fun testState() {
        val scenario =
            launchFragmentInContainer<TpStreamPlayerFragment>(initialState = Lifecycle.State.INITIALIZED)
        scenario.moveToState(Lifecycle.State.CREATED)


    }

}