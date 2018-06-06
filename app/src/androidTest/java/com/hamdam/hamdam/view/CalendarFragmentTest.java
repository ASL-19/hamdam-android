package com.hamdam.hamdam.view;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.RootMatchers;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;
import android.util.Log;

import com.github.ebraminio.droidpersiancalendar.calendar.PersianDate;
import com.github.ebraminio.droidpersiancalendar.utils.Utils;
import com.hamdam.hamdam.view.activity.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;

/**

/**
 * Test calendar fragment.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest

public class CalendarFragmentTest extends AndroidTestCase {
    Context context;

    @Rule
    public final ActivityTestRule<MainActivity> mActivityRule
            = new ActivityTestRule<MainActivity>(MainActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            context = InstrumentationRegistry.getTargetContext();
            if (context != null) {

                // Skip onboarding
                PreferenceManager.getDefaultSharedPreferences(context)
                        .edit()
                        .putBoolean(context.getString(com.hamdam.hamdam.R.string.onboarding_complete_key), true)
                        .apply();
                //DatabaseHelperImpl.getInstance(context).clearUserHistory();
            } else {
               Log.e("CalendarFragmentTest", "Null context while setting up test rule");
            }
        }
    };

    @Before
    public void setUp() {
        onView(allOf(isDescendantOfA
                        (ViewMatchers.withId(com.hamdam.hamdam.R.id.home_layout_container)),
                ViewMatchers.withId(com.hamdam.hamdam.R.id.today_indicator)))
                .check(matches(isDisplayed()))
        .perform(click());
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCalendarDisplay() {
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.calendar_pager))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSelectDate() {
        onView(allOf(ViewMatchers.withId(com.hamdam.hamdam.R.id.RecyclerView), ViewMatchers.isCompletelyDisplayed()))
                .perform(RecyclerViewActions.actionOnItemAtPosition(15, click()));
    }

    @Test
    public void testSwipeMonthBack() {
        onView(allOf(ViewMatchers.withId(com.hamdam.hamdam.R.id.calendar_pager), isDisplayed()))
                .perform(swipeLeft());
        onView(allOf(ViewMatchers.withId(com.hamdam.hamdam.R.id.RecyclerView), ViewMatchers.isCompletelyDisplayed()))
                .perform(RecyclerViewActions.actionOnItemAtPosition(15, click()));
    }

    @Test
    public void testSwipeMonthForward() {
        onView(allOf(ViewMatchers.withId(com.hamdam.hamdam.R.id.calendar_pager), isDisplayed()))
                .perform(swipeRight());
        onView(allOf(ViewMatchers.withId(com.hamdam.hamdam.R.id.RecyclerView), ViewMatchers.isCompletelyDisplayed()))
                .perform(RecyclerViewActions.actionOnItemAtPosition(15, click()));
    }


    @Test
    public void testSelectToggleAddPeriod() {
        PersianDate date = Utils.getToday();

        // Calculate position in recycleview
        int firstDayOfMonth = new PersianDate(date.getYear(), date.getMonth(), 1).getDayOfWeek();
        int which = (date.getDayOfMonth()) - 7 - firstDayOfMonth + (6 - (date.getDayOfMonth() % 7) * 2);
        onView(allOf(ViewMatchers.withId(com.hamdam.hamdam.R.id.RecyclerView), ViewMatchers.isCompletelyDisplayed()))
                .perform(RecyclerViewActions.actionOnItemAtPosition(which, click()));
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.period_edit_container)).perform(click());

        // Triggered dialog for today; check shows
        onView(ViewMatchers.withText(com.hamdam.hamdam.R.string.edit_later_hint)).inRoot(RootMatchers.isDialog())
                .check(matches(isDisplayed()));
        onView(ViewMatchers.withId(com.hamdam.hamdam.R.id.dialog_toggle)).perform(click());
        onView(ViewMatchers.withText(com.hamdam.hamdam.R.string.ok)).inRoot(RootMatchers.isDialog()).perform(click());

    }
}
