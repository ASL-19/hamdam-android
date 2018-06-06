package com.hamdam.hamdam.view;

import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.hamdam.hamdam.view.activity.OnboardingActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Test Onboarding Activity UI using espresso and mockito (in progress).
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class OnboardingActivityTest {

    @Rule
    public ActivityTestRule m_OnboardRule = new ActivityTestRule(OnboardingActivity.class);

    @Test
    public void testSkipButtonVisible() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.skip))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testNextButtonVisible() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.next))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSkipButtonClick() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.skip))
                .perform(click())
                .check(matches(ViewMatchers.withText(com.hamdam.hamdam.R.string.skip))); // not null but does stuff
    }

    @Test
    public void testNextButtonClick() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.next))
                .perform(click())
                .check(matches(ViewMatchers.withText(com.hamdam.hamdam.R.string.next))); // not null but does stuff
    }

    @Test
    public void testIconPagerVisible() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.viewPager))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testViewPagerFirstChildFragment() {
//        onView(withParent(withId(R.id.viewPager)))
//                .check(matches(isCompletelyDisplayed()))
//                .check(matches(withId(R.layout.fragment_onboard_periodselector)));
    }

    @Test
    public void testIconPagerSwipeRight() {
//        onView(withParent(withId(R.id.viewPager)))
//                .perform(swipeRight())
//                .check(matches(hasDescendant(withId(R.layout.fragment_onboard_numberselector))))
//                .perform(swipeRight())
//                .check(matches(hasDescendant(withId(R.layout.fragment_onboard_numberselector))))
//                .perform(swipeRight())
//                .check(matches(hasDescendant(withId(R.layout.fragment_onboard_numberselector))));
    }

}
