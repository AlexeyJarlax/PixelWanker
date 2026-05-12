package com.pavlovalexey.pavlovAlexeySandbox.ui

import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pavlovalexey.pavlovAlexeySandbox.MainActivity
import com.pavlovalexey.pavlovAlexeySandbox.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityEspressoTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchMainActivity_displaysGridPageContent() {
        val startGridText = composeRule.activity.getString(R.string.start_grid)

        composeRule.onAllNodes(hasText(startGridText) and hasClickAction(), useUnmergedTree = true)
            .assertCountEquals(1)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun bottomBar_navigatesToAppsAndAboutPages() {
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.bottom_nav_apps))
            .performClick()

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.search_apps_placeholder))
            .assertIsDisplayed()

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.bottom_nav_about))
            .performClick()

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.about_privacy_policy_button))
            .assertIsDisplayed()
    }

    @Test
    fun aboutPage_opensAndClosesPrivacyPolicyDialog() {
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.bottom_nav_about))
            .performClick()

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.about_privacy_policy_button))
            .performClick()

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.about_privacy_policy_text))
            .assertIsDisplayed()

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.about_dialog_dismiss), useUnmergedTree = true)
            .performClick()

        composeRule.onAllNodes(hasText(composeRule.activity.getString(R.string.about_privacy_policy_text)))
            .assertAny { !it.layoutInfo.isPlaced }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.about_privacy_policy_button))
            .assertIsDisplayed()
    }
}
