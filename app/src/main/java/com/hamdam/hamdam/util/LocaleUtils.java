package com.hamdam.hamdam.util;

import android.app.Application;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.view.ContextThemeWrapper;

import java.util.Locale;

/**
 * Utility class for setting app locale. Thanks to stackoverflow user Roberto B
 * { https://stackoverflow.com/users/1188571/roberto-b } for his solution.
 */
public class LocaleUtils {
    private static Locale sLocale;

    public static void setLocale(Locale locale) {
        if (locale != null) {
            sLocale = locale;
            Locale.setDefault(sLocale);
        }
    }

    public static void updateLocale(ContextThemeWrapper wrapper) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                && sLocale != null) {
            Configuration configuration = new Configuration();
            configuration.setLocale(sLocale);

            // config.locale is deprecated in Android N
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLayoutDirection(configuration.getLocales().get(0));

            // Unfortunately, have to check sdkInt >= 17 again or compiler complains.
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLayoutDirection(configuration.locale);
            }
            wrapper.applyOverrideConfiguration(configuration);
        }
    }

    public static void updateLocale(Application app, Configuration configuration) {
        if (sLocale != null && Build.VERSION.SDK_INT
                < Build.VERSION_CODES.JELLY_BEAN_MR1) {

            //Wrapping the configuration to avoid Activity endless loop
            Configuration config = new Configuration(configuration);
            config.locale = sLocale;
            Resources res = app.getBaseContext().getResources();
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
    }

    /**
     * Reformat the String as LTR to ensure that it is laid out from
     * left to right, but it doesn't affect overall layout of
     * TextView.
     * Thanks to Sven Bendel (https://plus.google.com/+SvenBendel)
     * for this implementation.
     */
    public static String buildLtr(String input) {
        return "\u200E" + "\u200F" + input + "\u200E";
    }
}
