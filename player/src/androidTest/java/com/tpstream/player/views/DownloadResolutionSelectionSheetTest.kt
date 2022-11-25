package com.tpstream.player.views

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tpstream.player.TpStreamPlayer
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock


@RunWith(AndroidJUnit4::class)
class DownloadResolutionSelectionSheetTest {

    @Mock
    private lateinit var player: TpStreamPlayer

    private var dialogFragment: DownloadResolutionSelectionSheet? = null

    //private val bundle  = Bundle().

    @Test
    fun launchUi() {

        launchFragmentInContainer<BottomSheetDialogFragment>().let { scenario ->
            scenario
                .moveToState(Lifecycle.State.RESUMED)
                .onFragment { fragment ->
                    fragment.show(fragment.parentFragmentManager.beginTransaction(),"tag")

                }
        }
    }
}