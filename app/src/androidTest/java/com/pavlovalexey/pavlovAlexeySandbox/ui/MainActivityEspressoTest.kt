package com.pavlovalexey.pavlovAlexeySandbox.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pavlovalexey.pavlovAlexeySandbox.MainActivity
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    @Test
    fun launchMainActivity_displaysRootView() {
        ActivityScenario.launch(MainActivity::class.java)

        onView(isRoot()).check(matches(isDisplayed()))
    }
}
