package com.tpstream.player.views

import android.os.Bundle
import androidx.fragment.app.testing.launchFragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tpstream.player.TpStreamPlayerFragment
import com.tpstream.player.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DownloadResolutionSelectionSheetTest{

    @Test
    fun test_player_view_is_visible() {
        launchFragmentInContainer<TpStreamPlayerFragment>(Bundle())

        onView(withId(R.id.exo_download)).perform(click())

        onView(withText("Download Quality")).inRoot(isDialog())
    }

}