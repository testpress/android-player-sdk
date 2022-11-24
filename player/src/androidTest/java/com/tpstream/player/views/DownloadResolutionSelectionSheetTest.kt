package com.tpstream.player.views

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.rule.ActivityTestRule
import com.tpstream.player.TpStreamPlayer
import com.tpstream.player.TpStreamPlayerFragment
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock


@MediumTest
@RunWith(AndroidJUnit4::class)
class DownloadResolutionSelectionSheetTest {

    @Mock
    private lateinit var player: TpStreamPlayer

    private var dialogFragment: DownloadResolutionSelectionSheet? = null

    @Rule
    val activityTestRule: ActivityTestRule<TestActivity> =
        ActivityTestRule(TestActivity::class.java)


    @Before
    fun showDialog() {
        // Create a dismiss listener to check with verifyDialogWasDismissed().
        dialogFragment = DownloadResolutionSelectionSheet(
            player,
            DefaultTrackSelector.Parameters.Builder(
                ApplicationProvider.getApplicationContext<Context?>().applicationContext
            )
                .build(),
            player.getCurrentTrackGroups()
        )
        dialogFragment!!.show(activityTestRule.activity.supportFragmentManager, "AdvancedSheetDownload")

        // Wait for dialog to be shown.
        onView(withText("Download Quality")).check(matches(isDisplayed()))
    }

    @After
    fun tearDown() {
        if (dialogFragment != null && dialogFragment!!.dialog != null && dialogFragment!!.dialog!!.isShowing
        ) {
            // Close the dialog
            Espresso.pressBack()
        }
    }

    @Test
    fun a() {
        val scenario = launchFragmentInContainer<TpStreamPlayerFragment>(Bundle())
        scenario.moveToState(Lifecycle.State.STARTED)
//        scenario.withFragment {
//            dialogFragment = DownloadResolutionSelectionSheet(
//                player,
//                DefaultTrackSelector.Parameters.Builder(ApplicationProvider.getApplicationContext<Context?>().applicationContext)
//                    .build(), player.getCurrentTrackGroups()
//            )
//            dialogFragment!!.show(
//                this.childFragmentManager.beginTransaction(),
//                "AdvancedSheetDownload"
//            )
//        }
        // Wait for dialog to be shown.

        // Wait for dialog to be shown.
        onView(withText("Download Quality")).check(matches(isDisplayed()))
    }

//    @Test
//    fun test_player_view_is_visible() {
//
//        onView(withText("Download Quality")).inRoot(isDialog())
//    }

}