package com.hamdam.hamdam.view;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;

import com.hamdam.hamdam.view.activity.MainActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerActions.openDrawer;
import static android.support.test.espresso.contrib.DrawerMatchers.isClosed;
import static android.support.test.espresso.contrib.DrawerMatchers.isOpen;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

/**
 * Test 123 (domestic violence hotline) fragment, a simple
 * {@Link Fragment} which launches native phone dialer.
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class Fragment123Test {

    @Rule
    public final IntentsTestRule<MainActivity> mIntentsRule
            = new IntentsTestRule<>(MainActivity.class);

    @Before
    public void setUp() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.drawer))
                .check(matches(isClosed()));
        openDrawer((com.hamdam.hamdam.R.id.drawer)); // Open Drawer
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.drawer)).check(matches(isOpen()));
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.navigation_view))
                .check(matches(isDisplayed()));

        // Navigate to 123 menu option
        onView(allOf(isDescendantOfA
                        (ViewMatchers.withId(com.hamdam.hamdam.R.id.app_drawer_layout)),
                ViewMatchers.withId(com.hamdam.hamdam.R.id.navigation_view)))
                .perform(RecyclerViewActions
                        .actionOnItemAtPosition(8, click()));

    }

    @Test
    public void testDialButtonDisplay() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.dial))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testTextDisplay() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.body_123))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testTitleDisplay() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.title_123))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testBackPressNavigation() {
        pressBack();
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.day_view))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testDialIntent() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.dial))
                .perform(click());
        intended(hasAction(Intent.ACTION_DIAL));
    }

    @Test
    public void testScreenRotation() {
        mIntentsRule.getActivity()
                .setRequestedOrientation
                        (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        testDialButtonDisplay();
        testTextDisplay();
        testTitleDisplay();
    }

    @Test
    public void testReturnAfterBackPress() {
        testBackPressNavigation();

        // Test returning to 123 fragment
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.drawer))
                .check(matches(isClosed()));
        openDrawer((com.hamdam.hamdam.R.id.drawer));

        onView(allOf(isDescendantOfA
                        (ViewMatchers.withId(com.hamdam.hamdam.R.id.app_drawer_layout)),
                ViewMatchers.withId(com.hamdam.hamdam.R.id.navigation_view)))
                .perform(RecyclerViewActions
                        .actionOnItemAtPosition(8, click()));

        testDialButtonDisplay();
    }

}
