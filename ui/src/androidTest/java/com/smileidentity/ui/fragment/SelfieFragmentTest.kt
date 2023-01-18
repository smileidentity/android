package com.smileidentity.ui.fragment

import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.testing.launchFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.LayoutAssertions.noOverlaps
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SelfieFragmentTest {
    @Test
    fun onCreateView_should_return_a_ComposeView() {
        val scenario = launchFragment { SelfieFragment.newInstance() }

        scenario.onFragment { assertTrue(it.view is ComposeView) }

        // Elements should not overlap
        onView(isRoot()).check(noOverlaps())
        onView(isRoot()).check { view, _ ->
            assertTrue(view.width > 0)
            assertTrue(view.height > 0)
        }
    }
}
