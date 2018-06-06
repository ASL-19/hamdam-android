package com.hamdam.hamdam.view;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.view.View;
import android.widget.ImageButton;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Custom Matchers to use in UI testing. Thanks to @dbottilo
 * (https://medium.com/@dbottillo/android-ui-test-espresso-
 * matcher-for-imageview-1a28c832626f#.fe8bjcmma) and Steve Mulder
 * (http://blog.xebia.com/android-custom-matchers-in-espresso/) for
 * their tutorials on creating matchers.
 */
public class EspressoTestMatchers {

    public static Matcher<View> withDrawable(final int resourceId) {
        return new DrawableMatcher(resourceId);
    }

    public static Matcher<View> noDrawable() {
        return new DrawableMatcher(-1);
    }

    public static class DrawableMatcher extends TypeSafeMatcher<View> {
        private final int expectedId;

        public DrawableMatcher(int resourceId) {
            super(ImageButton.class); // Instantiate a matcher
            this.expectedId = resourceId;
        }

        @Override
        protected boolean matchesSafely(View item) {
            if (!(item instanceof AppCompatImageButton)) {
                return false;
            }

            AppCompatImageButton button = (AppCompatImageButton) item;

            if (expectedId < 0) {
                return button.getDrawable() == null;
            }

            Drawable actualDrawable = ContextCompat.getDrawable(item.getContext(), item.getId());
            Drawable expectedDrawable = ContextCompat.getDrawable(item.getContext(), expectedId);

            if (expectedDrawable == null) {
                return false;
            }

            return actualDrawable.equals(expectedDrawable);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("with drawable from resource id: ");
            description.appendValue(expectedId);
        }
    }


    /*
     * Determine whether correct fragment is displaying
     */
//    public static Matcher<Fragment> onboardFragmentMatcher(final View fragment, int positionId) {
//        return new OnboardFragmentMatcher(fragment, positionId);
//    }
//
//
//    public static class OnboardFragmentMatcher extends TypeSafeMatcher<View> {
//        private View fragment;
//        private int id;
//
//        public OnboardFragmentMatcher(View fragment, int id) {
//            this.fragment = fragment;
//            this.id = id;
//        }
//
//        @Override
//        protected boolean matchesSafely(View item) {
//            if (!(item instanceof OnboardFragment)) {
//                return false;
//            }
//
//            if (item == null) {
//                Log.d("EspressoMatcher", "OnboardFragmentMatcher matchesSafely: null target fragment");
//                return false;
//            }
//
//            OnboardFragment targetFragment = (OnboardFragment) item;
//            return targetFragment.getId() == this.id;
//        }
//
//        @Override
//        public void describeTo(Description description) {
//            description.appendText("with class name: ");
//            description.appendText(fragment.getClass().getName());
//            this.describeTo(description); // Is this OK/
//        }
//    }
}

