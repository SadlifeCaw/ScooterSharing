package dk.itu.moapd.scootersharing.fragments

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Rule
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.RootMatchers.isDialog
import dk.itu.moapd.scootersharing.R
import dk.itu.moapd.scootersharing.activities.ScooterSharingActivity

@RunWith(AndroidJUnit4::class)
class FragmentTests{
    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(ScooterSharingActivity::class.java)

    @Test
    fun userViewFragment_changeDisplayNameTest() {
        // Click on Profile in the top right corner
        onView(withId(R.id.profile))
            .perform(click())
        // Click on Change Display name button.
        onView(withId(R.id.change_displayname_button))
            .perform(click())

        // Insert Name into the text field.
        onView(withId(R.id.text_field))
            .perform(clearText(), typeText("Name"))

        // Click confirm.
        onView(withText("Confirm"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        // Check if the name changed.
        onView(withId(R.id.user_displayname))
            .check(matches(withText("Name")))
    }
}