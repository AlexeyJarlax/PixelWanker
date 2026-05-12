package com.pavlovalexey.pavlovAlexeySandbox.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
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

        // В дереве может быть 2 текстовых ноды (контейнер + текст), поэтому проверяем факт
        // наличия хотя бы одного displayable и clickable узла с этим текстом.
        val nodes = composeRule.onAllNodesWithText(startGridText, useUnmergedTree = true)
            .fetchSemanticsNodes()

        val hasDisplayedClickableNode = nodes.any { node ->
            node.layoutInfo.isPlaced && hasClickAction().matches(node)
        }

        if (!hasDisplayedClickableNode) {
            throw AssertionError("Expected at least one displayed clickable node with text: $startGridText")
        }
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
        val dialogText = composeRule.activity.getString(R.string.about_privacy_policy_text)

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.bottom_nav_about))
            .performClick()

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.about_privacy_policy_button))
            .performClick()

        composeRule.onNodeWithText(dialogText, substring = true)
            .assertIsDisplayed()

        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.about_dialog_dismiss),
            useUnmergedTree = true
        ).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText(dialogText, substring = true).fetchSemanticsNodes().isEmpty()
        }

        composeRule.onNodeWithText(composeRule.activity.getString(R.string.about_privacy_policy_button))
            .assertIsDisplayed()
    }
}
