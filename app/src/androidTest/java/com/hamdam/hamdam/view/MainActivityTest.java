package com.hamdam.hamdam.view;

import android.content.pm.ActivityInfo;
import android.support.test.espresso.NoActivityResumedException;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.MediumTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.hamdam.hamdam.view.activity.MainActivity;
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
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.fail;
import static org.hamcrest.Matchers.allOf;

/**
 * Test MainActivity UI
 */
@RunWith(AndroidJUnit4.class)
@MediumTest
public class MainActivityTest {

    @Rule
    public final ActivityTestRule<MainActivity> mActivityRule
            = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testDefaultDrawerDisplay() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.drawer))
                .check(matches(isClosed()));
    }

    @Test
    public void testMainLayoutDisplay() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.app_main_layout))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testMainToolbarDisplay() {
        onView(allOf(isDescendantOfA
                        (ViewMatchers.withId(com.hamdam.hamdam.R.id.drawer)),
                ViewMatchers.withId(com.hamdam.hamdam.R.id.toolbar)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testFragmentHolderDisplay() {
        // fragment for home etc
        onView(allOf(isDescendantOfA
                        (ViewMatchers.withId(com.hamdam.hamdam.R.id.app_main_layout)),
                ViewMatchers.withId(com.hamdam.hamdam.R.id.fragment_holder)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testHomeScreenDisplay() {
        // check home is displayed
        onView(allOf(isDescendantOfA
                        (ViewMatchers.withId(com.hamdam.hamdam.R.id.fragment_holder)),
                ViewMatchers.withId(com.hamdam.hamdam.R.id.home_layout_container)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testHomeScrollViewDisplay() {
        // scrollview
        onView(allOf(isDescendantOfA
                        (ViewMatchers.withId(com.hamdam.hamdam.R.id.home_layout_container)),
                ViewMatchers.withId(com.hamdam.hamdam.R.id.day_view)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void rotateScreenCheckSetup() {
        mActivityRule.getActivity().setRequestedOrientation
                (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        testDefaultDrawerDisplay();
        testFragmentHolderDisplay();
        testHomeScreenDisplay();
        testHomeScrollViewDisplay();

    }

    @Test
    public void testTextDisplay() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.daily_questions_title))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testDateTodayDisplay() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.date_today))
                .check(matches(isDisplayed()));
    }


    @Test
    public void testScreenRotation() {
        mActivityRule.getActivity()
                .setRequestedOrientation
                        (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        testTextDisplay();
        testDateTodayDisplay();
    }

    @Test
    public void testBackPressFromAllMenuPositions() {
        for (int i = 1; i < 11; i++) { // position 1 is home screen- not added twice to backstack
            if (i != 1) {
                navigate(i);
                testBackPressNavigation();
            }
        }
    }

    private void testBackPressNavigation() {
        pressBack();
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.home_layout_container))
                .check(matches(isDisplayed()));
    }

    private void navigate(int whichOption) {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.drawer))
                .check(matches(isClosed()));
        openDrawer((com.hamdam.hamdam.R.id.drawer)); // Open Drawer
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.drawer)).check(matches(isOpen()));
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.navigation_view))
                .check(matches(isDisplayed()));

        // Navigate to a menu option
        onView(allOf(isDescendantOfA
                        (ViewMatchers.withId(com.hamdam.hamdam.R.id.app_drawer_layout)),
                ViewMatchers.withId(com.hamdam.hamdam.R.id.navigation_view)))
                .perform(RecyclerViewActions //include scrollTO
                        .actionOnItemAtPosition(whichOption, click()));
    }

    /*
     * Test backpress from buttons on homescreen
     */
    @Test
    public void testBackPressFromQuestions() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.daily_questions_container))
                .perform(click());
        pressBack();
        testHomeScreenDisplay();
    }

    @Test
    public void testBackPressFromInfographic() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.infographic_container))
                .perform(click());
        pressBack();
        testHomeScreenDisplay();
    }


    @Test
    public void testBackPressFromHealth() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.health_info_title_container))
                .perform(click());
        pressBack();
        testHomeScreenDisplay();
    }

    @Test
    public void testBackPressFromMarriageRights() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.marriage_rights_title_container))
                .perform(click());
        pressBack();
        testHomeScreenDisplay();
    }

    @Test // navigate twice via menus, backpress, arrive back at home screen
    public void testNavigationBackPress() {
        navigate(3);
        pressBack();
        testHomeScreenDisplay();

        navigate(4);
        navigate(9);
        pressBack();
        testHomeScreenDisplay();

        navigate(9);
        navigate(7);
        pressBack();
        testHomeScreenDisplay();
    }

    @Test
    public void testFragmentBackPress() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.marriage_rights_title_container))
                .perform(click());
        navigate(8);
        pressBack();
        testHomeScreenDisplay();
    }

    @Test
    public void testNavigationAndFragmentBackPress() {
        navigate(3);
        pressBack();
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.health_info_title_container))
                .perform(click());
        pressBack();
        testHomeScreenDisplay();

    }

    @Test(expected = NoActivityResumedException.class)
    public void testBackPressQuitApp() {
        pressBack();
        fail("Expected app to quit");
    }

    @Test(expected = NoActivityResumedException.class)
    public void testDoubleBackPressQuitApp() {
        navigate(3);
        navigate(7);
        pressBack();
        pressBack();
        fail("Expected app to quit");
    }

}

